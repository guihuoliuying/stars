package com.stars.util.redis.async;

import com.stars.util.callback.Callback;
import com.stars.util.callback.CallbackContext;

/**
 * Created by zhaowenshuo on 2016/2/24.
 */
public class EmptyCallback implements Callback {

    @Override
    public void onCalled(CallbackContext ctx) {
        // do nothing
    }

}
