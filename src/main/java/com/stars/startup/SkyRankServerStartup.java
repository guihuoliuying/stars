package com.stars.startup;

import com.stars.core.actor.ActorSystem;
import com.stars.core.module.ModuleManager;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataModuleFactory;
import com.stars.modules.skyrank.SkyRankModuleFactory;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.skyrank.SkyrankRpcServiceManager;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.Business;
import com.stars.server.main.actor.ActorServer;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.LogUtil;


public class SkyRankServerStartup implements Business {

	@Override
	public void init() throws Exception {
		try {
			MainStartup.initHotswapEnv();
			DBUtil.init();// 初始化数据库连接池(proxool)
			ActorServer.setActorSystem(new ActorSystem());
			PacketManager.loadCorePacket();
			SchedulerManager.init();
			ServiceSystem.init();
			ServiceHelper.init(new SkyrankRpcServiceManager());
			initModule();
			System.err.println("serverid="+MultiServerHelper.getServerId());
		} catch (Throwable e) {
			throw new Exception(e);
		}

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispatch(Packet packet) {
		// TODO Auto-generated method stub
		
	}
	
	private void initModule() throws Exception {
//		ModuleManager.register(Data, new DataModuleFactory()); //
//		ModuleManager.register(SkyRank, new SkyRankModuleFactory()); //
		ModuleManager.initDependence();
		ModuleManager.initPacket(); // 初始化数据包
		ModuleManager.init(); // 模块初始化
		ModuleManager.loadProductData();
		loadProduct();
		LogUtil.info("完成模块注册");
		
	}
	
	private void loadProduct() throws Exception{
		  new DataModuleFactory().loadProductData();
		  new SkyRankModuleFactory().loadProductData();
	}

}
