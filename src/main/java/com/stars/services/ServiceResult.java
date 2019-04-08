package com.stars.services;

/**
 * Created by zhaowenshuo on 2016/7/16.
 */
public class ServiceResult {

    private boolean isSuccess;
    private Object value;
    private String message;

    public ServiceResult(Object value) {
        this.isSuccess = true;
        this.value = value;
    }

    public ServiceResult(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public ServiceResult(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
