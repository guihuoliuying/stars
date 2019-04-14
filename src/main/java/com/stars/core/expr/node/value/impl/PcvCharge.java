package com.stars.core.expr.node.value.impl;

import com.stars.core.expr.node.value.ExprValue;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/21.
 */
public class PcvCharge extends ExprValue {
    @Override
    public Object eval(Map<String, Module> moduleMap) {
        try {
            LoginModule module = module(moduleMap, MConst.Login);
            return (long) module.getAccountRow().getChargeSum();
        } catch (Exception e) {
            LogUtil.error("", e);
            return 0L;
        }
    }

    @Override
    public String toString() {
        return "充值金额";
    }
}
