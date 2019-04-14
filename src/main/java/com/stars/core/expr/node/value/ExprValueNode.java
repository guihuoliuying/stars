package com.stars.core.expr.node.value;

import com.stars.core.expr.PushCondGlobal;
import com.stars.core.expr.node.ExprNode;
import com.stars.core.module.Module;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class ExprValueNode extends ExprNode {

    private String name;

    public ExprValueNode(String name) {
        this.name = name;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        ExprValue pcv = PushCondGlobal.getValue(name);
        if (pcv == null) {
            com.stars.util.LogUtil.error("条件表达式|不存在单值:" + name);
        }
        return pcv.eval(moduleMap);
    }

    public String toString() {
        ExprValue pcv = PushCondGlobal.getValue(name);
        if (pcv == null) {
            LogUtil.error("条件表达式|不存在单值:" + name);
        }
        return pcv.toString();
    }
}
