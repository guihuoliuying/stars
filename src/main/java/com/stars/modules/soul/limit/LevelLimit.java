package com.stars.modules.soul.limit;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;

import java.util.Map;

/**
 * Created by huwenjun on 2017/11/16.
 */
public class LevelLimit extends AbstractLimit {
    public LevelLimit(int type, int value) {
        super(type, value);
    }

    @Override
    public boolean limit(Map<String, Module> moduleMap, boolean sendTips, int type) {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        boolean isLimit = roleModule.getLevel() < getValue();
        if (isLimit && sendTips) {
            switch (type) {
                case TYPE_BREAK: {
                    roleModule.warn("soulgod_stage_lacklevel", getValue() + "");
                }
                break;
                case TYPE_UPGRADE: {
                    roleModule.warn("soulgod_levelup_lacklevel", getValue() + "");
                }
                break;
            }
        }
        return isLimit;
    }
}
