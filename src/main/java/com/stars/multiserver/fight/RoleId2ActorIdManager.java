package com.stars.multiserver.fight;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RoleId2ActorIdManager {
	
	public static ConcurrentHashMap<Long, Integer>map = new ConcurrentHashMap<Long, Integer>();

	private static ConcurrentMap<Long, String> roleId2FightIdMap = new ConcurrentHashMap<>();
	
	public static void put(long roleId,int actorId){
		map.put(roleId, actorId);
	}
	
	public static Integer get(long roleId){
		return map.get(roleId);
	}
	
	public static void remove(long roleId){
		map.remove(roleId);
	}

	// not that good
	public static void put(long roleId, String fightId){
		roleId2FightIdMap.put(roleId, fightId);
	}

	public static String getFightId(long roleId){
		return roleId2FightIdMap.get(roleId);
	}

	public static void removeRoleId(long roleId){
		roleId2FightIdMap.remove(roleId);
	}

}
