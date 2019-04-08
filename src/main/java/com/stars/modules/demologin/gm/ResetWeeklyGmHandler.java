package com.stars.modules.demologin.gm;

import com.stars.core.module.Module;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/7/27.
 */
public class ResetWeeklyGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        LoginModuleHelper.resetWeekly(false);
    }
}
