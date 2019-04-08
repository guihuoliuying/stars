package com.stars.network.server.session;

import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author xieyuejun
 *         <p/>
 *         会话管理
 */
public class SessionManager {

//	public static boolean debugLog = true;
//	public static boolean exceptionLog = true;
//    public static boolean sqlLog = true;

    private static ConcurrentMap<Long, com.stars.network.server.session.GameSession> sessionMap = new ConcurrentHashMap<>();//roleId-session
    
    private static ConcurrentSet<Channel>channelSet = new ConcurrentSet<Channel>();

    public static ConcurrentMap<Long, com.stars.network.server.session.GameSession> getSessionMap() {
        return sessionMap;
    }

    /**
     * 为key和session绑定关联，如果指定的key已有关联的session，返回旧的session， 这个调用是原子性的
     *
     * @param key
     * @param session
     * @return
     */
    public static com.stars.network.server.session.GameSession putIfAbsent(Long key, com.stars.network.server.session.GameSession session) {
        return sessionMap.putIfAbsent(key, session);
    }

    /**
     * 删除key和session的关联,当session不为指定的sesion是返回false，不删除，否则删除返回true
     *
     * @param key
     * @param session
     * @return
     */
    public static boolean remove(Long key, com.stars.network.server.session.GameSession session) {
        if (sessionMap.remove(key, session)) {
//            if (session.isActive()) {
//                session.getChannel().close();
//            }
            return true;
        }
        return false;
    }

    /**
     * 为key和session绑定关联
     *
     * @param key
     * @param session
     * @return
     */
    public static com.stars.network.server.session.GameSession put(Long key, com.stars.network.server.session.GameSession session) {
        return sessionMap.put(key, session);
    }

    /**
     * 删除指定key的session,如果session连接活跃则断开连接
     *
     * @param key
     * @return
     */
    public static com.stars.network.server.session.GameSession remove(Long key) {
        GameSession session = sessionMap.remove(key);
        // 因为Session间是共用通道
//        if (session != null) {
//            Channel channel = session.getChannel();
//            if (channel != null && channel.isActive()) {
//                channel.close();
//            }
//        }
        return session;
    }

    /**
     * 删除所有session，关闭所有连接
     */
    public static void removeAll() {
        // 因为Session间是共用通道
//        for (GameSession session : sessionMap.values()) {
//            Channel channel = session.getChannel();
//            if (channel != null && channel.isActive()) {
//                channel.close();
//            }
//        }
        sessionMap.clear();
    }

	public static ConcurrentSet<Channel> getChannelSet() {
		return channelSet;
	}

	public static void setChannelSet(ConcurrentSet<Channel> channelSet) {
		SessionManager.channelSet = channelSet;
	}

}
