package com.stars.multiserver.camp;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.multiserver.MultiServerHelper;
import com.stars.services.SConst;
import com.stars.services.ServiceManager;
import com.stars.services.fightServerManager.Conn2FightManagerServerCallBack;
import com.stars.services.fightServerManager.FSManagerService;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.services.fightServerManager.FSRPCNetExceptionTask;

/**
 * Created by huwenjun on 2017/6/28.
 */
public class CampServiceManager extends ServiceManager {
    @Override
    public void initSelfServices() throws Throwable {
        registerAndInit(SConst.campRemoteMainService, newService(new CampRemoteMainServiceActor()));
        registerAndInit(SConst.campRemoteFightService, newService(new CampRemoteFightServiceActor()));
        registerAndInit(SConst.FSManagerService, newService(new FSManagerServiceActor()));
    }

    @Override
    public void initRpc() throws Throwable {
        exportService(CampRemoteMainService.class, getService(SConst.campRemoteMainService));
        exportService(CampRemoteFightService.class, getService(SConst.campRemoteFightService));
        exportService(FSManagerService.class, getService(SConst.FSManagerService));
        initRpcHelper(CampRpcHelper.class);
        int commonServerId = MultiServerHelper.getServerId();
        int fsmsServerId = Integer.parseInt(ServerManager.getServer().getConfig().getProps().get(BootstrapConfig.FIGHTMANAGER).getProperty("serverId"));
        connectServer(BootstrapConfig.FIGHTMANAGER, new Conn2FightManagerServerCallBack(CampRpcHelper.rmfsManagerService(), commonServerId, fsmsServerId), new FSRPCNetExceptionTask(BootstrapConfig.FIGHTMANAGER, BootstrapConfig.FIGHTMANAGER1,
                CampRpcHelper.rmfsManagerService()));
    }

    @Override
    public void runScheduledJob() throws Throwable {
    }
}
