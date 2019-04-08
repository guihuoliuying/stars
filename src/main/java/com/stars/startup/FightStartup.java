package com.stars.startup;

import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.stars.modules.arroundPlayer.Packet.ServerHeartbeat;
import com.stars.modules.camp.packet.ServerCampFightPacket;
import com.stars.modules.chat.packet.ServerChatMessage;
import com.stars.modules.daily5v5.packet.ServerDaily5v5Revive;
import com.stars.modules.daily5v5.packet.ServerDaily5v5UseBuff;
import com.stars.modules.data.DataModuleFactory;
import com.stars.modules.demologin.packet.ClientReconnect;
import com.stars.modules.demologin.packet.ServerReconnect;
import com.stars.modules.familyactivities.war.packet.ServerFamilyWarBattleFightDirect;
import com.stars.modules.familyactivities.war.packet.ServerFamilyWarBattleFightRevive;
import com.stars.modules.familyactivities.war.packet.ui.ServerFamilyWarSafeSceneEnter;
import com.stars.modules.fightingmaster.packet.ServerFightReady;
import com.stars.modules.friend.packet.ServerBlacker;
import com.stars.modules.loottreasure.packet.ServerRequestSwitchRoom;
import com.stars.modules.pk.PKModuleFacotry;
import com.stars.modules.pk.packet.ConnectRegisterToFightServer;
import com.stars.modules.pk.packet.RegistConnToFightServer;
import com.stars.modules.pk.packet.ServerPVPData;
import com.stars.modules.pk.packet.StartPVP1FightPacket;
import com.stars.modules.scene.SceneModuleFactory;
import com.stars.modules.scene.packet.ServerExitFight;
import com.stars.modules.scene.packet.ServerExitFightBack;
import com.stars.modules.skill.SkillModuleFactory;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.*;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.multiserver.fight.handler.FightHandlerFactory;
import com.stars.multiserver.fight.handler.impl.*;
import com.stars.multiserver.packet.CreateFightActorReq;
import com.stars.multiserver.packet.NewFighterToFightActor;
import com.stars.multiserver.packet.StopFightActor;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.network.server.session.SessionManager;
import com.stars.server.Business;
import com.stars.server.main.actor.ActorServer;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.fightbase.StatusSender;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;
import com.stars.core.actor.ActorSystem;

public class FightStartup implements Business {

    private StatusSender sender;

    @Override
    public void init() throws Exception {
        try {
            MainStartup.initHotswapEnv();
            DBUtil.init();
            ActorServer.setActorSystem(new ActorSystem());
            String os = System.getProperty("os.name");
            String path = System.getProperty("user.dir");
            if (os.startsWith("win") || os.startsWith("Win")) {
                System.load(path + "/config/luajava/Navigation.dll");
            } else {
                System.load(path + "/config/luajava/libnavigation.so");
            }
            MultiServerHelper.loadPublicServerConfig();
            PacketManager.loadCorePacket();
            registerPacket();
            ServiceSystem.init();
            SchedulerManager.init();
            ServiceHelper.init(new FightServiceManager());
            initFightHandler();
            loadProduct();
            initStatusSender();
            LuaScripts.loadAll();
            new FightFrameTimer().start();
        } catch (Throwable e) {
            throw new Exception(e);
        }
    }

