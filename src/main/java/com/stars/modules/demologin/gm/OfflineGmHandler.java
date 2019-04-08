package com.stars.modules.demologin.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by wuyuxing on 2017/1/16.
 */
public class OfflineGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        LoginModule loginModule = (LoginModule) moduleMap.get(MConst.Login);
        loginModule.handleOfflineMsgFromGM();
    }
}
