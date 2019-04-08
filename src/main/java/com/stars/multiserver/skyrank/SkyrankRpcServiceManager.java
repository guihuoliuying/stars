package com.stars.multiserver.skyrank;

import com.stars.services.SConst;
import com.stars.services.ServiceManager;
import com.stars.services.skyrank.SkyRankKFService;
import com.stars.services.skyrank.SkyRankKFServiceActor;
import com.stars.services.skyrank.SkyRankLocalService;
import com.stars.services.skyrank.SkyRankLocalServiceActor;

/**
 * 
 * 天梯服务
 * @author xieyuejun
 *
 */
public class SkyrankRpcServiceManager extends ServiceManager {
    @Override
    public void initSelfServices() throws Throwable {
        registerAndInit(SConst.SkyRankKFService,new SkyRankKFServiceActor());
        registerAndInit(SConst.SkyRankLocalService,new SkyRankLocalServiceActor());
    }

    @Override
    public void initRpc() throws Throwable {
        exportService(SkyRankKFService.class,getService(SConst.SkyRankKFService));
        exportService(SkyRankLocalService.class,getService(SConst.SkyRankLocalService));
        initRpcHelper(SkyrankHelper.class);
    }

    @Override
    public void runScheduledJob() throws Throwable {

    }

}
