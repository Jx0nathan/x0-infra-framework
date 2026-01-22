package com.supercode.framework.executor;


import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.status.StatusExtension;
import com.dianping.cat.status.StatusExtensionRegister;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.supercode.master.utils.platform.RpcContext;
import com.supercode.master.utils.platform.TrackingUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static com.supercode.framework.constant.ThreadPoolConstant.*;

/**
 * @Author: jonathan.ji
 * @Date: 2021/8/23 10:33
 * @Desc: 线程池基础实现，定义为抽象，目的是不要直接初始化当前类，需要通过继承后使用，方便protect方法的包装调用与重写；
 * 这里不提供空参构造，建议指定线程池名称方便区分
 */
@Log4j2
public abstract class AbstractThreadPoolBasicExecutor extends ThreadPoolExecutor {

    public static final String GRAY_ENV_HEADER = "X-GRAY-ENV";
    private static final String EXECUTOR_CONCAT = "-executor-%d";
    private final String threadPoolExecutorName;

    /**
     * 自定义线程池
     *
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     * @param executorName
     * @param handler
     */
    protected AbstractThreadPoolBasicExecutor(int corePoolSize,
                                              int maximumPoolSize,
                                              long keepAliveTime,
                                              TimeUnit unit,
                                              BlockingQueue<Runnable> workQueue,
                                              String executorName,
                                              RejectedExecutionHandler handler) {
        super(
                corePoolSize
                , maximumPoolSize
                , keepAliveTime
                , unit
                , workQueue
                , new ThreadFactoryBuilder().setNameFormat(executorName.concat(EXECUTOR_CONCAT)).build()
                , handler
        );
        threadPoolExecutorName = executorName;
        catHeartbeat(this, executorName);
    }

    /**
     * 默认线程池：自定义线程池名称
     *
     * @param executorName
     */
    protected AbstractThreadPoolBasicExecutor(String executorName) {
        super(
                DEFAULT_CORE_POOL_SIZE
                , DEFAULT_MAXIMUM_POOL_SIZE
                , DEFAULT_KEEP_ALIVE_TIME
                , DEFAULT_KEEP_ALIVE_TIME_UNIT
                , new LinkedBlockingQueue<>(DEFAULT_MAX_QUEUE_CAPACITY)
                , new ThreadFactoryBuilder().setNameFormat(executorName.concat(EXECUTOR_CONCAT)).build()
                , getAbortPolicy()
        );
        threadPoolExecutorName = executorName;
        catHeartbeat(this, executorName);
    }

    /**
     * 默认线程池：自定义拒绝策略
     *
     * @param executorName
     * @param rejectedExecutionHandler
     */
    protected AbstractThreadPoolBasicExecutor(String executorName, RejectedExecutionHandler rejectedExecutionHandler) {
        super(
                DEFAULT_CORE_POOL_SIZE
                , DEFAULT_MAXIMUM_POOL_SIZE
                , DEFAULT_KEEP_ALIVE_TIME
                , DEFAULT_KEEP_ALIVE_TIME_UNIT
                , new LinkedBlockingQueue<>(DEFAULT_MAX_QUEUE_CAPACITY)
                , new ThreadFactoryBuilder().setNameFormat(executorName.concat(EXECUTOR_CONCAT)).build()
                , rejectedExecutionHandler
        );
        threadPoolExecutorName = executorName;
        catHeartbeat(this, executorName);
    }

    protected static RejectedExecutionHandler getCallerRunsPolicy() {
        return new ThreadPoolExecutor.CallerRunsPolicy() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                log.warn(BUSY_WARN_MSG);
                super.rejectedExecution(r, e);
            }
        };
    }

    protected static RejectedExecutionHandler getAbortPolicy() {
        return new ThreadPoolExecutor.AbortPolicy() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                log.warn(BUSY_WARN_MSG);
                super.rejectedExecution(r, e);
            }
        };
    }

    protected static RejectedExecutionHandler getDiscardPolicy() {
        return new ThreadPoolExecutor.DiscardPolicy() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                log.warn(BUSY_WARN_MSG);
                super.rejectedExecution(r, e);
            }
        };
    }

    protected static RejectedExecutionHandler getDiscardOldestPolicy() {
        return new ThreadPoolExecutor.DiscardOldestPolicy() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                log.warn(BUSY_WARN_MSG);
                super.rejectedExecution(r, e);
            }
        };
    }

    /**
     * 重写execute函数 实现灰度标签和traceId跨线程透传
     * 这里方法加final，禁止子类重写
     *
     * @param command 任务
     */
    @Override
    public final void execute(Runnable command) {
        // trace id
        String trace = TrackingUtil.getTrace();

        // gray标签等
        RpcContext rpcContext = RpcContext.getContext();
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String grayEnvHeader = null;
        if (requestAttributes != null) {
            try {
                grayEnvHeader = requestAttributes.getRequest().getHeader(GRAY_ENV_HEADER);
            } catch (Exception e) {
                log.warn("AbstractThreadPoolBasicExecutor get grayEnvHeader error: {}", e.getMessage());
            }
        }
        // cat跨线程传递
        final Cat.Context context = new ExecutorCatContext();
        Cat.logRemoteCallClient(context);

        String finalGrayEnvHeader = grayEnvHeader;
        log.info("【execute】trace: {}, grayEnvHeader: {}", trace, grayEnvHeader);
        super.execute(() -> {
            TrackingUtil.saveTrace(trace);
            RpcContext.getContext().set(rpcContext.get());
            if (finalGrayEnvHeader != null) {
                RpcContext.getContext().set(GRAY_ENV_HEADER, finalGrayEnvHeader);
            }
            RequestContextHolder.setRequestAttributes(requestAttributes, true);
            Transaction forkedTransaction = Cat.newTransaction("ThreadPoolExecutor", threadPoolExecutorName);
            Cat.logRemoteCallServer(context);
            try {
                command.run();
                forkedTransaction.setStatus(Message.SUCCESS);
            } catch (Exception e) {
                log.error("【task execute error】", e);
                Cat.logError(e);
                forkedTransaction.setStatus(e);
            } finally {
                TrackingUtil.clearTrace();
                RpcContext.clear();
                forkedTransaction.complete();
            }
        });
    }

    /**
     * cat添加线程池埋点
     *
     * @param executor
     * @param executorName
     */
    private void catHeartbeat(ThreadPoolExecutor executor, String executorName) {
        StatusExtensionRegister.getInstance().register(new StatusExtension() {
            @Override
            public String getId() {
                return "Custom threadPool ".concat(executorName);
            }

            @Override
            public String getDescription() {
                return "Custom threadPool ".concat(executorName);
            }

            @Override
            public Map<String, String> getProperties() {
                Map<String, String> map = new HashMap<>(5);
                map.put("largest-pool-size", String.valueOf(executor.getLargestPoolSize()));
                map.put("max-pool-size", String.valueOf(executor.getMaximumPoolSize()));
                map.put("core-pool-size", String.valueOf(executor.getCorePoolSize()));
                map.put("current-pool-size", String.valueOf(executor.getPoolSize()));
                map.put("queue-size", String.valueOf(executor.getQueue().size()));
                return map;
            }
        });
    }

    static class ExecutorCatContext implements Cat.Context {

        private final Map<String, String> properties = new HashMap<>();

        @Override
        public void addProperty(String key, String value) {
            properties.put(key, value);
        }

        @Override
        public String getProperty(String key) {
            return properties.get(key);
        }
    }

}
