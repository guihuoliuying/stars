package com.stars.modules.base.condition.func.open;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.base.condition.func.BaseExprFunc;
import com.stars.modules.foreshow.ForeShowModule;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/4/1.
 */
public class PcfIsOpen extends BaseExprFunc {

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
