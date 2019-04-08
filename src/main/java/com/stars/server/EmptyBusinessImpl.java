package com.stars.server;

import com.stars.network.server.packet.Packet;
import com.stars.util.log.CoreLogger;

/**
 * Created by zws on 2015/9/15.
 */
public class EmptyBusinessImpl implements Business {

    @Override
    public void init() throws Exception {
        com.stars.util.log.CoreLogger.info("空业务逻辑");
    }

    @Override
    public void clear() {
        com.stars.util.log.CoreLogger.info("空业务逻辑");
    }

    @Override
    public void dispatch(Packet packet) {
        CoreLogger.info("空业务逻辑");
    }
}
