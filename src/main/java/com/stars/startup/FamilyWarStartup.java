package com.stars.startup;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.stars.modules.arroundPlayer.Packet.ServerHeartbeat;
import com.stars.modules.data.DataModuleFactory;
import com.stars.modules.drop.DropModuleFactory;
import com.stars.modules.familyactivities.war.FamilyActWarModuleFactory;
import com.stars.modules.pk.packet.ConnectRegisterToFightServer;
import com.stars.modules.rank.RankModuleFactory;
import com.stars.modules.scene.SceneModuleFactory;
import com.stars.modules.skill.SkillModuleFactory;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarServiceManager;
import com.stars.multiserver.fightingmaster.RoleId2ServerIdManager;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.network.server.session.SessionManager;
import com.stars.server.Business;
import com.stars.server.main.actor.ActorServer;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;

/**
 * Created by zhaowenshuo on 2016/11/15.
 */
public class FamilyWarStartup implements Business {

    @Override
    public void init() throws Exception {
        MainStartup.initHotswapEnv();
        try {
            DBUtil.init();
            MultiServerHelper.loadPublicServerConfig();
            SchedulerHelper.initAndStart();
            SchedulerManager.init();
            loadProductData();
            ServiceSystem.init();
            ServiceHelper.init(new FamilyWarServiceManager());
            PacketManager.loadCorePacket();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private void loadProductData() throws Exception {
        new DataModuleFactory().loadProductData();
        new FamilyActWarModuleFactory().loadProductData();
        new SkillModuleFactory().loadProductData();
        new SceneModuleFactory().loadProductData();
        new DropModuleFactory().loadProductData();
        new RankModuleFactory().loadProductData();
    }

    @Override
    public void clear() {

    }

    @Override
    public void dispatch(Packet packet) {
        try {
            if (packet instanceof ServerHeartbeat) {
                /** 过滤掉心跳 */
                return;
            }
            if (packet instanceof ConnectRegisterToFightServer) {
                /** 处理session */
                ConnectRegisterToFightServer trts = (ConnectRegisterToFightServer) packet;
                GameSession gs = packet.getSession();
                gs.setRoleId(trts.getFighter());
                gs.setServerId(RoleId2ServerIdManager.get(gs.getRoleId()));
                SessionManager.put(gs.getRoleId(), gs);
                Actor ltActor = ActorServer.getActorSystem().getActor(String.valueOf(gs.getActorId()));
                LogUtil.info("actorid = " + gs.getActorId() + " , " + String.valueOf(ltActor));
                ltActor.tell(packet, Actor.noSender);
                LogUtil.info(gs.getRoleId() + "家族战服注册成功");
                return;
            }
        } catch (Throwable e) {
            LogUtil.error("", e);
        }
    }
}
