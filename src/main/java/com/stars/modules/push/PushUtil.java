package com.stars.modules.push;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.node.PushCondNode;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/30.
 */
public class PushUtil {

    public static boolean isTrue(PushCondNode expr, Map<String, Module> moduleMap) {
        return (Long) expr.eval(moduleMap) != 0L;
    }

}
