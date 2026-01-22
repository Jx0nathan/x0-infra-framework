package com.supercode.framework.log.config;

import org.springframework.core.env.Environment;

/**
 * @Author: jonathan.ji
 * @Date: 2022/1/23 20:50
 * @Desc:
 */
public abstract class AbstractLogConfiguration {

    private static final String LOG_KAFKA_TOPIC = "managent.logs.kafka.topic";

    private static final String LOG_KAFKA_BOOTSTRAPSERVERS = "managent.logs.kafka.bootstrapservers";
    private static final String LOG_PATTERN_KEY = "managent.logs.pattern";
    private static final String LOG4J2_PATTERN_DEFAULT = "[%-p] [%d{yyyy.MM.dd HH:mm:ss.SSS}] [${sys:local-ip}] [%X{traceId}] [%t] [%c(%L)] - %m%n";
    private final Environment env;

    public AbstractLogConfiguration(Environment env) {
        this.env = env;
    }

    public String getBootstrapservers() {
        return env.getProperty(LOG_KAFKA_BOOTSTRAPSERVERS);
    }

    public String getKafkaTopic() {
        return env.getProperty(LOG_KAFKA_TOPIC, "bizLog");
    }

    public String getLog4jPattern() {
        return env.getProperty(LOG_PATTERN_KEY, LOG4J2_PATTERN_DEFAULT);
    }

    public String getProperty(String key) {
        return this.env.getProperty(key);
    }

    public abstract void init();

}
