package com.stars.multiserver.payServer;

import com.stars.services.SConst;
import com.stars.services.ServiceManager;

public class PayServerServieManager extends ServiceManager {

	@Override
	public void initSelfServices() throws Throwable {
		register(SConst.RMPayService, newService(new RMPayServiceActor()));
	}

	@Override
	public void initRpc() throws Throwable {
		exportService(RMPayServerService.class, getService(SConst.RMPayService));
		initRpcHelper(RMPayServerHelper.class);
	}

	@Override
	public void runScheduledJob() throws Throwable {
		// TODO Auto-generated method stub

	}

}
