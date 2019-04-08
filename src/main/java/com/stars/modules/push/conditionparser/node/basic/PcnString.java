package com.stars.modules.push.conditionparser.node.basic;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.node.PushCondNode;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcnString extends PushCondNode {

    private String str;

    public PcnString(String str) {
        this.str = str;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        return str;
    }
}
