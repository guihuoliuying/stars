package com.stars.server.login2.asyncdb;

/**
 * Created by zhaowenshuo on 2016/2/19.
 */
public abstract class AsyncDbCallback {

    private long callbackId;
    private long timestamp;

    public long getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(long callbackId) {
        this.callbackId = callbackId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public abstract void onCalled(AsyncDbResult result);

}
