package com.stars.modules.family.submodules.entry;

import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/10/8.
 */
public interface FamilyActEntryFilter {

    int getMask(int activityId, Map<String, Module> moduleMap);

}
