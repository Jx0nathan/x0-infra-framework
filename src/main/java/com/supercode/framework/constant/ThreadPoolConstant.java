package com.supercode.framework.constant;

import java.util.concurrent.TimeUnit;

/**
 * @Author: jonathan.ji
 * @Date: 2021/8/23 16:44
 * @Desc: 线程池常量
 */
public final class ThreadPoolConstant {

    /**
     * 默认核心线程数
     **/
    public static final int DEFAULT_CORE_POOL_SIZE = 4;
    /**
     * 默认最大线程数
     **/
    public static final int DEFAULT_MAXIMUM_POOL_SIZE = 8;
    /**
     * 默认线程生存时间
     **/
    public static final int DEFAULT_KEEP_ALIVE_TIME = 5;
    /**
     * 默认线程生存时间单位
     **/
    public static final TimeUnit DEFAULT_KEEP_ALIVE_TIME_UNIT = TimeUnit.MINUTES;
    /**
     * 默认等待队列最大容量
     **/
    public static final int DEFAULT_MAX_QUEUE_CAPACITY = 1000;
    /**
     * 线程池触发拒绝策略日志信息
     **/
    public static final String BUSY_WARN_MSG = "【thread pool busy】trigger rejected policy";

    private ThreadPoolConstant() {
        throw new IllegalStateException("Utility class");
    }

}
