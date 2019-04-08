package com.stars.modules.opactsecondskill;

import com.stars.modules.opactsecondskill.prodata.SecKillVo;

import java.util.List;
import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/7/27.
 */
public class OpActSecondKillManager {
    private static Map<Integer, SecKillVo> SEC_KILL_MAP;
    private static Map<Integer, List<SecKillVo>> SEC_KILL_MAP_BY_GROUP;
    private static Map<Integer, SecKillVo> CHECK_GROUP_SEC_KILL_MAP;
    private static boolean IS_DAILY_RESET;

    public static boolean isOpActSecKillOpen = false;
    public static long stopTimeStamp;

    public static Map<Integer, SecKillVo> getSecKillMap() {
        return SEC_KILL_MAP;
    }

    public static SecKillVo getSecKillVoById(int id) {
        return SEC_KILL_MAP.get(id);
    }

    public static void setSecKillMap(Map<Integer, SecKillVo> secKillMap) {
        SEC_KILL_MAP = secKillMap;
    }

    public static Map<Integer, List<SecKillVo>> getSecKillMapByGroup() {
        return SEC_KILL_MAP_BY_GROUP;
    }

    public static void setSecKillMapByGroup(Map<Integer, List<SecKillVo>> secKillMapByGroup) {
        SEC_KILL_MAP_BY_GROUP = secKillMapByGroup;
    }

    public static List<SecKillVo> getSecKillVoListByGroup(int group) {
        return SEC_KILL_MAP_BY_GROUP.get(group);
    }

    public static Map<Integer, SecKillVo> getCheckGroupSecKillMap() {
        return CHECK_GROUP_SEC_KILL_MAP;
    }

    public static void setCheckGroupSecKillMap(Map<Integer, SecKillVo> checkGroupSecKillMap) {
        CHECK_GROUP_SEC_KILL_MAP = checkGroupSecKillMap;
    }

    public static boolean isDailyReset() {
        return IS_DAILY_RESET;
    }

    public static void setIsDailyReset(boolean isDailyReset) {
        IS_DAILY_RESET = isDailyReset;
    }
}
