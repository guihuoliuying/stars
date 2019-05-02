package com.stars.core.expr.node.func;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.ExprContext;
import com.stars.core.expr.node.ExprNode;
import com.stars.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprFuncNode extends ExprNode {

    private String name;
    private List<ExprNode> paramList;

    public ExprFuncNode(ExprConfig config, String name, List<ExprNode> paramList) {
        super(config);
        this.name = name;
        this.paramList = paramList;
    }

    @Override
    public Object eval(Object obj, ExprContext ctx) {
        ExprFunc func = config.getFunc(name);
        if (func == null) {
            LogUtil.error("条件表达式|不存在函数:" + name);
        }
        List<Object> list = new ArrayList<>();
        for (ExprNode param : paramList) {
            list.add(param.eval(obj, null));
        }
        return func.eval(obj, list);
    }

    public String getName() {
        return name;
    }

    @Override
    public String inorderString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        paramList.forEach(node -> sb.append(node.inorderString()).append(","));
        sb.append("]");
        return String.format("(%s,%s,%s)", "F", name, sb.toString());
    }
}
