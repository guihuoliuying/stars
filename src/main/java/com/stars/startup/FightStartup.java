package com.stars.startup;

import com.stars.core.actor.Actor;
import com.stars.core.actor.ActorSystem;
import com.stars.core.db.DBUtil;
import com.stars.core.schedule.SchedulerManager;
import com.stars.modules.chat.packet.ServerChatMessage;
import com.stars.modules.data.DataModuleFactory;
import com.stars.modules.demologin.packet.ClientReconnect;
import com.stars.modules.demologin.packet.ServerReconnect;
import com.stars.modules.friend.packet.ServerBlacker;
import com.stars.modules.scene.SceneModuleFactory;
import com.stars.modules.scene.packet.ServerExitFight;
import com.stars.modules.scene.packet.ServerExitFightBack;
import com.stars.modules.skill.SkillModuleFactory;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.FightFrameTimer;
import com.stars.multiserver.fight.FightServiceManager;
import com.stars.multiserver.fight.LuaScripts;
import com.stars.multiserver.fight.RoleId2ActorIdManager;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.multiserver.fight.handler.FightHandlerFactory;
import com.stars.multiserver.fight.handler.impl.FightingMasterFightHandler;
import com.stars.multiserver.fight.handler.impl.TPGFightHandler;
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
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispatch(Packet packet) {

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
            PacketManager.register(CreateFightActorReq.class);
            PacketManager.register(NewFighterToFightActor.class);
            PacketManager.register(StopFightActor.class);
            PacketManager.register(ServerExitFight.class);
            PacketManager.register(ServerExitFightBack.class);
            PacketManager.register(ServerChatMessage.class);
            PacketManager.register(ServerReconnect.class);
            PacketManager.register(ServerBlacker.class);
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
        FightHandlerFactory.register(FightConst.T_FIGHTING_MASTER, FightingMasterFightHandler.class);
        FightHandlerFactory.register(FightConst.T_TEAM_PVP_GAME_FIGHT, TPGFightHandler.class);
    }

}
