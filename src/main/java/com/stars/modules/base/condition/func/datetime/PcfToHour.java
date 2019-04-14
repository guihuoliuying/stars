package com.stars.modules.base.condition.func.datetime;

import com.google.common.base.Preconditions;
import com.stars.core.module.Module;
import com.stars.modules.base.condition.func.BaseExprFunc;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcfToHour extends BaseExprFunc {
    @Override
    public Object eval(Map<String, Module> moduleMap, List<Object> paramList) {
        Preconditions.checkArgument(paramList.size() == 1);
        Preconditions.checkArgument(paramList.get(0) instanceof Long);
        long time = (long) paramList.get(0);
        return time / 3600000;
    }
}
