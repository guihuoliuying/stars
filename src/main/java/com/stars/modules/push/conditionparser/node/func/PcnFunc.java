package com.stars.modules.push.conditionparser.node.func;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.PushCondGlobal;
import com.stars.modules.push.conditionparser.node.PushCondNode;
import com.stars.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcnFunc extends PushCondNode {

    private String name;
    private List<PushCondNode> paramList;

    public PcnFunc(String name, List<PushCondNode> paramList) {
        this.name = name;
        this.paramList = paramList;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        PushCondFunc func = PushCondGlobal.getFunc(name);
        if (func == null) {
            LogUtil.error("条件表达式|不存在函数:" + name);
        }
        List<Object> list = new ArrayList<>();
        for (PushCondNode param : paramList) {
            list.add(param.eval(moduleMap));
        }
        return func.eval(moduleMap, list);
    }
}
