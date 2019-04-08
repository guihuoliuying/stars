package com.stars.server.connector;

import io.netty.channel.Channel;

import java.util.Comparator;

/**
 * Created by zws on 2015/8/21.
 */
public class BackendSession {

    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;

    public static Comparator<BackendSession> comparator = new Comparator<BackendSession>() {
        @Override
        public int compare(BackendSession ch1, BackendSession ch2) {
            if (ch1.connectionCount() != ch2.connectionCount()) {
                return ch1.connectionCount() - ch2.connectionCount();
            } else {
                return ch1.serverId() - ch2.serverId();
            }
        }
    };

    private int serverId; // fixme: add field - addr: BackendAddress
    private Channel channel;
    private int state;
    private int connectionCount;
    private long heartbeatTimestamp;

    public BackendSession(int serverId, Channel channel) {
        this.serverId = serverId;
        this.channel = channel;
    }

    public int serverId() {
        return this.serverId;
    }

    public void channel(Channel channel) {
        this.channel = channel;
    }

    public Channel channel() {
        return channel;
    }

    public void state(int state) {
        switch (state) {
            case CONNECTED:case CONNECTING:
                this.state = state;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public int state() {
        return this.state;
    }

    public int addConnectionCount(int delta) {
        connectionCount = connectionCount + delta;
        return connectionCount;
    }

    public void connectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    public int connectionCount() {
        return connectionCount;
    }

    public void heartbeatTimestamp(long heartbeatTimestamp) {
        this.heartbeatTimestamp = heartbeatTimestamp;
    }

    public long heartbeatTimestamp() {
        return heartbeatTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BackendSession that = (BackendSession) o;

        return serverId == that.serverId;

    }

    @Override
    public int hashCode() {
        return serverId;
    }

}
