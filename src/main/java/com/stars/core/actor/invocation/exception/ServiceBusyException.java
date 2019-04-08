package com.stars.core.actor.invocation.exception;

/**
 * Created by zhaowenshuo on 2016/7/5.
 */
public class ServiceBusyException extends RuntimeException {

    public ServiceBusyException() {
    }

    public ServiceBusyException(String message) {
        super(message);
    }

    public ServiceBusyException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceBusyException(Throwable cause) {
        super(cause);
    }
}
