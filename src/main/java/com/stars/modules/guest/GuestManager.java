package com.stars.modules.guest;

import com.stars.modules.guest.prodata.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhouyaohui on 2017/1/3.
 */
public class GuestManager {

    // 每日免费刷新次数
    public static int FLUSH_FREE_COUNT = 0; // 免费刷新次数，暂时不用
    public static int REFRESH_TIMES = 0;      // 每日次数重置
    public static Map<Integer, Integer> RMB_REFRESH_COST;
    public static int HELP_LIMIT_TIME;  // 求助有效时间

    private static ConcurrentMap<Integer, GuestInfoVo> infoMap = new ConcurrentHashMap<>();
    private static ConcurrentMap<Integer, ConcurrentMap<Integer, GuestStageVo>> stageMap = new ConcurrentHashMap<>();
    private static ConcurrentMap<Integer, GuestFeelingVo> feelingMap = new ConcurrentHashMap<>();
    private static ConcurrentMap<Integer, GuestMissionVo> missionMap = new ConcurrentHashMap<>();
    private static ConcurrentMap<Byte, ConcurrentMap<Integer, GuestMissionVo>> qualityMap = new ConcurrentHashMap<>();
    private static ConcurrentMap<String, List<GuestRefreshVo>> refreshMap = new ConcurrentHashMap<>();

    public static void setInfoMap(ConcurrentMap<Integer, GuestInfoVo> map) {
        infoMap = map;
    }

    public static void setStageMap(ConcurrentMap<Integer, ConcurrentMap<Integer, GuestStageVo>> map) {
        stageMap = map;
    }

    public static void setFeelingMap(ConcurrentMap<Integer, GuestFeelingVo> map) {
        feelingMap = map;
    }

    public static void setMissionMap(ConcurrentMap<Integer, GuestMissionVo> map) {
        missionMap = map;
    }

    public static void setQualityMap(ConcurrentMap<Byte, ConcurrentMap<Integer, GuestMissionVo>> map) {
        qualityMap = map;
    }

    public static void setRefreshMap(ConcurrentMap<String, List<GuestRefreshVo>> map) {
        refreshMap = map;
    }

    /** 获取info产品数据 */
    public static GuestInfoVo getInfoVo(int guestId) {
        return infoMap.get(guestId);
    }

    /** 获取所有门客id */
    public static Set<Integer> getAllGuestId() {
        return infoMap.keySet();
    }

    /** 获取门客指定星级stage产品数据 */
    public static GuestStageVo getStageVo(int guestId, int level) {
        ConcurrentMap<Integer, GuestStageVo> map = getStageMap(guestId);
        if (map == null) return null;
        return map.get(level);
    }

    /** 获取门客的stage产品数据 */
    public static ConcurrentMap<Integer, GuestStageVo> getStageMap(int guestId) {
        return stageMap.get(guestId);
    }

    /** 获取最小的星级产品数据 */
    public static GuestStageVo getMinStageVo(int guestId) {
        ConcurrentMap<Integer, GuestStageVo> map = getStageMap(guestId);
        if (map == null) return null;
        int min = Integer.MAX_VALUE;
        for (int level : map.keySet()) {
            if (level < min) {
                min = level;
            }
        }
        return map.get(min);
    }

    /** 获取门客下一星级产品数据 */
    public static GuestStageVo getNextStageVo(int guestId, int cur) {
        Map<Integer, GuestStageVo> map = stageMap.get(guestId);
        if (map == null) return null;
        return map.get(cur + 1);
    }

    /** 获取门客情缘 */
    public static Map<Integer, GuestFeelingVo> getFeelingByGuest(int guestId) {
        Map<Integer, GuestFeelingVo> feeling = new HashMap<>();
        for (GuestFeelingVo feelingVo : feelingMap.values()) {
            if (feelingVo.contains(guestId)) {
                feeling.put(feelingVo.getGuestFeelId(), feelingVo);
            }
        }
        return feeling;
    }

    /** 获取门客情缘 */
    public static GuestFeelingVo getFeelingById(int feelingId) {
        return feelingMap.get(feelingId);
    }

    /** 获取刷新产品数据 */
    public static List<GuestRefreshVo> getRefreshList(int guestCount) {
        for (Map.Entry<String, List<GuestRefreshVo>> entry : refreshMap.entrySet()) {
            String[] counts = entry.getKey().split("[+]");
            if (Integer.valueOf(counts[0]) <= guestCount &&
                    guestCount <= Integer.valueOf(counts[1])) {
                return entry.getValue();
            }
        }
        return null;
    }

    /** 根据品质获取任务 */
    public static Map<Integer, GuestMissionVo> getMissionByQuality(byte quality) {
        return qualityMap.get(quality);
    }

    /** 根据任务id获取任务 */
    public static GuestMissionVo getMissionById(int missionId) {
        return missionMap.get(missionId);
    }
}
