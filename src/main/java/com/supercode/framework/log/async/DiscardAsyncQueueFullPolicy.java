package com.supercode.framework.log.async;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.async.AsyncQueueFullPolicy;
import org.apache.logging.log4j.core.async.EventRoute;
import org.apache.logging.log4j.util.PropertiesUtil;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: jonathan.ji
 * @Date: 2022/1/23 20:35
 * @Desc: 异步日志丢弃策略
 */
@Log4j2
public class DiscardAsyncQueueFullPolicy implements AsyncQueueFullPolicy {

    private static final String PROPERTY_NAME_DISCARDING_THRESHOLD_LEVEL = "log4j2.DiscardThreshold";
    private final Level thresholdLevel;
    private final AtomicLong discardCount = new AtomicLong();

    public DiscardAsyncQueueFullPolicy() {
        Level level = Level.toLevel(
                PropertiesUtil.getProperties().getStringProperty(PROPERTY_NAME_DISCARDING_THRESHOLD_LEVEL, Level.INFO.name())
                , Level.INFO
        );
        log.debug("Creating custom AsyncQueueFullPolicy(discardThreshold:{})", level);
        this.thresholdLevel = level;
    }

    @Override
    public EventRoute getRoute(long backgroundThreadId, Level level) {
        if (level.isLessSpecificThan(thresholdLevel)) {
            if (discardCount.getAndIncrement() == 0) {
                log.warn("Async queue is full, discarding event with level {}. " +
                                "This message will only appear once; future events from {} " +
                                "are silently discarded until queue capacity becomes available.",
                        level, thresholdLevel);
            }
            return EventRoute.DISCARD;
        }
        return EventRoute.SYNCHRONOUS;
    }
}
