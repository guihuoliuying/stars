package com.stars.modules.fightingmaster.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.fightingmaster.FightingMasterModule;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by zhouyaohui on 2016/11/8.
 */
public class FightingMasterGM implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        FightingMasterModule fm = (FightingMasterModule) moduleMap.get(MConst.FightingMaster);
        fm.enterFightingMaster();
    }
}
