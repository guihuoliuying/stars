package com.stars.modules.trump;

import com.stars.modules.trump.prodata.TrumpKarmaVo;
import com.stars.modules.trump.prodata.TrumpLevelVo;
import com.stars.modules.trump.prodata.TrumpVo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhouyaohui on 2016/9/18.
 */
public class TrumpManager {

    /** 法宝产品数据 */
    public static ConcurrentMap<Integer, TrumpVo> trumpMap = new ConcurrentHashMap<>();
    public static ConcurrentMap<Integer, ConcurrentMap<Short, TrumpLevelVo>> levelMap = new ConcurrentHashMap<>();
    public static ConcurrentMap<Integer, Short> maxLevel = new ConcurrentHashMap<>();
    public static Map<Integer, TrumpKarmaVo>  trumpKarmaMap;

    /**
     * 获取法宝等级配置
     * @param trumpId
     * @param level
     * @return
     */
    public static TrumpLevelVo getTrumpLevelVo(int trumpId, short level) {
        ConcurrentMap<Short, TrumpLevelVo> map = levelMap.get(trumpId);
        if (map == null) {
            return null;
        }
        return map.get(level);
    }

    /**
     * 获取法宝产品数据
     * @param trumpId
     * @return
     */
    public static TrumpVo getTrumpVo(int trumpId) {
        return trumpMap.get(trumpId);
    }

    /**
     * 最小等级
     * @param trumpId
     * @return
     */
    public static TrumpLevelVo getMinTrumpLevelVo(int trumpId) {
        Map<Short, TrumpLevelVo> map = levelMap.get(trumpId);
        if (map == null) {
            return null;
        }
        TrumpLevelVo levelVo = null;
        for (TrumpLevelVo lv : map.values()) {
            if (levelVo != null && lv.getLevel() > levelVo.getLevel()) {
                continue;
            }
            levelVo = lv;
        }
        return levelVo;
    }
}
