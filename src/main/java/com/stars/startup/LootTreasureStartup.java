package com.stars.startup;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.schedule.SchedulerManager;
import com.stars.core.db.DBUtil;
import com.stars.modules.arroundPlayer.Packet.ServerHeartbeat;
import com.stars.modules.data.DataManager;
import com.stars.modules.data.DataModuleFactory;
import com.stars.modules.drop.DropModuleFactory;
import com.stars.modules.loottreasure.LootTreasureManager;
import com.stars.modules.loottreasure.LootTreasureModuleFactory;
import com.stars.modules.loottreasure.packet.AttendLootTreasure;
import com.stars.modules.loottreasure.packet.ServerLootTreasureRankList;
import com.stars.modules.loottreasure.packet.ServerLootTreasureRankReq;
import com.stars.modules.loottreasure.packet.ServerRequestSwitchRoom;
import com.stars.modules.pk.packet.ConnectRegisterToFightServer;
import com.stars.modules.pk.packet.RegistConnToFightServer;
import com.stars.modules.pk.packet.ServerPVPData;
import com.stars.modules.rank.RankModuleFactory;
import com.stars.modules.role.RoleModuleFactory;
import com.stars.modules.scene.SceneModuleFactory;
import com.stars.modules.scene.packet.ServerExitFight;
import com.stars.modules.scene.packet.ServerExitFightBack;
import com.stars.modules.scene.packet.ServerRoleRevive;
import com.stars.modules.scene.packet.fightSync.ServerFightDamage;
import com.stars.modules.skill.SkillModuleFactory;
import com.stars.multiserver.LootTreasure.LootTreasureFlow;
import com.stars.multiserver.LootTreasure.LoottreasureServiceManager;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.RoleId2ActorIdManager;
import com.stars.multiserver.packet.CreateFightActorBack;
import com.stars.multiserver.packet.LuaFrameDataBack;
import com.stars.multiserver.packet.NewFighterToFightActorBack;
import com.stars.multiserver.packet.OfflineNotice;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.network.server.session.SessionManager;
import com.stars.server.Business;
import com.stars.server.main.actor.ActorServer;
import com.stars.server.main.message.Disconnected;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;
import com.stars.core.actor.ActorSystem;

