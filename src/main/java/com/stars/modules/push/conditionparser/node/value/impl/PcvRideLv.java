package com.stars.modules.push.conditionparser.node.value.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.push.conditionparser.node.value.PushCondValue;
import com.stars.modules.ride.RideManager;
import com.stars.modules.ride.prodata.RideLevelVo;
import com.stars.modules.role.RoleModule;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/30.
 */
public class PcvRideLv extends PushCondValue {
    @Override
    public Object eval(Map<String, Module> moduleMap) {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        RideLevelVo vo = RideManager.getRideLvById(roleModule.getRideLevelId());
        return (long) vo.getLevel();
    }

    @Override
    public String toString() {
        return "坐骑等级";
    }
}
