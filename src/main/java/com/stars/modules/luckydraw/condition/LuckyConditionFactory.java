package com.stars.modules.luckydraw.condition;

import com.stars.modules.luckydraw.condition.imp.LuckyCondition0;
import com.stars.modules.luckydraw.condition.imp.LuckyCondition1;
import com.stars.modules.luckydraw.condition.imp.LuckyCondition2;
import com.stars.modules.luckydraw.condition.imp.LuckyCondition3;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyConditionFactory {
    public static Map<Integer, Class<? extends LuckyCondition>> luckyConditionMap = new HashMap<>();

    static {
        luckyConditionMap.put(0, LuckyCondition0.class);
        luckyConditionMap.put(1, LuckyCondition1.class);
        luckyConditionMap.put(2, LuckyCondition2.class);
        luckyConditionMap.put(3, LuckyCondition3.class);
    }

    public static LuckyCondition newInstance(int conditionType, int time) {
        Class<? extends LuckyCondition> luckyConditionClazz = luckyConditionMap.get(conditionType);
        if (luckyConditionClazz == null) {
            throw new RuntimeException("没有对应的幸运条件，找策划确认");
        }
        try {
            LuckyCondition luckyCondition = luckyConditionClazz.getConstructor(int.class).newInstance(time);
            return luckyCondition;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<LuckyCondition> parseLuckyConditionList(String condition) {
        Map<Integer, Integer> conditionTimeMap = StringUtil.toMap(condition, Integer.class, Integer.class, '+', '|');
        List<LuckyCondition> luckyConditions = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : conditionTimeMap.entrySet()) {
            LuckyCondition luckyCondition = newInstance(entry.getKey(), entry.getValue());
            luckyConditions.add(luckyCondition);
        }
        return luckyConditions;
    }
}
