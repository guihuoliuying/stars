package com.stars.modules.callboss;

import com.stars.modules.callboss.prodata.CallBossVo;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/9/5.
 */
public class CallBossManager {
    // 可召唤时间
    public static String startTime;// 召唤开始时间
    public static String endTime;// 召唤结束时间

    // callbossvo <id,vo>
    public static Map<Integer, CallBossVo> callBossVoMap;

    public static CallBossVo getCallBossVo(int bossId) {
        return callBossVoMap.get(bossId);
    }
}
