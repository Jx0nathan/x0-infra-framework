package com.supercode.framework.exception;


/**
 * @author jonathan.ji
 */
@SuppressWarnings("unused")
public class RestClientException extends RuntimeException {

    public RestClientException() {
    }

    public RestClientException(String message) {
        super(message);
    }

    public RestClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestClientException(Throwable cause) {
        super(cause);
    }

    protected RestClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
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
