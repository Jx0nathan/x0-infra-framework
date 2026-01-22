package com.supercode.framework.log.exception;

import lombok.extern.log4j.Log4j2;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * @Author: jonathan.ji
 * @Date: 2022/1/23 20:50
 * @Desc:
 */
@Log4j2
public class GlobalUncaughtExceptionHandler implements UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        String message = sw.getBuffer().toString();
        log.warn("An exception has been raised by Name:{},ThreadId:{},Message:{}", t.getName(), t.getId(), message);
    }

}
