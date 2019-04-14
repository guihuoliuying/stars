package com.stars.services.pay;

import com.stars.bootstrap.ServerManager;
import com.stars.multiserver.MainRpcHelper;
import com.stars.core.rpc.RpcClientConnectedCallback;

public class Conn2PayServerCallBack implements RpcClientConnectedCallback {

	@Override
	public void ontCalled(int serverId) {
		MainRpcHelper.rmPayServerService().registerMainServer(serverId,
				ServerManager.getServer().getConfig().getServerId());
	}

}
