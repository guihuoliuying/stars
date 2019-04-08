package com.stars.modules.weeklygift;

import com.stars.modules.weeklygift.prodata.WeeklyGiftVo;

import java.util.*;

/**
 * Created by chenkeyu on 2017-06-28.
 */
public class WeeklyGiftManager {
    private static Map<Integer, WeeklyGiftVo> weeklyGiftVoMap = new HashMap<>();

    public static Map<Integer, WeeklyGiftVo> getWeeklyGiftVoMap() {
        return weeklyGiftVoMap;
    }

    public static void setWeeklyGiftVoMap(Map<Integer, WeeklyGiftVo> weeklyGiftVoMap) {
        WeeklyGiftManager.weeklyGiftVoMap = weeklyGiftVoMap;
    }

    public static List<WeeklyGiftVo> getVos(int vipLv, int level) {
        List<WeeklyGiftVo> vos = new ArrayList<>();
        for (WeeklyGiftVo vo : weeklyGiftVoMap.values()) {
            if (vipLv >= vo.getMinVipLv() && vipLv <= vo.getMaxVipLv()
                    && level >= vo.getMinLevel() && level <= vo.getMaxLevel()) {
                vos.add(vo);
            }
        }
        return vos;
    }

    public static List<WeeklyGiftVo> getVos(Collection<Integer> giftId) {
        List<WeeklyGiftVo> vos = new ArrayList<>();
        for (WeeklyGiftVo vo : weeklyGiftVoMap.values()) {
            if (giftId.contains(vo.getWeeklyGiftId())) {
                vos.add(vo);
            }
        }
        return vos;
    }
}
