package com.stars.multiserver.fight;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.services.SConst;
import com.stars.services.ServiceManager;
import com.stars.services.fightbase.FightBaseService;
import com.stars.services.fightbase.FightBaseServiceActor;

/**
 * Created by zhouyaohui on 2016/11/8.
 */
public class FightServiceManager extends ServiceManager {

    @Override
    public void initSelfServices() throws Throwable {
        registerAndInit(SConst.FightBaseService, newDispatchService(FightBaseServiceActor.class, 16));
    }

    @Override
    public void initRpc() throws Throwable {
        exportService(FightBaseService.class, getService(SConst.FightBaseService));
        initRpcHelper(FightRPC.class);
        connectServer(BootstrapConfig.FIGHTMANAGER);//战斗管理服
        connectServer(BootstrapConfig.FIGHTMANAGER1);//战斗管理服备
    }

    @Override
    public void runScheduledJob() throws Throwable {

    }
}
