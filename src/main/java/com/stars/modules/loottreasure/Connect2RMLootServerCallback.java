package com.stars.modules.loottreasure;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.multiserver.MainRpcHelper;
import com.stars.core.rpc2.RpcClientConnectedCallback;

import java.util.Properties;

/**
 * Created by panzhenfeng on 2016/12/12.
 */
public class Connect2RMLootServerCallback implements RpcClientConnectedCallback {
    @Override
    public void ontCalled(int serverId) {
        BootstrapConfig config = ServerManager.getServer().getConfig();
        Properties props = config.getProps().get("loottreasure");
        MainRpcHelper.rmltService().registerLoottreasureServer(Integer.parseInt(props.getProperty("serverId")),
                ServerManager.getServer().getConfig().getServerId());

    }
}
