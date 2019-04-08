package com.stars.modules.familyactivities.expedition;

import com.stars.modules.familyactivities.expedition.prodata.FamilyActExpeditionBuffInfoVo;
import com.stars.modules.familyactivities.expedition.prodata.FamilyExpeditionVo;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/10/11.
 */
public class FamilyActExpeditionManager {

    public static final int STATE_NOT_STARTED = 0; // 未开始
    public static final int STATE_STARTED = 1; // 进行中
    public static final int STATE_PASSED = 2; // 通关
    public static final int STATE_END = 3; // 领奖完毕

    public static int maxId;
    public static Map<Integer, Map<Integer, FamilyExpeditionVo>> expeditionVoMap;
    public static Map<Integer, Integer> familyLevel2ExpeditionIdMap;
    public static Map<Integer, FamilyActExpeditionBuffInfoVo> buffInfoVoMap;
    public static Map<Integer, Integer> buffLevelMap;

    public static FamilyActExpeditionFlow flow;

}
