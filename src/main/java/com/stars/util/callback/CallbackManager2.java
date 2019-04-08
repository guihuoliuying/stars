package com.stars.util.callback;

import com.stars.util.log.CoreLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhaowenshuo on 2016/4/2.
 */
public class CallbackManager2 {

    private AtomicLong idGenerator = new AtomicLong(0);
    private ConcurrentMap<Long, CallbackRecord2> recordMap = new ConcurrentHashMap<>();
    private Thread timeoutChecker = new CallbackTimeoutChecker();

    public long submit(com.stars.util.callback.Callback2 callback) {
        return submit(callback, 1);
    }

    public long submit(com.stars.util.callback.Callback2 callback, int count) {
        CallbackRecord2 record = new CallbackRecord2(nextId(), callback, now(), count);
        recordMap.put(record.callbackId, record);
        return record.callbackId;
    }

    public com.stars.util.callback.Callback2 finish(long callbackId) {
        CallbackRecord2 record = recordMap.get(callbackId);
        if (record != null && record.count.decrementAndGet() == 0) {
            record = recordMap.remove(callbackId);
            if (record != null) {
                return record.callback;
            }
        }
        return null;
    }

    public com.stars.util.callback.Callback2 finish(long callbackId, Runnable runnable) {
        CallbackRecord2 record = recordMap.get(callbackId);
        if (record != null) {
            synchronized (record) {
                if (recordMap.containsKey(callbackId)) {
                    runnable.run();
                }
            }
            return finish(callbackId);
        }
        return null;
    }

    public Callback2 fail(long callbackId) {
        CallbackRecord2 record = recordMap.remove(callbackId);
        if (record != null) {
            synchronized (record) {
                record = recordMap.remove(callbackId);
                if (record != null) {
                    return record.callback;
                }
            }
        }
        return null;
    }

    private long nextId() {
        return idGenerator.getAndIncrement();
    }

    private long now() {
        return System.currentTimeMillis();
    }

    class CallbackTimeoutChecker extends Thread {
        @Override
        public void run() {
            while (true) {
                long now = now();
                for (CallbackRecord2 record : recordMap.values()) {
                    try {
                        if (now - record.timestamp > record.getTimeout() && recordMap.remove(record.callbackId) != null) {
                            CoreLogger.trace("记录超时, callbackId={}, timestamp={}, now={}", record.callbackId, record.timestamp, now);
                            synchronized (record) {
                                record.callback.onTimeout();
                            }
                        }
                    } catch (Exception e) {
                        // todo: to do something
                        CoreLogger.error("CallbackManager异常", e);
                    }
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (Exception e) {

                }
            }
        }
    }

}

class CallbackRecord2 {
    final long callbackId;
    final com.stars.util.callback.Callback2 callback;
    final long timestamp;
    final AtomicInteger count;

    public CallbackRecord2(long callbackId, com.stars.util.callback.Callback2 callback, long timestamp) {
        this(callbackId, callback, timestamp, 1);
    }

    public CallbackRecord2(long callbackId, com.stars.util.callback.Callback2 callback, long timestamp, int count) {
        this.callbackId = callbackId;
        this.callback = callback;
        this.timestamp = timestamp;
        this.count = new AtomicInteger(count);
    }

    public long getTimeout() {
        return callback.getTimeout();
    }
}
