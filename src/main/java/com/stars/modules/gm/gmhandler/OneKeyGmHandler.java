package com.stars.modules.gm.gmhandler;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.gm.GmModule;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/4/11.
 */
public class OneKeyGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        GmModule gmModule = (GmModule) moduleMap.get(MConst.Gm);
        gmModule.exec("additem 30101=200");
        gmModule.exec("additem 30103=200");
        gmModule.exec("additem 30105=200");
        gmModule.exec("additem 30106=200");
        gmModule.exec("additem 30111=200");
        gmModule.exec("additem 30114=200");
        gmModule.exec("additem 30112=200");
        gmModule.exec("additem 30113=200");
        gmModule.exec("additem 30115=200");
        gmModule.exec("additem 80101=200");
        gmModule.exec("additem 80102=200");
        gmModule.exec("additem 80103=200");
        gmModule.exec("additem 80104=200");
        gmModule.exec("additem 80105=200");
        gmModule.exec("additem 80106=200");
        gmModule.exec("additem 80107=200");
        gmModule.exec("additem 80108=200");
        gmModule.exec("additem 80109=200");
        gmModule.exec("additem 80110=200");
        gmModule.exec("additem 80111=200");
        gmModule.exec("additem 80112=200");
        gmModule.exec("additem 80113=200");
        gmModule.exec("additem 80114=200");
        gmModule.exec("additem 80115=200");
        gmModule.exec("additem 30201=10000");
        gmModule.exec("addroleexp 10000000");
        gmModule.exec("payment 8");
        gmModule.exec("inductfinishall");
    }

}
