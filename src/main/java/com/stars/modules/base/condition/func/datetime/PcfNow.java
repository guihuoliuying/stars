package com.stars.modules.base.condition.func.datetime;

import com.google.common.base.Preconditions;
import com.stars.core.module.Module;
import com.stars.modules.base.condition.func.BaseExprFunc;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcfNow extends BaseExprFunc {

    @Override
    public Object eval(Map<String, Module> moduleMap, List<Object> paramList) {
        Preconditions.checkArgument(paramList.size() == 0);
        return System.currentTimeMillis();
    }

}
