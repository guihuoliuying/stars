package com.stars.modules.activeweapon.handler;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.activeweapon.prodata.ActiveWeaponVo;
import com.stars.modules.dungeon.DungeonModule;

import java.util.Map;

/**
 * Created by huwenjun on 2017/6/15.
 */
public class ConditionHandler2 extends ConditionHandler {
    public ConditionHandler2(Map<String, Module> moduleMap, ActiveWeaponVo activeWeaponVo) {
        super(moduleMap, activeWeaponVo);
    }

    public ConditionHandler2() {
    }

    @Override
    public boolean check() {
        DungeonModule dungeonModule = (DungeonModule) moduleMap.get(MConst.Dungeon);
        String condition = activeWeaponVo.getCondition();
        int dungeonId = Integer.parseInt(condition);
        return dungeonModule.isPassDungeon(dungeonId);
    }

    @Override
    public Integer getType() {
        return 2;
    }
}
