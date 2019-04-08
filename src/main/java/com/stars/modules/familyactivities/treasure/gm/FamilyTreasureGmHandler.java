package com.stars.modules.familyactivities.treasure.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.familyactivities.treasure.FamilyTreasureModule;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-03-01 17:00
 */
public class FamilyTreasureGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        FamilyTreasureModule module = (FamilyTreasureModule) moduleMap.get(MConst.FamilyActTreasure);
        /*module.updateFamilyRank();
        module.updateRoleRank();*/
    }
}
