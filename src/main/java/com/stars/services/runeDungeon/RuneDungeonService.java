package com.stars.services.runeDungeon;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;

import java.util.List;
import java.util.Map;

public interface RuneDungeonService extends Service, ActorService{
	
	public List<Object> getOfflineAward(long roleId);
	
	public void sendHelpFightAward(long friendId, long beHelpId, Map<Integer, Integer> toolMap);
	
}
