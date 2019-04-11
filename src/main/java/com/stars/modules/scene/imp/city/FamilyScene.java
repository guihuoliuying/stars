package com.stars.modules.scene.imp.city;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.scene.ArroundScene;

import java.util.Map;

/**
 *
 * 家族领地场景
 * Created by zhouyaohui on 2016/10/8.
 */
public class FamilyScene extends ArroundScene {

    private String position;

    @Override
    public String getPosition() {
        return position;
    }

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
        return false;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {

    }

    @Override
    public void exit(Map<String, Module> moduleMap) {

    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public String getArroundId(Map<String, Module> moduleMap) {
        FamilyModule familyModule = (FamilyModule) moduleMap.get(MConst.Family);
        return String.valueOf(familyModule.getAuth().getFamilyId());
    }
}
