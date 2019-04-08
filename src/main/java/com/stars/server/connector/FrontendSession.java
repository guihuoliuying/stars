package com.stars.server.connector;

import com.stars.server.connector.stat.ConnectorStat;
import io.netty.channel.Channel;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zws on 2015/8/18.
 */
public class FrontendSession {

    public static final int BINDING = 1; // 绑定游戏服连接的状态
    public static final int UNBINDING = 2; // 没有绑定游戏服连接的状态

    private int connectionId; // 客户端连接的ID
    private int state; // 会话的状态
    private Channel frontendChannel; // 客户端连接的Channel
    private int backendChannelIndex; // 游戏服连接的下标
    private ConnectorStat stat;
    private String account; // 账号名
    private Set<Integer> boundServerIdSet = new HashSet<>();

    /* 统计信息 */
    private long connectTimestamp;

    public FrontendSession(int connectionId) {
        this.connectionId = connectionId;
        this.backendChannelIndex = -1;
    }

    public int connectionId() {
        return connectionId;
    }

    public void state(int state) {
        switch (state) {
            case BINDING: case UNBINDING:
                this.state = state;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public int state() {
        return this.state;
    }

    public Channel frontendChannel() {
        return frontendChannel;
    }

    public void frontendChannel(Channel frontendChannel) {
        this.frontendChannel = frontendChannel;
    }

    public int backendChannelIndex() {
        return backendChannelIndex;
    }

    public void backendChannelIndex(int backendChannelIndex) {
        this.backendChannelIndex = backendChannelIndex;
        if (backendChannelIndex > 0) {
            boundServerIdSet.add(backendChannelIndex);
        }
    }

    public void connectTimestamp(long timestamp) {
        this.connectTimestamp = timestamp;
    }

    public long connectTimestamp() {
        return this.connectTimestamp;
    }

    public void stat(ConnectorStat stat) {
        this.stat = stat;
    }

    public ConnectorStat stat() {
        return this.stat;
    }

    public void account(String account) {
        this.account = account;
    }

    public String account() {
        return account;
    }

    public Set<Integer> getBoundServerIdSet() {
        return boundServerIdSet;
    }
}
