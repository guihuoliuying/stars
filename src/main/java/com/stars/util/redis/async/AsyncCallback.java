package com.stars.util.redis.async;

import com.stars.util.callback.Callback;
import com.stars.util.callback.CallbackContext;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by zhaowenshuo on 2015/12/22.
 */
public class AsyncCallback implements com.stars.util.callback.Callback {

    private ThreadPoolExecutor executor;
    private com.stars.util.callback.Callback callback;

    public AsyncCallback(ThreadPoolExecutor executor, Callback callback) {
        this.executor = executor;
        this.callback = callback;
    }

    @Override
    public void onCalled(final CallbackContext ctx) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                callback.onCalled(ctx);
            }
        });
    }
}
