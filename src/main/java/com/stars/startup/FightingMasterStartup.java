package com.stars.startup;

import com.google.common.base.Preconditions;
import com.stars.bootstrap.SchedulerHelper;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.stars.modules.arroundPlayer.Packet.ServerHeartbeat;
import com.stars.modules.buddy.BuddyModuleFactory;
import com.stars.modules.data.DataModuleFactory;
import com.stars.modules.drop.DropModuleFactory;
import com.stars.modules.fightingmaster.FightingMasterModuleFactory;
import com.stars.modules.fightingmaster.packet.ServerFightReady;
import com.stars.modules.fightingmaster.packet.ServerFightingMaseter;
import com.stars.modules.gm.packet.ServerGm;
import com.stars.modules.induct.packet.ServerInduct;
import com.stars.modules.offlinepvp.OfflinePvpModuleFactory;
import com.stars.modules.pk.PKModuleFacotry;
import com.stars.modules.pk.packet.ConnectRegisterToFightServer;
import com.stars.modules.role.RoleModuleFactory;
import com.stars.modules.scene.SceneModuleFactory;
import com.stars.modules.scene.packet.ServerEnterCity;
import com.stars.modules.skill.SkillModuleFactory;
import com.stars.modules.skyrank.packet.ServerSkyRankReq;
import com.stars.modules.tool.ToolModuleFactory;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fightingmaster.FightingMasterServiceManager;
import com.stars.multiserver.fightingmaster.RoleId2ServerIdManager;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.network.server.session.SessionManager;
import com.stars.server.Business;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.fightingmaster.FightingMasterService;
import com.stars.util.LogUtil;

/**
 * Created by zhouyaohui on 2016/11/1.
 */
public class FightingMasterStartup implements Business {

    @Override
    public void init() throws Exception {
        try {
            MainStartup.initHotswapEnv();
            DBUtil.init();// 初始化数据库连接池(proxool)
            MultiServerHelper.loadPublicServerConfig();
//            Preconditions.checkState(HotUpdateManager.init(LogManager.getLogger("console"), LogManager.getLogger("console")));
            SchedulerHelper.initAndStart("./config/jobs/fightingmaster/quartz.properties");
            SchedulerManager.init();
            loadProduct();
            ServiceSystem.init();
            ServiceHelper.init(new FightingMasterServiceManager());
            PacketManager.loadCorePacket();
            initPacket();
        } catch (Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public static void loadProduct() throws Exception {
        new DataModuleFactory().loadProductData();
        new RoleModuleFactory().loadProductData();
        new SkillModuleFactory().loadProductData();
        new SceneModuleFactory().loadProductData();
        new PKModuleFacotry().loadProductData();
        new ToolModuleFactory().loadProductData();
        new BuddyModuleFactory().loadProductData();
        new OfflinePvpModuleFactory().loadProductData();
        new FightingMasterModuleFactory().loadProductData();
        new DropModuleFactory().loadProductData();
    }

    private void initPacket() throws Exception {
        PacketManager.register(ConnectRegisterToFightServer.class);
        PacketManager.register(ServerHeartbeat.class);
        PacketManager.register(ServerGm.class);
        PacketManager.register(ServerFightingMaseter.class);
        PacketManager.register(ServerEnterCity.class);
        PacketManager.register(ServerFightReady.class);
        PacketManager.register(ServerInduct.class);
        PacketManager.register(ServerSkyRankReq.class);
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
                ConnectRegisterToFightServer trts = (ConnectRegisterToFightServer)packet;
                GameSession gs = packet.getSession();
                gs.setRoleId(trts.getFighter());
                gs.setServerId(RoleId2ServerIdManager.get(gs.getRoleId()));
                SessionManager.put(gs.getRoleId(), gs);
                return;
            }

            Preconditions.checkState(packet.getRoleId() != 0, "packet must contain roleid");
            FightingMasterService service = ServiceHelper.fightingMasterService();
            service.dispatch(packet);
        } catch (Throwable e) {
            LogUtil.error("", e);
        }
    }
}
