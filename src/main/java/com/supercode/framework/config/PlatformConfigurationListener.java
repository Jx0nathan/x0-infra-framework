package com.supercode.framework.config;

import com.supercode.master.env.EnvUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.annotation.Order;

/**
 * @author jonathan.ji
 */
@Order(value = 1)
public class PlatformConfigurationListener implements SpringApplicationRunListener {

    public PlatformConfigurationListener(SpringApplication application, String[] args) {
        if (EnvUtil.isDev() || EnvUtil.isQa()) {
            System.setProperty("springdoc.api-docs.enabled", "true");
        } else {
            // 线上环境关闭swagger
            System.setProperty("springdoc.api-docs.enabled", "false");
        }
    }
}
