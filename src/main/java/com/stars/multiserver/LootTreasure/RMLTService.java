package com.stars.multiserver.LootTreasure;

import com.stars.modules.loottreasure.packet.AttendLootTreasure;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.Set;

public interface RMLTService extends Service, ActorService {
	
	@AsyncInvocation
	void attendLoottreasue(int serverId,AttendLootTreasure aLootTreasure);
	
	@AsyncInvocation
	void addFighterToFightActorBack(int serverId,int actor,int room,Set<Long>fighters);
	
	@AsyncInvocation
	void doCreateFightActorBack(int serverId,int actor,int room ,String fightActor);
	
	@AsyncInvocation
	void doFightFramData(int serverId,int actor,int room,LuaFrameData lData);
	
	@AsyncInvocation
	void doExistFight(int serverId,int actor,int roomId,long roleId);
	
	@AsyncInvocation
	void doFightOffline(int serverId,int actor,int roomId,long roleId);
	
	@AsyncInvocation
	void doSwitchRoom(int serverId,int actor, long roleId, byte roomType);

	@AsyncInvocation
	void registerLoottreasureServer(int serverId,int comFrom);

	/**
	 * 重载产品数据
	 */
	@AsyncInvocation
	void reloadProduct(int serverId);
}
