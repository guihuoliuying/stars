package com.stars.startup;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.schedule.SchedulerManager;
import com.stars.core.db.DBUtil;
import com.stars.modules.buddy.BuddyModuleFactory;
import com.stars.modules.camp.CampModuleFactory;
import com.stars.modules.data.DataModuleFactory;
import com.stars.modules.offlinepvp.OfflinePvpModuleFactory;
import com.stars.modules.pk.PKModuleFacotry;
import com.stars.modules.role.RoleModuleFactory;
import com.stars.modules.scene.SceneModuleFactory;
import com.stars.modules.skill.SkillModuleFactory;
import com.stars.modules.tool.ToolModuleFactory;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.camp.CampServiceManager;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.Business;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.LogUtil;

/**
 * Created by huwenjun on 2017/6/28.
 */
public class CampStartup implements Business {
    @Override
    public void init() throws Exception {
        try {
            MainStartup.initHotswapEnv();
            DBUtil.init();
            MultiServerHelper.loadPublicServerConfig();
            SchedulerHelper.init();
            SchedulerManager.init();
            loadProductData();
            ServiceSystem.init();
            ServiceHelper.init(new CampServiceManager());
            PacketManager.loadCorePacket();
            SchedulerHelper.start();
        } catch (Throwable throwable) {
            LogUtil.error(throwable.getMessage(), throwable);
            throw new RuntimeException();
        }

    }

    private void loadProductData() throws Exception {
        new DataModuleFactory().loadProductData();
        new RoleModuleFactory().loadProductData();
        new SkillModuleFactory().loadProductData();
        new SceneModuleFactory().loadProductData();
        new PKModuleFacotry().loadProductData();
        new ToolModuleFactory().loadProductData();
        new BuddyModuleFactory().loadProductData();
        new OfflinePvpModuleFactory().loadProductData();
        new CampModuleFactory().loadProductData();
    }

    @Override
    public void clear() {

    }

    @Override
    public void dispatch(Packet packet) {

    }
}
