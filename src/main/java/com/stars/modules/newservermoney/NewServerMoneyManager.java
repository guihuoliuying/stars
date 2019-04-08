package com.stars.modules.newservermoney;

import com.stars.modules.newservermoney.prodata.NewServerMoneyVo;

import java.util.Map;

/**
 * Created by liuyuheng on 2017/1/4.
 */
public class NewServerMoneyManager {
    // <operateId, <typeid, vo>>
    public static Map<Integer, Map<Integer, NewServerMoneyVo>> moneyVoMap;
    // 奖励开始/结束时间<rewardType, [starttime, endtime]>
    public static Map<Integer, long[]> execTimeMap;

    public static NewServerMoneyVo getMoneyVo(int operateId, int type) {
        if (!moneyVoMap.containsKey(operateId))
            return null;
        return moneyVoMap.get(operateId).get(type);
    }

    public static Map<Integer, NewServerMoneyVo> getMoneyVoMap(int operateId) {
        return moneyVoMap.get(operateId);
    }
}
