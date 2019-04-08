package com.stars.core.actor.invocation.exception;

/**
 * Created by zhaowenshuo on 2016/6/27.
 */
public class InvocationException extends RuntimeException {

    public InvocationException() {
    }

    public InvocationException(String message) {
        super(message);
    }

    public InvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvocationException(Throwable cause) {
        super(cause);
    }
}
