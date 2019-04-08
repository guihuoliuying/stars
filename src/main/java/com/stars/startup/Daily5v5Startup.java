package com.stars.startup;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.stars.modules.daily5v5.Daily5v5ModuleFactory;
import com.stars.modules.data.DataModuleFactory;
import com.stars.modules.scene.SceneModuleFactory;
import com.stars.modules.skill.SkillModuleFactory;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.daily5v5.Daily5v5ServiceManager;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.Business;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;

public class Daily5v5Startup implements Business{

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
            ServiceHelper.init(new Daily5v5ServiceManager());
            PacketManager.loadCorePacket();
            SchedulerHelper.start();
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}
	
	private void loadProductData() throws Exception {
        new DataModuleFactory().loadProductData();
        new SkillModuleFactory().loadProductData();
		new SceneModuleFactory().loadProductData();
        new Daily5v5ModuleFactory().loadProductData();
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
