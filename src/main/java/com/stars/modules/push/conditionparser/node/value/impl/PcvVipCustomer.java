package com.stars.modules.push.conditionparser.node.value.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.customerService.CustomerServiceModule;
import com.stars.modules.push.conditionparser.node.value.PushCondValue;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/21.
 */
public class PcvVipCustomer extends PushCondValue {
    @Override
    public Object eval(Map<String, Module> moduleMap) {
    	CustomerServiceModule module = (CustomerServiceModule)moduleMap.get(MConst.CustomerService);
    	byte state = module.getSavePlayInfoState();
        return (long)state;
    }

    @Override
    public String toString() {
        return "贵族信息";
    }
}
