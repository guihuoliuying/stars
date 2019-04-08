package com.stars.server.login2.task;

import com.stars.util.LogUtil;

/**
 * Created by zhaowenshuo on 2016/3/14.
 */
public abstract class LoginTask implements Runnable {

    @Override
    public void run() {
        try {
            run0();
        } catch (Throwable cause) {
        	LogUtil.error("业务逻辑异常", cause);
        }
    }

    protected abstract void run0();
}
