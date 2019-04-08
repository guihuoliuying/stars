package com.stars.modules.soul.limit;

import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/11/16.
 */
public class LimitFactory {
    public static Map<Integer, Class<? extends AbstractLimit>> limitMap = new HashMap<>();

    static {
        limitMap.put(1, LevelLimit.class);
        limitMap.put(2, FightLimit.class);
    }

    public static AbstractLimit getLimit(int type, int value) {
        Class<? extends AbstractLimit> limitClass = limitMap.get(type);
        if (limitClass == null) {
            throw new NullPointerException("元神系统：未注册的限制类型：" + type);
        } else {
            try {
                AbstractLimit abstractLimit = limitClass.getConstructor(int.class, int.class).newInstance(type, value);
                return abstractLimit;
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public static List<AbstractLimit> parseLimit(String limit) {
        List<AbstractLimit> limits = new ArrayList<>();
        Map<Integer, Integer> map = StringUtil.toMap(limit, Integer.class, Integer.class, '+', '&');
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            AbstractLimit abstractLimit = getLimit(entry.getKey(), entry.getValue());
            limits.add(abstractLimit);
        }
        return limits;
    }
}
