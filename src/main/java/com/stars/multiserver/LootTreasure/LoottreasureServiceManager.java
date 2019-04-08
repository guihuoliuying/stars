package com.stars.multiserver.LootTreasure;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.services.SConst;
import com.stars.services.ServiceManager;
import com.stars.services.fightServerManager.Conn2FightManagerServerCallBack;
import com.stars.services.fightServerManager.FSManagerService;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.services.fightServerManager.FSRPCNetExceptionTask;

import static com.stars.services.SConst.FSManagerService;

public class LoottreasureServiceManager extends ServiceManager {

	@Override
	public void initSelfServices() throws Throwable {
		registerAndInit(SConst.RMLoottreasureService, newService(new RMLTServiceActor()));
		registerAndInit(SConst.FSManagerService, newService(new FSManagerServiceActor()));
	}

	@Override
	public void initRpc() throws Throwable {
		exportService(RMLTService.class, getService(SConst.RMLoottreasureService));
		exportService(FSManagerService.class, getService(FSManagerService));
		initRpcHelper(RMLTRPCHelper.class);
		int commonId = ServerManager.getServer().getConfig().getServerId();
        int managerServerId = Integer.parseInt(ServerManager.getServer().getConfig().getProps().
        		get(BootstrapConfig.FIGHTMANAGER).getProperty("serverId"));
        connectServer(BootstrapConfig.FIGHTMANAGER,
        		new Conn2FightManagerServerCallBack(RMLTRPCHelper.rmfsManagerService(), commonId, managerServerId),
        		new FSRPCNetExceptionTask(BootstrapConfig.FIGHTMANAGER, BootstrapConfig.FIGHTMANAGER1,
        				RMLTRPCHelper.rmfsManagerService()));
	}

	@Override
	public void runScheduledJob() throws Throwable {

	}

}
