package com.stars.modules.newfirstrecharge.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.newfirstrecharge.NewFirstRechargeModule;

import java.util.Map;

/**
 * Created by huwenjun on 2017/9/7.
 */
public class NewFirstRechargeGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        String actionStr = args[0];
        String[] actionGroup = actionStr.split("=");
        NewFirstRechargeModule newFirstRechargeModule = (NewFirstRechargeModule) moduleMap.get(MConst.NewFirstRechargeModule1);
        switch (actionGroup[0]) {
            case "main": {
                newFirstRechargeModule.reqMainUIData();
            }
            break;
            case "reward": {
                int group = Integer.parseInt(actionGroup[1]);
                newFirstRechargeModule.reqTakeReward(group);
            }
            break;
        }
    }
}
