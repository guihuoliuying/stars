package com.stars.server.connector;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zws on 2015/8/21.
 */
public class BackendConfig implements Iterable<BackendAddress> {

//    private final List<BackendAddress> list = new ArrayList<>(); // 游戏服配置列表
	
	private  Map<Integer, BackendAddress>configMap = new ConcurrentHashMap<Integer, BackendAddress>();

    public BackendAddress add(int serverId, String ip, int port) {
//        list.add(new BackendAddress(serverId, ip, port));
    	if (configMap.containsKey(serverId)) {
			return null;
		}
        return configMap.put(serverId, new BackendAddress(serverId, ip, port));
    }

    public void add(BackendAddress address) {
//        list.add(address);
    	configMap.put(address.getServerId(), address);
    }

    // fixme: add method: get(serverId), remove(serverId), clear()

    public int size() {
        return configMap.size();
    }

    public boolean contains(BackendAddress address) {
//        return list.contains(address);
    	return configMap.containsKey(address.getServerId());
    }

    @Override
    public Iterator<BackendAddress> iterator() {
//        return list.iterator();
    	return configMap.values().iterator();
    }
}
