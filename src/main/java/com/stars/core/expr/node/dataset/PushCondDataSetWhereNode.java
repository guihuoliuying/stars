package com.stars.core.expr.node.dataset;

import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public interface PushCondDataSetWhereNode {

    String getFieldName();

    boolean eval(PushCondData data, Map<String, Module> moduleMap);

}
