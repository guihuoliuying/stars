package com.stars.modules.push.conditionparser.node;

import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/22.
 */
public abstract class PushCondNode {

    public abstract Object eval(Map<String, Module> moduleMap);

}
