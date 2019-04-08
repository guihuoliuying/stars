package com.stars.modules.demologin.message;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2016/7/25.
 */
public class LoginSyncMsg extends BaseLoginMsg {

    private boolean isSucceeded;
    private CountDownLatch latch = new CountDownLatch(1);

    public void finish(boolean isSucceeded) {
        this.isSucceeded = isSucceeded;
        latch.countDown();
    }

    public boolean isSucceeded() {
        return isSucceeded;
    }

    public void await() throws InterruptedException {
        latch.await();
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return latch.await(timeout, unit);
    }
}
