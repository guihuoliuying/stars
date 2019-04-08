package com.stars.multiserver.daily5v5;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.services.SConst;
import com.stars.services.ServiceManager;
import com.stars.services.fightServerManager.Conn2FightManagerServerCallBack;
import com.stars.services.fightServerManager.FSManagerService;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.services.fightServerManager.FSRPCNetExceptionTask;

public class Daily5v5ServiceManager extends ServiceManager{

	@Override
	public void initSelfServices() throws Throwable {
//		registerAndInit(SConst.Daily5v5Service, newService(new Daily5v5ServiceActor()));
		registerAndInit(SConst.Daily5v5MatchService, newService(new Daily5v5MatchServiceActor()));
		registerAndInit(SConst.FSManagerService, newService(new FSManagerServiceActor()));
	}

	@Override
	public void initRpc() throws Throwable {
//		exportService(Daily5v5Service.class, getService(SConst.Daily5v5Service));
		exportService(Daily5v5MatchService.class, getService(SConst.Daily5v5MatchService));
		exportService(FSManagerService.class, getService(SConst.FSManagerService));
		initRpcHelper(Daily5v5RpcHelper.class);
		int commonId = ServerManager.getServer().getConfig().getServerId();
        int managerServerId = Integer.parseInt(ServerManager.getServer().getConfig().getProps().
        		get(BootstrapConfig.FIGHTMANAGER).getProperty("serverId"));
        connectServer(BootstrapConfig.FIGHTMANAGER,
        		new Conn2FightManagerServerCallBack(Daily5v5RpcHelper.rmfsManagerService(), commonId, managerServerId),
        		new FSRPCNetExceptionTask(BootstrapConfig.FIGHTMANAGER, BootstrapConfig.FIGHTMANAGER1,
        				Daily5v5RpcHelper.rmfsManagerService()));
	}

	@Override
	public void runScheduledJob() throws Throwable {
		// TODO Auto-generated method stub
		
	}

}
