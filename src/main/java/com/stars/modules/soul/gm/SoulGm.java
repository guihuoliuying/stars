package com.stars.modules.soul.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.soul.SoulModule;

import java.util.Map;

/**
 * Created by huwenjun on 2017/11/16.
 */
public class SoulGm implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        SoulModule soulModule = (SoulModule) moduleMap.get(MConst.Soul);
        String arg = args[0];
        String[] group = arg.split("=");
        String action = group[0];
        switch (action) {
            case "main": {
                soulModule.reqMainUI();
            }
            break;
            case "upgrade": {
                soulModule.reqUpgrade();
            }
            break;
            case "onekeyupgrade": {
                soulModule.reqOnekeyUpgrade();

            }
            break;
            case "break": {
                soulModule.reqBreak();

            }
            break;
        }
        soulModule.warn("GM执行成功");
    }
}
