package com.stars.server.connector.runnable.connection;

import com.stars.server.connector.BackendAddress;
import com.stars.server.connector.BackendSession;
import com.stars.server.connector.Connector;

/**
 * Created by zws on 2015/9/30.
 */
public class BackendChannelCloseTask implements Runnable {

    private com.stars.server.connector.BackendAddress address;

    public BackendChannelCloseTask(BackendAddress address) {
        this.address = address;
    }

    @Override
    public void run() {
        BackendSession backendSession = Connector.getBackendSession(address.serverId);
        if (backendSession != null && backendSession.channel() != null) {
            backendSession.channel().close();
        }
    }
}
