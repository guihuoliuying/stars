package com.stars.modules.family.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/12.
 */
public class FamilySkillGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        if (args == null && args.length == 0) {
            throw new IllegalArgumentException("");
        }
        FamilyModule familyModule = (FamilyModule) moduleMap.get(MConst.Family);
        switch (args[0]) {
            case "u":
                familyModule.upgradeSkillLevel(args[1], Integer.parseInt(args[2]));
                break;
            case "uamap":
                familyModule.upgradeSkillLevelAmap();
                break;
        }
    }
}
