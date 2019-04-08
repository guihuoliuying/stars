package com.stars.modules.push.conditionparser.node.value;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.PushCondGlobal;
import com.stars.modules.push.conditionparser.node.PushCondNode;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcnValue extends PushCondNode {

    private String name;

    public PcnValue(String name) {
        this.name = name;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        PushCondValue pcv = PushCondGlobal.getValue(name);
        if (pcv == null) {
            com.stars.util.LogUtil.error("条件表达式|不存在单值:" + name);
        }
        return pcv.eval(moduleMap);
    }

    public String toString() {
        PushCondValue pcv = PushCondGlobal.getValue(name);
        if (pcv == null) {
            LogUtil.error("条件表达式|不存在单值:" + name);
        }
        return pcv.toString();
    }
}
