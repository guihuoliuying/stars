package com.stars.core.expr.node.func.impl.math;

import com.google.common.base.Preconditions;
import com.stars.core.expr.node.func.ExprFunc;
import com.stars.core.module.Module;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcfSub extends ExprFunc {
    @Override
    public Object eval(Map<String, Module> moduleMap, List<Object> paramList) {
        Preconditions.checkArgument(paramList.size() == 2);
        Preconditions.checkArgument(paramList.get(0) instanceof Long);
        Preconditions.checkArgument(paramList.get(1) instanceof Long);
        long l = (long) paramList.get(0);
        long r = (long) paramList.get(1);
        return l - r;
    }
}
