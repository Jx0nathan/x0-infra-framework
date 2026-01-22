package com.supercode.framework.log.kafka;

import com.supercode.framework.log.layout.MaskUtil;
import com.supercode.master.env.EnvUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.util.Log4jThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author: jonathan.ji
 * @Date: 2022/1/23 21:15
 * @Desc:
 */
public class AdvancedKafkaManager extends AbstractManager {
    public static final String MANAGEMENT_LOGGING_MASK_ENABLE = "management.logging.mask.enable";
    private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedKafkaManager.class);
    private static final ExecutorService SEND_MESSAGE_EXECUTOR = LogThreadPool.getInstance();
    private static final KafkaProducerFactory KAFKA_PRODUCER_FACTORY = new DefaultKafkaProducerFactory();
    private final Properties config = new Properties();
    private Producer<byte[], byte[]> producer;

    public AdvancedKafkaManager(final LoggerContext loggerContext, final String name, final String bootstrapServers) {
        super(loggerContext, name);
        config.put(ProducerConfig.CLIENT_ID_CONFIG, "log-appender-1");
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.ACKS_CONFIG, "0");
        config.put(ProducerConfig.RETRIES_CONFIG, 0);
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        PathMatchingResourcePatternResolver pmrpr = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = pmrpr.getResources("classpath*:log/log-pattern-mask.txt");
            MaskUtil.init(resources);
        } catch (Exception e) {
            LOGGER.warn("cannot load or parse the log-pattern-mask.txt file!");
        }
    }

    private void closeProducer(final long timeout, final TimeUnit timeUnit) {
        if (producer != null) {
            final Thread closeThread = new Log4jThread(() -> {
                if (producer != null) {
                    producer.close();
                }
            }, "AdvancedKafkaManager-CloseThread");
            closeThread.setDaemon(true);
            closeThread.start();
            try {
                closeThread.join(timeUnit.toMillis(timeout));
            } catch (final InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean releaseSub(final long timeout, final TimeUnit timeUnit) {
        if (timeout > 0) {
            closeProducer(timeout, timeUnit);
        }
        return true;
    }

    public void send(final String topic, final String text) {
        SEND_MESSAGE_EXECUTOR.submit(() -> {
            if (producer != null) {
                try {
                    String enable = EnvUtil.getProperty(MANAGEMENT_LOGGING_MASK_ENABLE, "false");
                    if (BooleanUtils.toBoolean(enable)) {
                        StringBuilder sb = MaskUtil.maskSensitiveInfo(text);
                        final ProducerRecord<byte[], byte[]> newRecord =
                                new ProducerRecord<>(topic, sb.toString().getBytes());
                        producer.send(newRecord);
                    } else {
                        final ProducerRecord<byte[], byte[]> newRecord =
                                new ProducerRecord<>(topic, text.getBytes());
                        producer.send(newRecord);
                    }
                } catch (Exception e) {
                    // do nothing
                }
            }
        });
    }

    public void startup() {
        producer = KAFKA_PRODUCER_FACTORY.newKafkaProducer(config);
    }
}
