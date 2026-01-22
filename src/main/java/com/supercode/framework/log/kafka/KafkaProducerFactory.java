package com.supercode.framework.log.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: jonathan.ji
 * @Date: 2022/1/23 21:15
 * @Desc:
 */
public interface KafkaProducerFactory {

    Map<Properties, KafkaProducer<byte[], byte[]>> PRODUCER_CACHE =
            new ConcurrentHashMap<>();

    static KafkaProducer<byte[], byte[]> getLogKafkaProducer() {
        if (!PRODUCER_CACHE.isEmpty()) {
            for (KafkaProducer<byte[], byte[]> value : PRODUCER_CACHE.values()) {
                return value;
            }
        }
        return null;
    }

    Producer<byte[], byte[]> newKafkaProducer(Properties config);

}