public class LootTreasureStartup implements Business {
	public LootTreasureFlow lFlow;
	@Override
	public void init() throws Exception {
		try {
			MainStartup.initHotswapEnv();
			ActorServer.setActorSystem(new ActorSystem());
			PacketManager.loadCorePacket();
			registerPacket();
//			MainStartup.initHotswapEnv();
	        DBUtil.init();
	        MultiServerHelper.loadPublicServerConfig();
			loadProduct();
			ServiceSystem.init();
			SchedulerManager.init();
	        ServiceHelper.init(new LoottreasureServiceManager());
			SchedulerHelper.initAndStart();
	        lFlow = new LootTreasureFlow();
	        lFlow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(2));
//	        lFlow.onTriggered(1, false);
		} catch (Throwable cause) {
			LogUtil.error(cause.getMessage(),cause);
			System.exit(-1);
		}

	}

	public static void loadProduct() throws Exception {
		//需要加载夺宝的产品数据
		new DataModuleFactory().loadProductData();
		new RoleModuleFactory().loadProductData();
		new DropModuleFactory().loadProductData();
		new LootTreasureModuleFactory().loadProductData();
		new SkillModuleFactory().loadProductData();
		new SceneModuleFactory().loadProductData();
		new RankModuleFactory().loadProductData();
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispatch(Packet packet) {
		if (packet instanceof ConnectRegisterToFightServer) {
			ConnectRegisterToFightServer trts = (ConnectRegisterToFightServer)packet;
			GameSession gs = packet.getSession();
			gs.setRoleId(trts.getFighter());
			gs.setActorId(RoleId2ActorIdManager.get(gs.getRoleId()));
			SessionManager.put(gs.getRoleId(), gs);
			Actor ltActor = ActorServer.getActorSystem().getActor(String.valueOf(gs.getActorId()));
			LogUtil.info("actorid = "+gs.getActorId()+" , "+ String.valueOf(ltActor));
			ltActor.tell(packet, Actor.noSender);
			LootTreasureManager.log(gs.getRoleId() + "野外夺宝服注册成功");
			return;
		}
		
		if(packet instanceof ServerFightDamage){
			//接收到客户端上传的伤害包;
			int actorId = RoleId2ActorIdManager.get(packet.getSession().getRoleId());
			Actor actor = ActorServer.getActorSystem().getActor(actorId);
			if(actor != null){
				actor.tell(packet, Actor.noSender);
			}else{
				LogUtil.error("野外夺宝活动,接收到了客户端上传的伤害包,但是没有对应的actor可以接收="+packet.getRoleId());
			}
			return;
		}
		
		
		if (packet instanceof ServerPVPData) {
			return;
		}
		if(packet instanceof ServerRoleRevive){
			ServerRoleRevive serverRoleRevive = (ServerRoleRevive)packet;
			long roleId = serverRoleRevive.getRoleId();
			int actorId = RoleId2ActorIdManager.get(roleId);
			Actor actor = ActorServer.getActorSystem().getActor(actorId);
			if(actor != null){
				actor.tell(packet, Actor.noSender);
			}
			return;
		}
		if (packet instanceof ServerLootTreasureRankReq){ //需要返回ClientLootTreasuerRankBack到主服;
//			ClientLootTreasureRankBack clientLootTreasureRankBack = new ClientLootTreasureRankBack();
//			Map<String, LTActor> ltActorMap = lFlow.getLtActorMap();
//			AbstractLootTreasure lootTreasure = null;
//			for(Map.Entry<String, LTActor> kvp : ltActorMap.entrySet()){
//				lootTreasure = kvp.getValue().getLootTreasure();
//				clientLootTreasureRankBack.addListRankVoList(kvp.getValue().getId(), lootTreasure.ltDamageRank.getFirstList(LootTreasureConstant.RANK_POLLFROM_SERVER_COUNT));
//			}
//			ServerLootTreasureRankReq serverLootTreasureRankReq = (ServerLootTreasureRankReq)packet;
//			PacketManager.send(serverLootTreasureRankReq.getSession(), clientLootTreasureRankBack);
			return;
		}
		if (packet instanceof ServerHeartbeat) {
			return;
		}
		if (packet instanceof ServerExitFightBack){
			//注意,这里不需要做什么处理,和掉线的处理一致就行;
			ServerExitFightBack serverExitFightBack = (ServerExitFightBack)packet;
			//发送到原actor中进行处理;
			String key = serverExitFightBack.getKey();
			String actorid = key.split("\\|")[0];
			Actor actor = ActorServer.getActorSystem().getActor(actorid);
			actor.tell(serverExitFightBack, Actor.noSender);
			return;
		}
		if(packet instanceof  ServerExitFight){
			int actorId = RoleId2ActorIdManager.get(packet.getRoleId());
			Actor actor = ActorServer.getActorSystem().getActor(actorId);
			actor.tell(packet, Actor.noSender);
			return;
		}
		if (packet instanceof ServerRequestSwitchRoom){
			int actorId = RoleId2ActorIdManager.get(packet.getRoleId());
			Actor actor = ActorServer.getActorSystem().getActor(actorId);
			actor.tell(packet, Actor.noSender);
			return;
		}
		GameSession gs = packet.getSession();
		Actor a = ActorServer.getActorSystem().getActor(String.valueOf(gs.getActorId()));
		if(a != null){
			a.tell(packet, Actor.noSender);
		}

	}
	
	public void registerPacket(){
		try {
			PacketManager.register(ConnectRegisterToFightServer.class);
			PacketManager.register(RegistConnToFightServer.class);
			PacketManager.register(ServerHeartbeat.class);
			PacketManager.register(AttendLootTreasure.class);
			PacketManager.register(ServerFightDamage.class);
			PacketManager.register(CreateFightActorBack.class);
			PacketManager.register(NewFighterToFightActorBack.class);
			PacketManager.register(LuaFrameDataBack.class);
			PacketManager.register(OfflineNotice.class);
			PacketManager.register(Disconnected.class);
			PacketManager.register(ServerPVPData.class);
			PacketManager.register(ServerLootTreasureRankList.class);
			PacketManager.register(ServerRoleRevive.class);
			PacketManager.register(ServerLootTreasureRankReq.class);
			PacketManager.register(ServerExitFightBack.class);
			PacketManager.register(ServerExitFight.class);
			PacketManager.register(ServerRequestSwitchRoom.class);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			System.exit(0);
		}
		
	}

}