    public static void loadProduct() throws Exception {
        new DataModuleFactory().loadProductData();
        new SkillModuleFactory().loadProductData();
        new SceneModuleFactory().loadProductData();
        new PKModuleFacotry().loadProductData();
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispatch(Packet packet) {
        if (packet instanceof ServerRequestSwitchRoom) {
            ServerRequestSwitchRoom serverRequestSwitchRoom = (ServerRequestSwitchRoom) packet;
            long roleId = serverRequestSwitchRoom.getRoleId();
            byte roomType = serverRequestSwitchRoom.getRoomType();
            String actorId = RoleId2ActorIdManager.getFightId(roleId);
            Actor actor = ActorServer.getActorSystem().getActor(actorId);
            if (actor != null && actor instanceof FightActor) {
                FightActor fightActor = (FightActor) actor;
                LoottreasureFightHandler fightHandler = (LoottreasureFightHandler) fightActor.getFightHandler();
                fightHandler.handleSwitchRoom(roleId, roomType);
            }
            return;
        }

        if (packet instanceof ServerReconnect) {
            ServerReconnect serverReconnect = (ServerReconnect) packet;
            long roleId = serverReconnect.getRoleId();
            String actorId = RoleId2ActorIdManager.getFightId(roleId);
            if (actorId == null) {
                PacketManager.send(serverReconnect.getSession(), new ClientReconnect(false));
                return;
            }
            Actor actor = ActorServer.getActorSystem().getActor(actorId);
            if (actor == null) {
                PacketManager.send(serverReconnect.getSession(), new ClientReconnect(false));
                return;
            }
            //重连包暂时不做验证
            GameSession gs = serverReconnect.getSession();
            gs.setRoleId(roleId);
            SessionManager.put(gs.getRoleId(), gs);
            PacketManager.send(serverReconnect.getSession(), new ClientReconnect(true));
            return;
        }

        if (packet instanceof ConnectRegisterToFightServer) {
            ConnectRegisterToFightServer trts = (ConnectRegisterToFightServer) packet;
            GameSession gs = packet.getSession();
            gs.setRoleId(trts.getFighter());
            SessionManager.put(gs.getRoleId(), gs);
            LogUtil.info(gs.getRoleId() + "到战斗服注册成功");
            //这里不要return了，因为fightActor还需要处理这个包
        }
        GameSession gs = packet.getSession();
        String fightId = RoleId2ActorIdManager.getFightId(gs.getRoleId());
        Actor a = null;
        if (fightId != null) {
            a = ActorServer.getActorSystem().getActor(RoleId2ActorIdManager.getFightId(gs.getRoleId()));
        }
        if (a == null && RoleId2ActorIdManager.get(gs.getRoleId()) != null) {
            a = ActorServer.getActorSystem().getActor(RoleId2ActorIdManager.get(gs.getRoleId()));
        }
        if (a == null) {
            LogUtil.info("战斗服 not find actor:" + gs.getActorId() + ",packet=0x" + Integer.toHexString(packet.getType()) + ",role=" + gs.getRoleId());
        } else {
            a.tell(packet, Actor.noSender);
        }

    }

    public void registerPacket() {
        try {
            PacketManager.register(ConnectRegisterToFightServer.class);
            PacketManager.register(StartPVP1FightPacket.class);
            PacketManager.register(ServerPVPData.class);
            PacketManager.register(RegistConnToFightServer.class);
            PacketManager.register(ServerHeartbeat.class);
            PacketManager.register(CreateFightActorReq.class);
            PacketManager.register(NewFighterToFightActor.class);
            PacketManager.register(StopFightActor.class);
            PacketManager.register(ServerExitFight.class);
            PacketManager.register(ServerExitFightBack.class);
            PacketManager.register(ServerRequestSwitchRoom.class); //野外夺宝PVP玩家手动请求切房间;
            PacketManager.register(ServerChatMessage.class);
            PacketManager.register(ServerFamilyWarBattleFightDirect.class);
            PacketManager.register(ServerFightReady.class);
            PacketManager.register(ServerReconnect.class);
            PacketManager.register(ServerFamilyWarBattleFightRevive.class);
            PacketManager.register(ServerBlacker.class);
            PacketManager.register(ServerFamilyWarSafeSceneEnter.class);//家族战请求切回备战场景
            PacketManager.register(ServerDaily5v5Revive.class);//日常5v5复活
            PacketManager.register(ServerDaily5v5UseBuff.class);//日常5v5 使用主动buff
            PacketManager.register(ServerCampFightPacket.class);//齐楚大作战
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            System.exit(0);
        }

    }

    public void initStatusSender() {
        sender = new StatusSender();
        sender.setRunState(true);
        sender.start();
    }

    public void initFightHandler() throws Throwable {
        FightHandlerFactory.register(FightConst.T_PVP, PvpFightHandler.class);
        FightHandlerFactory.register(FightConst.T_FIGHTING_MASTER, FightingMasterFightHandler.class);
        FightHandlerFactory.register(FightConst.T_LOOT_TREASURE, LoottreasureFightHandler.class);
        FightHandlerFactory.register(FightConst.T_TEAM_PVP_GAME_FIGHT, TPGFightHandler.class);
        FightHandlerFactory.register(FightConst.T_ESCORT_CARGO, EscortFightHandler.class);
        FightHandlerFactory.register(FightConst.T_FAMILY_WAR_ELITE_FIGHT, FamilyWarEliteFightHandler.class);
        FightHandlerFactory.register(FightConst.T_FAMILY_WAR_NORMAL_FIGHT, FamilyWarNormalFightHandler.class);
//        FightHandlerFactory.register(FightConst.T_FAMILY_WAR_STAGE_FIGHT, FamilyWarStageFightHandler.class);
        FightHandlerFactory.register(FightConst.T_FAMILY_ESCORT, FamilyEscortFightHandler.class);
        FightHandlerFactory.register(FightConst.T_DAILY_5V5, Daily5v5FightHandler.class);
        FightHandlerFactory.register(FightConst.T_CAMP_FIGHT, CampFightHandler.class);
    }

}
