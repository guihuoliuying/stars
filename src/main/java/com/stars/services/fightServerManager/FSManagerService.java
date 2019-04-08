package com.stars.services.fightServerManager;

import com.stars.multiserver.fightManager.FightServer;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.concurrent.ConcurrentHashMap;

public interface FSManagerService extends Service, ActorService {
	@AsyncInvocation
	void receiveFightServerMap(int serverId,ConcurrentHashMap<Integer, FightServer> fsMap);
	
	int getFightServer(byte level);
	
	@AsyncInvocation
	void setFightServerNetStatus(byte serverLevel,int serverId,boolean status);
}
