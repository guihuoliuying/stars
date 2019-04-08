package com.stars.server.login2.asyncdb;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/2/19.
 */
public class AsyncDbResult {

    private boolean isSuccess;
    private boolean isTimeout;
    private Throwable cause;

    private boolean hasResultSet = false;
    private int updatedCount;
    private Map<String, Object> resultSet;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public boolean isTimeout() {
        return isTimeout;
    }

    public void setTimeout(boolean timeout) {
        isTimeout = timeout;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public boolean hasResultSet() {
        return hasResultSet;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void setUpdatedCount(int updatedCount) {
        this.hasResultSet = false;
        this.updatedCount = updatedCount;
    }

    public Map<String, Object> getResultSet() {
        return resultSet;
    }

    public void setResultSet(Map<String, Object> resultSet) {
        this.hasResultSet = true;
        this.resultSet = resultSet;
    }
}
