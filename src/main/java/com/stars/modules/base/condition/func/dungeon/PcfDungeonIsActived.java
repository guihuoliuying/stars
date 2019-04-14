package com.stars.modules.base.condition.func.dungeon;

import com.google.common.base.Preconditions;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.base.condition.func.BaseExprFunc;
import com.stars.modules.dungeon.DungeonModule;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/16.
 */
public class PcfDungeonIsActived extends BaseExprFunc {
    @Override
    public Object eval(Map<String, Module> moduleMap, List<Object> paramList) {
        Preconditions.checkArgument(paramList.size() == 1);
        Preconditions.checkArgument(paramList.get(0) instanceof Integer);

        DungeonModule dungeonModule = module(moduleMap, MConst.Dungeon);
        return dungeonModule.isDungeonActive((Integer) paramList.get(0)) ? 1L : 0L;
    }
}
