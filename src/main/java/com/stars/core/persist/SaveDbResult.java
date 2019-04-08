package com.stars.core.persist;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhaowenshuo on 2017/2/7.
 */
public class SaveDbResult {

    public final static int STATE_RUNNING = 0; // 在跑
    public final static int STATE_FINISHED = 1; // 完成
    public final static int STATE_CANCELLED = 2; // 取消

    private long roleId;
    private long timestamp;
    private AtomicInteger state;
    private AtomicInteger counter;

    public SaveDbResult(long roleId, long timestamp, AtomicInteger counter) {
        this.roleId = roleId;
        this.timestamp = timestamp;
        this.state = new AtomicInteger(STATE_RUNNING);
        this.counter = counter;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public AtomicInteger getCounter() {
        return counter;
    }

    public void setCounter(AtomicInteger counter) {
        this.counter = counter;
    }

    public boolean finish() {
        return state.compareAndSet(STATE_RUNNING, STATE_FINISHED);
    }

    public boolean isFinished() {
        return state.get() == STATE_FINISHED;
    }

    public boolean cancel() {
        return state.compareAndSet(STATE_RUNNING, STATE_CANCELLED);
    }

    public boolean isCancelled() {
        return state.get() == STATE_CANCELLED;
    }
}
