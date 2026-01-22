package com.supercode.framework.exception;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jonathan.ji
 */
@SuppressWarnings("unused")
public class JacksonException extends RuntimeException {

    /**
     * 参数占位符
     */
    private static final String PLACEHOLDER = "{}";

    public JacksonException() {
        super();
    }

    public JacksonException(String message) {
        super(message);
    }

    public JacksonException(Throwable cause) {
        super(cause);
    }

    public JacksonException(String format, Object... arguments) {
        super(format(format, arguments));
    }

    /**
     * 格式化参数
     */
    private static String format(String format, Object... arguments) {
        if (StringUtils.isNotBlank(format) && ArrayUtils.isNotEmpty(arguments)) {
            for (Object argument : arguments) {
                format = StringUtils.replace(format, PLACEHOLDER, argument + "");
            }
        }
        return format;
    }

    /**
     * for batter performance
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public Throwable doFillInStackTrace() {
        return fillInStackTrace();
    }
}
