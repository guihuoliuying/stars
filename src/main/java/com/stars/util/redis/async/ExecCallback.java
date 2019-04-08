package com.stars.util.redis.async;

import com.stars.util.ExecuteManager;
import com.stars.util.callback.Callback;
import com.stars.util.callback.CallbackContext;

/**
 * Created by zhaowenshuo on 2016/2/24.
 */
public class ExecCallback implements com.stars.util.callback.Callback {

    private com.stars.util.callback.Callback callback;

    public ExecCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onCalled(final CallbackContext ctx) {
        ExecuteManager.execute(new Runnable() {
            @Override
            public void run() {
                callback.onCalled(ctx);
            }
        });
    }

}
