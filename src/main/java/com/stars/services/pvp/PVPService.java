package com.stars.services.pvp;

import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFight;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.HashMap;
import java.util.Map;

public interface PVPService extends Service, ActorService {
	
	@AsyncInvocation
	void startPVP(long invitor,long invitee, byte[] data);
	
	@AsyncInvocation
	void socketDisconnect();

	@AsyncInvocation
	void startPvp(long inviterId, long inviteeId, ClientEnterFight enterPacket);

	// rpc interface
	@AsyncInvocation
	void onFightCreationSucceeded(int mainServerId, int fightServerId, String fightId, long inviterId, long inviteeId);

	@AsyncInvocation
	void finishPvp(int mainServerId, long inviterId, String fightId, long winnerId, long loserId);

	@AsyncInvocation
	void handleDamage(int mainServerId, long inviterId, Map<String, HashMap<String, Integer>> damageMap);

	@AsyncInvocation
	void handleDead(int mainServerId, long inviterId, Map<String, String> deadMap);
}
