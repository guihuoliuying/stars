package com.stars.modules.push.conditionparser.node.basic;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.node.PushCondNode;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcnDigits extends PushCondNode {

    private long digits;

    public PcnDigits(String str) {
        this.digits = Long.parseLong(str);
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        return digits;
    }

    @Override
    public String toString() {
        return Long.toString(digits);
    }
}
