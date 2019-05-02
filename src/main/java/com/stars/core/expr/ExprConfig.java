package com.stars.core.expr;

import com.stars.core.expr.node.dataset.ExprDataSet;
import com.stars.core.expr.node.func.ExprFunc;
import com.stars.core.expr.node.value.ExprValue;
import com.stars.core.expr.tips.ExprTips;
import com.stars.core.expr.tips.ExprTipsType;
import com.stars.util.LogUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.stars.core.expr.ExprTag.*;

public class ExprConfig {

    // 单值
    private Map<String, ExprValue> valueMap = new HashMap<>();
    // 集合
    private Map<String, Class<? extends ExprDataSet>> dataSetClassMap = new HashMap<>();
    private Map<String, Set<String>> dataSetFieldMap = new HashMap<>();
    // 函数
    private Map<String, ExprFunc> funcMap = new HashMap<>();
    // 提示
//    private Map<Integer, ExprTips> tipsMap = new HashMap<>();
    private Map<ExprTipsType, Map<String, Map<String, ExprTips>>> tipsMap = new HashMap<>();

    protected void registerValue(String name, Class<? extends ExprValue> clazz) {
        checkNotNull(name);
        checkNotNull(clazz);
        checkArgument(!valueMap.containsKey(name), "value name duplicate: " + name);
        try {
            valueMap.put(name, clazz.newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void registerDataSet(String name, Class<? extends ExprDataSet> clazz) {
        checkNotNull(name);
        checkNotNull(clazz);
        checkArgument(!dataSetClassMap.containsKey(name), "data set name duplicate: " + name);
        try {
            dataSetClassMap.put(name, clazz);
            dataSetFieldMap.put(name, clazz.newInstance().fieldSet());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void registerFunc(String name, Class<? extends ExprFunc> clazz) {
        checkNotNull(name);
        checkNotNull(clazz);
        checkArgument(!funcMap.containsKey(name), "function name duplicate: " + name);
        try {
            funcMap.put(name, clazz.newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void registerTips(ExprTipsType type, String name, String predicate, int tipsId, String tips) {
        checkArgument(tipsId > 0, "tipsId must be greater than 0: " + tipsId);
        checkArgument(!tipsMap.containsKey(tipsId), "tipsId duplicate: " + tipsId);
        checkNotNull(tips);
        Map<String, Map<String, ExprTips>> map0 = tipsMap.computeIfAbsent(type, t -> new HashMap<>());
        Map<String, ExprTips> map1 = map0.computeIfAbsent(name, n -> new HashMap<>());
        map1.put(predicate != null ? predicate : "", new ExprTips(tipsId, tips));
    }

    public ExprValue getValue(String name) {
        return valueMap.get(name);
    }

    public ExprDataSet newDataSet(String name, Object obj) {
        Class<? extends ExprDataSet> clazz = dataSetClassMap.get(name);
        if (clazz == null) {
            LogUtil.error("条件表达式|不存在集合:" + name);
        }
        try {
            return clazz.getConstructor(Map.class).newInstance(obj);
        } catch (Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public Set<String> getFieldSet(String name) {
        return dataSetFieldMap.getOrDefault(name, Collections.emptySet());
    }

    public ExprFunc getFunc(String name) {
        return funcMap.get(name);
    }

    public ExprTips getTips(ExprTipsType type, String name, String predicate) {
        predicate = predicate != null ? predicate : "";
        Map<String, ExprTips> map = tipsMap.get(type).get(name);
        if (!predicate.equals("") && !map.containsKey(predicate)) {
            predicate = "";
        }
        return tipsMap.get(type).get(name).get(predicate);
    }

    public String toString(int tag) {
        switch (tag) {
            case TAG_IDENTIFIER:
                return "标识符";
            case TAG_DIGITS:
                return "数值";
            case TAG_STRING:
                return "字符串";
            case TAG_RELATION_OP:
                return "关系运算符";
            case TAG_OR:
                return "或运算";
            case TAG_AND:
                return "与运算";
            case TAG_NOT:
                return "非运算";
            case TAG_IN:
                return "in";
            case TAG_BETWEEN:
                return "between";
            case TAG_EOF:
                return "eof";

            case TAG_PARENTHESIS_LEFT:
                return "(";
            case TAG_PARENTHESIS_RIGHT:
                return ")";
            case TAG_BRACKET_LEFT:
                return "[";
            case TAG_BRACKET_RIGHT:
                return "]";
            case TAG_BRACE_LEFT:
                return "{";
            case TAG_BRACE_RIGHT:
                return "}";
            case TAG_COMMA:
                return ",";
        }
        return "";
    }

}
