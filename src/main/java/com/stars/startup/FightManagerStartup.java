package com.stars.startup;

import com.stars.core.schedule.SchedulerManager;
import com.stars.multiserver.fightManager.RMFSManagerServiceManager;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.Business;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.LogUtil;

public class FightManagerStartup implements Business {
	
	@Override
	public void init() throws Exception {
		MainStartup.initHotswapEnv();
		ServiceSystem.init();
		try {
			SchedulerManager.init();
			ServiceHelper.init(new RMFSManagerServiceManager());
			PacketManager.loadCorePacket();
		} catch (Throwable cause) {
			LogUtil.error(cause.getMessage(),cause);
			System.exit(-1);
		}
        
	}

	@Override
	public void clear() {

	}

	@Override
	public void dispatch(Packet packet) {
		
	}

}
