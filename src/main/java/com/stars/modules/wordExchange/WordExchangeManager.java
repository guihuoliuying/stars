package com.stars.modules.wordExchange;

import com.stars.modules.wordExchange.prodata.CollectAwardVo;
import com.stars.util.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/7.
 */
public class WordExchangeManager {
    public static Map<Integer, CollectAwardVo> COLLECT_AWARD_MAP;
    public static Map<Integer,List<CollectAwardVo>> ACTIVITY_COLLECT_LIST;

    public static void setCollectAwardMap(Map<Integer, CollectAwardVo> collectAwardMap) {
        COLLECT_AWARD_MAP = collectAwardMap;
    }

    public static void setActivityCollectList(Map<Integer, List<CollectAwardVo>> activityCollectList) {
        ACTIVITY_COLLECT_LIST = activityCollectList;
    }

    public static CollectAwardVo getCollectAwardVo(int id){
        if(StringUtil.isEmpty(COLLECT_AWARD_MAP)) return null;
        return COLLECT_AWARD_MAP.get(id);
    }

    public static List<CollectAwardVo> ActivityCollectList(int activityId){
        if(StringUtil.isEmpty(ACTIVITY_COLLECT_LIST)) return null;
        return ACTIVITY_COLLECT_LIST.get(activityId);
    }
}
