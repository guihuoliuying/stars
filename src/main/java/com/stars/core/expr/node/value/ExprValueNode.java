package com.stars.core.expr.node.value;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.ExprContext;
import com.stars.core.expr.node.ExprNode;
import com.stars.util.LogUtil;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class ExprValueNode extends ExprNode {

    private String name;

    public ExprValueNode(ExprConfig config, String name) {
        super(config);
        this.name = name;
    }

    @Override
    public Object eval(Object obj, ExprContext ctx) {
        ExprValue pcv = config.getValue(name);
        if (pcv == null) {
            LogUtil.error("条件表达式|不存在单值:" + name);
        }
        return pcv.eval(obj);
    }

    public String toString() {
        ExprValue pcv = config.getValue(name);
        if (pcv == null) {
            LogUtil.error("条件表达式|不存在单值:" + name);
        }
        return pcv.toString();
    }

    @Override
    public String inorderString() {
        return String.format("(%s,%s)", "V", name);
    }
}
