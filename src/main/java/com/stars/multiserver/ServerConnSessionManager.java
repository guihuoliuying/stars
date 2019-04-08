package com.stars.multiserver;

import com.stars.network.server.session.GameSession;
import com.stars.util.LogUtil;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServerConnSessionManager {
	
	public static int CONN_ID_PVP_M2F = -2;
	public static int CONN_ID_LOOT_M2L = -3;
	public static int CONN_ID_LOOT_L2F = -4;
	
	
	private static ConcurrentMap<Integer, GameSession> sessionMap = new ConcurrentHashMap<>();
	
	 /**
     * 删除key和session的关联,当session不为指定的sesion是返回false，不删除，否则删除返回true
     *
     * @param key
     * @param session
     * @return
     */
    public static boolean remove(int key, GameSession session) {
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
    public static GameSession put(int key, GameSession session) {
        return sessionMap.put(key, session);
    }
    
    
    public static GameSession get(int key){
    	return sessionMap.get(key);
    }

    /**
     * 删除指定key的session,如果session连接活跃则断开连接
     *
     * @param key
     * @return
     */
    public static GameSession remove(Integer key) {
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
    
    public static void print(){
    	Set<Entry<Integer, GameSession>>set = sessionMap.entrySet();
    	for (Entry<Integer, GameSession> entry : set) {
			LogUtil.info("session:"+entry.getKey()+","+(entry.getValue() == null));
		}
    }
}
