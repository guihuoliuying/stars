package com.stars.services.fightServerManager;


import com.stars.multiserver.fightManager.RMFSManagerService;
import com.stars.core.rpc2.RpcClientConnectedCallback;

public class Conn2FightManagerServerCallBack implements RpcClientConnectedCallback {

	private int commonId;
	
	private int managerServerId;
	
	private RMFSManagerService rmfsManagerService;
	
	
	
	public Conn2FightManagerServerCallBack(RMFSManagerService rmfsManagerService,int commonId,int managerServerId){
		this.rmfsManagerService = rmfsManagerService;
		this.commonId = commonId;
		this.managerServerId = managerServerId;
	}
	
	@Override
	public void ontCalled(int serverId) {
		rmfsManagerService.registerCommonServer(managerServerId, commonId);
	}

}
