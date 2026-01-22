package com.supercode.framework.log.mask;

import com.supercode.framework.log.layout.LogMaskWordUtil;
import com.supercode.framework.log.layout.MaskUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;
import java.util.List;

/**
 * @author jonathan.ji
 */
@Order(Integer.MAX_VALUE)
public class MaskKeyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger log = LoggerFactory.getLogger(MaskKeyInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        String plainTextPatternsStr = env.getProperty("mask.key.plainTextPatterns");
        if (StringUtils.isNotEmpty(plainTextPatternsStr)) {
            List<String> plainTextPatterns = Arrays.asList(plainTextPatternsStr.split("\n"));
            MaskUtil.init(plainTextPatterns, env.getProperty("spring.application.name"));
            log.info("initialized the plain text patterns for MaskUtil");
        }

        String jsonFieldKeysStr = env.getProperty("mask.key.jsonFieldKeys");
        if (StringUtils.isNotEmpty(jsonFieldKeysStr)) {
            List<String> jasonFieldKeys = Arrays.asList(jsonFieldKeysStr.split(","));
            LogMaskWordUtil.init(jasonFieldKeys);
            log.info("initialized the json field keys for LogMaskUtils. jsonFieldKeys: {}", jsonFieldKeysStr);
        }
    }
}
