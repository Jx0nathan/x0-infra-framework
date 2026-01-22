package com.supercode.framework.log.listener;

import com.supercode.framework.log.config.Log4j2Configuration;
import com.supercode.framework.log.exception.GlobalUncaughtExceptionHandler;
import com.supercode.framework.utils.SampleUtil;
import com.supercode.master.env.EnvUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.time.Duration;

/**
 * @Author: jonathan.ji
 * @Date: 2022/1/23 21:35
 * @Desc: 配置Log4j2初始化
 */
@Log4j2
public class LoggingApplicationRunListener implements SpringApplicationRunListener {

    private static final String MANAGEMENT_LOGGING_ENABLE = "management.logging.enable";

    public LoggingApplicationRunListener(SpringApplication application, String[] args) {
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        try {
            System.setProperty("localhost.default.nic.list", "bond0,eth0,em0,br0,en0,gpd0");
            if (BooleanUtils.toBoolean(environment.getProperty(MANAGEMENT_LOGGING_ENABLE, "true"))) {
                Log4j2Configuration logConfiguration = Log4j2Configuration.createLogConfiguration(environment);
                logConfiguration.init();
            }
            SampleUtil.init(environment);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        Thread.setDefaultUncaughtExceptionHandler(new GlobalUncaughtExceptionHandler());
    }

    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        if (BooleanUtils.toBoolean(context.getEnvironment().getProperty(MANAGEMENT_LOGGING_ENABLE, "true"))) {
            if (EnvUtil.isEcs() || EnvUtil.isK8s() || EnvUtil.isNotMacOs()) {
                log.info("will resetConsoleAppenderLevel");
                Log4j2Configuration.resetConsoleAppenderLevel();
            }
        }
        Log4j2Configuration.addSampleFilter();
    }
}
