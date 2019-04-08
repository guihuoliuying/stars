package com.stars.modules.soul.limit;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;

import java.util.Map;

/**
 * Created by huwenjun on 2017/11/16.
 */
public class FightLimit extends AbstractLimit {
    public FightLimit(int type, int value) {
        super(type, value);
    }

    @Override
    public boolean limit(Map<String, Module> moduleMap, boolean sendTips, int type) {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        int fightScore = roleModule.getFightScore();
        boolean isLimit = fightScore < getValue();
        if (isLimit && sendTips) {
            switch (type) {
                case AbstractLimit.TYPE_BREAK: {
                    roleModule.warn("soulgod_stage_lackfighting", getValue() + "");
                }
                break;
                case AbstractLimit.TYPE_UPGRADE: {
                    roleModule.warn("soulgod_levelup_lackfighting", getValue() + "");
                }
                break;
            }
        }
        return isLimit;
    }
}
