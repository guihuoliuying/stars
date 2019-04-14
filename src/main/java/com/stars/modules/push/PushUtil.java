package com.stars.modules.push;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/30.
 */
public class PushUtil {

    public static boolean isTrue(ExprNode expr, Map<String, Module> moduleMap) {
        return (Long) expr.eval(moduleMap) != 0L;
    }

}
