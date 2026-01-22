package com.supercode.framework.log.async;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.async.AsyncLoggerDefaultExceptionHandler;
import org.apache.logging.log4j.core.async.RingBufferLogEvent;

/**
 * @Author: jonathan.ji
 * @Date: 2022/1/23 20:30
 * @Desc: 异步日志异常处理
 */
@Log4j2
public class AsyncLogExceptionHandler extends AsyncLoggerDefaultExceptionHandler {

    @Override
    public void handleEventException(Throwable throwable, long sequence, RingBufferLogEvent event) {
        StringBuilder eventStringBuilder = new StringBuilder();
        try {
            eventStringBuilder.append(event.toString());
        } catch (Exception e) {
            eventStringBuilder.append("ERROR calling toString() on ")
                    .append(event.getClass().getName())
                    .append(": ")
                    .append(e);
        }
        log.warn("AsyncLogger error handling event seq={}, value={}", sequence, eventStringBuilder.toString(), throwable);
    }
}
