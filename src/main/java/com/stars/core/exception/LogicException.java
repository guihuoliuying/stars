package com.stars.core.exception;

/**
 * Created by zhouyaohui on 2017/1/4.
 */
public class LogicException extends RuntimeException {
    private String[] params;

    public LogicException(String message, String[] params) {
        super(message);
        this.params = params;
    }

    public String[] getParams() {
        return params;
    }
}
