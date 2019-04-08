package com.stars.util.actlock;

/**
 * Created by zhaowenshuo on 2017/5/10.
 */
public class ActSimpleLock {

    private boolean isLock = false;
    private long timeoutTimestamp = 0L;

    public void lock(long timeout) {
        isLock = true;
        long timestamp = System.currentTimeMillis() + timeout;
        if (timestamp > timeoutTimestamp) {
            timeoutTimestamp = timestamp;
        }
    }

    public void unlock() {
        isLock = false;
        timeoutTimestamp = 0L;
    }

    public boolean isLock() {
        return isLock && System.currentTimeMillis() < timeoutTimestamp;
    }

}
