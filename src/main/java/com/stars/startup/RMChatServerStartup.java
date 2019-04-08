package com.stars.startup;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.stars.modules.daregod.DareGodModuleFactory;
import com.stars.modules.data.DataModuleFactory;
import com.stars.modules.drop.DropModuleFactory;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.chat.ChatRpcServiceManager;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.Business;
import com.stars.server.main.actor.ActorServer;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.core.actor.ActorSystem;


public class RMChatServerStartup implements Business {

    @Override
    public void init() throws Exception {
        try {
            DBUtil.init();
            MainStartup.initHotswapEnv();
            MultiServerHelper.loadPublicServerConfig();
            ActorServer.setActorSystem(new ActorSystem());
            PacketManager.loadCorePacket();
            SchedulerHelper.initAndStart();
            SchedulerManager.init();
            ServiceSystem.init();
            loadProductData();
            ServiceHelper.init(new ChatRpcServiceManager());
        } catch (Throwable e) {
            throw new Exception(e);
        }

    }

    private void loadProductData() throws Exception {
        new DataModuleFactory().loadProductData();
        new DropModuleFactory().loadProductData();
        new DareGodModuleFactory().loadProductData();
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispatch(Packet packet) {
        // TODO Auto-generated method stub

    }

}
