package com.stars.services.fightServerManager;

import com.stars.bootstrap.ServerManager;
import com.stars.multiserver.fightManager.RMFSManagerService;
import com.stars.util.LogUtil;
import com.stars.core.rpc.RpcClient;

public class FSRPCNetExceptionTask implements Runnable {
	
	private String server1;//当前正在连的
	
	private String server2;//连接断开后需要连的
	
	private RMFSManagerService rmService;
	
	public FSRPCNetExceptionTask(String server1,String server2,RMFSManagerService rmService){
		this.server1 = server1;
		this.server2 = server2;
		this.rmService = rmService;
	}

	@Override
	public void run() {
		int commonId = ServerManager.getServer().getConfig().getServerId();
        int managerServerId = Integer.parseInt(ServerManager.getServer().getConfig().getProps().
        		get(server2).getProperty("serverId"));
        try {
        	new RpcClient(server2, new Conn2FightManagerServerCallBack(rmService, commonId, managerServerId),
    				new FSRPCNetExceptionTask(server2,server1,rmService)).connect();
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		
	}

}
