package com.stars.multiserver.fightManager;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

public interface RMFSManagerService extends Service, ActorService {
	
	@AsyncInvocation
	void registerFightServer(int serverId,int fServer,String fServerIp,int fServerPort,int load);
	
	@AsyncInvocation
	void registerCommonServer(int serverId,int commonServer);
}
