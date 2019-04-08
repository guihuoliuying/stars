package com.stars.server.login2.asyncdb;

import com.stars.util.ExecuteManager;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhaowenshuo on 2016/2/19.
 */
public class AsyncDbManager {

    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private static ConcurrentMap<Long, AsyncDbCallback> callbacks = new ConcurrentHashMap<>();
    private static AtomicLong idCreator = new AtomicLong(0);

    public static void init() throws Exception {
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                /* 超时检查 */
                long now = System.currentTimeMillis();
                for (AsyncDbCallback callback : callbacks.values()) {
                    if (callback.getTimestamp() - now > 30000 && callbacks.remove(callback.getCallbackId()) != null) {
                        com.stars.server.login2.asyncdb.AsyncDbResult result = new com.stars.server.login2.asyncdb.AsyncDbResult();
                        result.setTimeout(true);
                        finish(callback.getCallbackId(), result);
                    }
                }
            }}, 30, 30, TimeUnit.SECONDS);
    }

    public static void exec(int dbId, String sql, AsyncDbCallback callback) {
        long callbackId = idCreator.getAndIncrement(); // 注意：可能为负数；但在有生之年都不会出现（除非被攻击）
        callback.setCallbackId(callbackId);
        callback.setTimestamp(System.currentTimeMillis());
        callbacks.put(callbackId, callback);
        service.execute(new AsyncDbTask(dbId, sql, callbackId));
    }

    static void finish(long callbackId, final AsyncDbResult resultSet) {
        final AsyncDbCallback callback = callbacks.remove(callbackId);
        if (callback != null) {
            ExecuteManager.execute(new Runnable() {
                @Override
                public void run() {
                    callback.onCalled(resultSet);
                }
            });
        } else {
            // must be timeout
        }
    }

}
