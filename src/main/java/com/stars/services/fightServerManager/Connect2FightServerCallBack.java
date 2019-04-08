package com.stars.services.fightServerManager;

import com.stars.services.ServiceHelper;
import com.stars.core.rpc2.RpcClientConnectedCallback;

public class Connect2FightServerCallBack implements RpcClientConnectedCallback {

	private byte serverLevel;
	
	
	public Connect2FightServerCallBack(byte serverLevel){
		this.serverLevel = serverLevel;
	}
	
	@Override
	public void ontCalled(int serverId) {
		ServiceHelper.fsManagerService().setFightServerNetStatus(serverLevel, serverId, true);
	}

}
