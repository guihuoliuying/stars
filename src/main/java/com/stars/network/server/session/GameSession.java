package com.stars.network.server.session;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author xieyuejun
 *         <p/>
 *         网络会话
 */
public class GameSession {

    private int connectionId;
    private Channel channel = null;
    private String account;
    private long roleId;
    private AtomicLong receivePacketCount = new AtomicLong(0L);
    private boolean isServerSession;
    private int serverId;
    private ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<>();
    private int actorId;

    public boolean isActive() {
    	if (channel == null) {
			return false;
		}
        return channel.isActive();
    }

    public boolean isServerSession() {
        return isServerSession;
    }

    public void setServerSession(boolean isPublicSession) {
        this.isServerSession = isPublicSession;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void writeAndFlush(Object msg) {
        this.channel.writeAndFlush(msg);
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void addReceivePacketCount() {
        receivePacketCount.incrementAndGet();
    }

    public Channel getChannel() {
        return channel;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void putAttribute(String key, Object val) {
        attributes.put(key, val);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }

	public int getActorId() {
		return actorId;
	}

	public void setActorId(int actorId) {
		this.actorId = actorId;
	}

}