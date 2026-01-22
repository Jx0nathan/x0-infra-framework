package com.supercode.framework.executor;

import lombok.extern.log4j.Log4j2;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;

/**
 * @Author: Aaron
 * @Date: 2021/10/28 23:37
 * @Desc:
 */
@Log4j2
@Configuration
@ConditionalOnProperty(value = "supercode-framework-async-config-enable", havingValue = "true", matchIfMissing = true)
public class AsyncConfig implements AsyncConfigurer {

    public AsyncConfig() {
        log.info("######### AsyncConfig enable ##########");
    }

    @Override
    public Executor getAsyncExecutor() {
        return new AbstractThreadPoolBasicExecutor("supercode-framework-default") {
        };
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

}
