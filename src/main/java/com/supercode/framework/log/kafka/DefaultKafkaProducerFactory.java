package com.supercode.framework.log.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Properties;

/**
 * @Author: jonathan.ji
 * @Date: 2022/1/23 21:15
 * @Desc:
 */
public class DefaultKafkaProducerFactory implements KafkaProducerFactory {

    @Override
    public org.apache.kafka.clients.producer.Producer<byte[], byte[]> newKafkaProducer(final Properties config) {
        if (PRODUCER_CACHE.containsKey(config)) {
            return PRODUCER_CACHE.get(config);
        } else {
            KafkaProducer<byte[], byte[]> kafkaProducer = new KafkaProducer<>(config);
            PRODUCER_CACHE.put(config, kafkaProducer);
            return kafkaProducer;
        }

    }

}
