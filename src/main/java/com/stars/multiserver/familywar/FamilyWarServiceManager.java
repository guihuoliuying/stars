package com.stars.multiserver.familywar;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.services.SConst;
import com.stars.services.ServiceManager;
import com.stars.services.fightServerManager.Conn2FightManagerServerCallBack;
import com.stars.services.fightServerManager.FSManagerService;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.services.fightServerManager.FSRPCNetExceptionTask;

/**
 * Created by chenkeyu on 2017-04-27 15:55
 */
public class FamilyWarServiceManager extends ServiceManager {
    @Override
    public void initSelfServices() throws Throwable {
        registerAndInit(SConst.FSManagerService, newService(new FSManagerServiceActor()));
        registerAndInit(SConst.FamilyWarQualifyingService, newService(new FamilyWarQualifyingServiceActor()));
        registerAndInit(SConst.FamilyWarRemoteService, newService(new FamilyWarRemoteServiceActor()));
        registerAndInit(SConst.FamilyWarRankService, newService(new FamilywarRankServiceActor()));
    }

    @Override
    public void initRpc() throws Throwable {
        exportService(FSManagerService.class, getService(SConst.FSManagerService));
        exportService(FamilyWarQualifyingService.class, getService(SConst.FamilyWarQualifyingService));
        exportService(FamilyWarRemoteService.class, getService(SConst.FamilyWarRemoteService));
        exportService(FamilywarRankService.class, getService(SConst.FamilyWarRankService));
        initRpcHelper(FamilyWarRpcHelper.class);
        int commonId = ServerManager.getServer().getConfig().getServerId();
        int managerServerId = Integer.parseInt(ServerManager.getServer().getConfig().getProps().
                get(BootstrapConfig.FIGHTMANAGER).getProperty("serverId"));
        connectServer(BootstrapConfig.FIGHTMANAGER,
                new Conn2FightManagerServerCallBack(FamilyWarRpcHelper.rmfsManagerService(), commonId, managerServerId),
                new FSRPCNetExceptionTask(BootstrapConfig.FIGHTMANAGER, BootstrapConfig.FIGHTMANAGER1,
                        FamilyWarRpcHelper.rmfsManagerService()));
    }

    @Override
    public void runScheduledJob() throws Throwable {

    }
}
