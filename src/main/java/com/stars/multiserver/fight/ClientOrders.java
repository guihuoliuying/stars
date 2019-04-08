package com.stars.multiserver.fight;

import com.google.common.primitives.Ints;

/**
 * Created by zhaowenshuo on 2016/12/1.
 */
public class ClientOrders {

    public static byte[] createReadyOrder() {
        return Ints.toByteArray(20210);
    }


}
