package com.stars.core.expr.node.func.impl.open;

import com.stars.core.expr.node.func.ExprFunc;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.foreshow.ForeShowModule;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/4/1.
 */
public class PcfIsOpen extends ExprFunc {

    @Override
    public Object eval(Map<String, Module> moduleMap, List<Object> paramList) {
        if (paramList.size() < 1) {
            return (long) 0;
        }
        ForeShowModule module = module(moduleMap, MConst.ForeShow);
        for (Object param : paramList) {
            if (!module.isOpen((String) param)) {
                return (long) 0;
            }
        }
        return (long) 1;
    }
}
