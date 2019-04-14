package com.stars.core.expr.node.func.impl.math;

import com.google.common.base.Preconditions;
import com.stars.core.expr.node.func.ExprFunc;
import com.stars.core.module.Module;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcfAdd extends ExprFunc {
    @Override
    public Object eval(Map<String, Module> moduleMap, List<Object> paramList) {
        Preconditions.checkArgument(paramList.size() >= 2);
        long ret = 0L;
        for (int i = 0; i < paramList.size(); i++) {
            Preconditions.checkArgument(paramList.get(i) instanceof Long);
            ret += (long) paramList.get(i);
        }
        return ret;
    }
}
