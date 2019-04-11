package com.stars.multiserver.fight;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhaowenshuo on 2016/12/29.
 */
public class ServerOrders {

    private static AtomicInteger instanceIdCreator = new AtomicInteger(0);

    private static int nextInstanceId() {
        int id = instanceIdCreator.decrementAndGet();
        if (id >= 0) {
            synchronized (instanceIdCreator) {
                if (!instanceIdCreator.compareAndSet(id, -1)) {
                    return instanceIdCreator.decrementAndGet(); // 实际上不会这么快耗尽，所有不再做检查
                } else {
                    return -1;
                }
            }
        }
        return id;
    }

}
