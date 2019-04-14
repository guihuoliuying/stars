package com.stars.core.expr.node.func;

import com.stars.core.expr.PushCondGlobal;
import com.stars.core.expr.node.ExprNode;
import com.stars.core.module.Module;
import com.stars.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprFuncNode extends ExprNode {

    private String name;
    private List<ExprNode> paramList;

    public ExprFuncNode(String name, List<ExprNode> paramList) {
        this.name = name;
        this.paramList = paramList;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        ExprFunc func = PushCondGlobal.getFunc(name);
        if (func == null) {
            LogUtil.error("条件表达式|不存在函数:" + name);
        }
        List<Object> list = new ArrayList<>();
        for (ExprNode param : paramList) {
            list.add(param.eval(moduleMap));
        }
        return func.eval(moduleMap, list);
    }
}
