package com.stars.core.actor.invocation.exception;

/**
 * Created by zhaowenshuo on 2016/7/5.
 */
public class ServiceStopException extends RuntimeException {

    public ServiceStopException() {
    }

    public ServiceStopException(String message) {
        super(message);
    }

    public ServiceStopException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceStopException(Throwable cause) {
        super(cause);
    }
}
