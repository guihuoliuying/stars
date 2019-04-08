package com.stars.util.callback;

import com.stars.util.log.CoreLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhaowenshuo on 2015/12/22.
 */
public class CallbackManager {

    private long timeout; // 超时时间
    private long interval; // 检查间隔

    private AtomicLong idGenerator = new AtomicLong(0);
    private ConcurrentMap<Long, CallbackRecord> recordMap = new ConcurrentHashMap<>();
    private Thread timeoutChecker = new CallbackTimeoutChecker();

    public CallbackManager(long timeout, long interval) {
        this.timeout = timeout;
        this.interval = interval;
        this.timeoutChecker.start();
    }

    public long submit(com.stars.util.callback.Callback callback) {
        CallbackRecord record = new CallbackRecord(nextId(), callback, now());
        recordMap.put(record.callbackId, record);
        return record.callbackId;
    }
    
    public long submit(long callbackId, com.stars.util.callback.Callback callback) {
    	CallbackRecord record = new CallbackRecord(callbackId, callback, now());
    	recordMap.put(record.callbackId, record);
    	return record.callbackId;
    }
    
    
    public com.stars.util.callback.Callback finish(long callbackId) {
        CallbackRecord record = recordMap.remove(callbackId);
        if (record != null) {
            return record.callback;
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
                for (CallbackRecord record : recordMap.values()) {
                    try {
                        if (now - record.timestamp > timeout && recordMap.remove(record.callbackId) != null) {
                            CoreLogger.trace("记录超时, callbackId={}, timestamp={}, now={}", record.callbackId, record.timestamp, now);
                            Callback callback = record.callback;
                            callback.onCalled(new CallbackContext(CallbackResult.TIMEOUT, null, null));
                        }
                    } catch (Exception e) {
                        // todo: to do something
                        CoreLogger.error("CallbackManager异常", e);
                    }
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(interval);
                } catch (Exception e) {

                }
            }
        }
    }

}

class CallbackRecord {
    final long callbackId;
    final com.stars.util.callback.Callback callback;
    final long timestamp;

    public CallbackRecord(long callbackId, com.stars.util.callback.Callback callback, long timestamp) {
        this.callbackId = callbackId;
        this.callback = callback;
        this.timestamp = timestamp;
    }
}
