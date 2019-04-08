package com.stars.modules.family.submodules.entry.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.family.submodules.entry.FamilyActEntryFilter;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.services.family.FamilyConst;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/5/12.
 */
public class FamilyEscortEntryFilter implements FamilyActEntryFilter {
    @Override
    public int getMask(int activityId, Map<String, Module> moduleMap) {
        ForeShowModule module = (ForeShowModule) moduleMap.get(MConst.ForeShow);
        return module.isOpen(ForeShowConst.FAMILY_ESCORT) ? FamilyConst.ACT_BTN_MASK_ALL : FamilyConst.ACT_BTN_MASK_NONE;
    }
}
