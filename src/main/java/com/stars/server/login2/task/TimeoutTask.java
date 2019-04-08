package com.stars.server.login2.task;

import com.stars.server.login2.sdk.core.LSdkVerifyResult;
import com.stars.server.login2.sdk.core.LVerifyContext;

/**
 * Created by zhaowenshuo on 2016/2/2.
 */
public class TimeoutTask extends LoginTask {

    private LVerifyContext ctx;

    public TimeoutTask(LVerifyContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run0() {
        LSdkVerifyResult result = new LSdkVerifyResult(false, null, null, null);
        result.setTimeout(true);
        ctx.onResponse(result);
    }
}
