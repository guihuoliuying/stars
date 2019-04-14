package com.stars.core.expr.node.value.impl;

import com.stars.core.expr.node.value.ExprValue;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcvOfflineHours extends ExprValue {
    @Override
    public Object eval(Map<String, Module> moduleMap) {
        LoginModule loginModule = module(moduleMap, MConst.Login);
        long last = loginModule.getLastLastLoginTimestamp();
        long now = loginModule.getLastLoginTimestamp();
        if (last != 0) {
            return (now - last) / 3600000L;
        }
        return (long) 0;
    }

    @Override
    public String toString() {
        return "离线时间";
    }
}
