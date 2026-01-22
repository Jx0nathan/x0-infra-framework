package com.supercode.framework.log.kafka;

import com.supercode.framework.executor.ThreadPoolMonitorRegister;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @author jonathan.ji
 * @date 2022/6/17
 * @description
 */
public class LogThreadPool extends ThreadPoolExecutor {
    private static final LogThreadPool logThreadPool = new LogThreadPool(
            3, 5, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(500),
            "kafka-sendlog-pool-%d",
            new ThreadPoolExecutor.DiscardPolicy());

    private LogThreadPool(int corePoolSize,
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
                , new ThreadFactoryBuilder().setNameFormat(executorName).build()
                , handler
        );
        ThreadPoolMonitorRegister.registerCatHeartbeat(this, executorName);
    }

    public static LogThreadPool getInstance() {
        return logThreadPool;
    }
}
