package com.stars.multiserver.fightManager;

import com.stars.services.SConst;
import com.stars.services.ServiceManager;

public class RMFSManagerServiceManager extends ServiceManager {

	@Override
	public void initSelfServices() throws Throwable {
		registerAndInit(SConst.RMFManagerService, newService(new RMFSManagerServiceActor()));
	}

	@Override
	public void initRpc() throws Throwable {
		exportService(RMFSManagerService.class,getService(SConst.RMFManagerService));
		initRpcHelper(RMFSManagerRPCHelper.class);
	}

	@Override
	public void runScheduledJob() throws Throwable {
		// TODO Auto-generated method stub

	}

}
