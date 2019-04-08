package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.dragonboat.DragonBoatModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;

import java.util.Map;

/**
 * Created by huwenjun on 2017/5/10.
 */
public class DragonBoatToolFunc extends ToolFunc {
    private int speed;

    public DragonBoatToolFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        /**
         * 配置格式为：27|x，
         */
        String str[] = function.split("\\|");
        speed = Integer.parseInt(str[1]);

    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
        ToolFuncResult tr;
        tr = super.useCondition(moduleMap, args);
        if (tr == null) {
            tr = new ToolFuncResult(true, null);
        }
        return tr;
    }

    @Override
    public Map<Integer, Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        Integer dragonBoatId = (Integer) args[0];
        DragonBoatModule module = (DragonBoatModule) moduleMap.get(MConst.DragonBoat);
        module.updateDragonBoatSpeed(dragonBoatId,speed);
        return null;
    }
}
