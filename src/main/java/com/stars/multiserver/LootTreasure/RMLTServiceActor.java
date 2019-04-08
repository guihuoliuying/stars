package com.stars.multiserver.LootTreasure;

import com.stars.bootstrap.ServerManager;
import com.stars.modules.loottreasure.packet.AttendLootTreasure;
import com.stars.modules.loottreasure.packet.ServerRequestSwitchRoom;
import com.stars.modules.scene.packet.ServerExitFight;
import com.stars.multiserver.LootTreasure.event.AddFighterBackEvent;
import com.stars.multiserver.LootTreasure.event.CreateFightBackEvent;
import com.stars.multiserver.LootTreasure.event.FightFrameEvent;
import com.stars.multiserver.LootTreasure.event.LTOfflineEvent;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.server.main.actor.ActorServer;
import com.stars.services.ServiceSystem;
import com.stars.startup.LootTreasureStartup;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.HashSet;
import java.util.Set;

public class RMLTServiceActor extends ServiceActor implements RMLTService {

	private static HashSet<Integer> lootServers = new HashSet<>();
	private static int fightActorIdCounter = 0;
	
	public RMLTServiceActor(){
		
	}

	@Override
	public void init() throws Throwable {
		ServiceSystem.getOrAdd("rmltService", this);
	}

	@Override
	public void printState() {

	}

	public static synchronized String createFightId(){
		return new StringBuilder("loottreasure").append("-").append(MultiServerHelper.getServerId()).append("-").append(fightActorIdCounter++).toString();
	}

	@Override
	public void doCreateFightActorBack(int serverId, int actor, int room,
			String fightActor) {
		  Actor a = ActorServer.getActorSystem().getActor(actor);
		  a.tell(new CreateFightBackEvent(room, fightActor), Actor.noSender);
	}
	
	@Override
	public void addFighterToFightActorBack(int serverId, int actor, int room,
			Set<Long> fighters) {
		 Actor a = ActorServer.getActorSystem().getActor(actor);
		 a.tell(new AddFighterBackEvent(room, fighters), Actor.noSender);
	}
	
	@Override
	public void doFightFramData(int serverId, int actor, int room,
			LuaFrameData lData) {
		Actor a = ActorServer.getActorSystem().getActor(actor);
		a.tell(new FightFrameEvent(room, lData), Actor.noSender);
	}
	
	@Override
	public void doFightOffline(int serverId, int actor, int roomId, long roleId) {
		Actor a = ActorServer.getActorSystem().getActor(actor);
		a.tell(new LTOfflineEvent(roomId, roleId), Actor.noSender);
	}
	
	@Override
	public void doExistFight(int serverId, int actor, int roomId, long roleId) {
		Actor a = ActorServer.getActorSystem().getActor(actor);
		a.tell(new ServerExitFight(roleId), Actor.noSender);
	}
	
	@Override
	public void doSwitchRoom(int serverId,int actor, long roleId, byte roomType) {
		Actor a = ActorServer.getActorSystem().getActor(actor);
		ServerRequestSwitchRoom serverRequestSwitchRoom = new ServerRequestSwitchRoom();
		serverRequestSwitchRoom.setRoomType(roomType);
		serverRequestSwitchRoom.setRoleId(roleId);
		a.tell(serverRequestSwitchRoom, Actor.noSender);
	}
	
	public void attendLoottreasue(int serverId,AttendLootTreasure aLootTreasure) {
		Actor a = ActorServer.getActorSystem().getActor(aLootTreasure.getId());
		a.tell(aLootTreasure, Actor.noSender);
	}

	@Override
	public void registerLoottreasureServer(int serverId,int comFrom) {
		LogUtil.info("server:" + serverId+" , "+comFrom + "  register lootTreasure");
		if(!lootServers.contains(comFrom)){
			lootServers.add(comFrom);
		}
	}

	public static void syncRankListToAllServers(String lootSectionId, LTDamageRank ltDamageRank){
		for(Integer serverId : lootServers){
			RMLTRPCHelper.lootTreasureService().addClientLootTreasureRankList(serverId, lootSectionId, ltDamageRank);
		}
	}

	@Override
	public void reloadProduct(int serverId) {
		try {
			LootTreasureStartup.loadProduct();
		} catch (Exception e) {
			LogUtil.info("夺宝服重载产品数据失败,serverId={}", ServerManager.getServer().getConfig().getServerId());
			LogUtil.error("", e);
		}
	}
}
