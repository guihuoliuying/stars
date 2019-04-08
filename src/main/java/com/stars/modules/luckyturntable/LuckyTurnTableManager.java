package com.stars.modules.luckyturntable;

import com.stars.modules.luckyturntable.prodata.LuckyTurnTableVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-07-13.
 */
public class LuckyTurnTableManager {
    public static final byte DRAWN = 1;
    public static final byte NOTDRAWN = 0;
    public static final byte INIT = 1;
    public static final byte OVER = 0;
    public static final int SHOW = 1;
    public static final int UNSHOW = 0;
    public static final String LUCKYTURNTABLE = "LTT.RESETTIMESTAMP";
    public static final int MAIL_ID = 31001;
    public static Map<Integer, LuckyTurnTableVo> luckyTurnTableMap = new HashMap<>();
    public static int luckWard_Worth;
    public static int recycle_ItemId;
    public static int recycle_Count;
    public static int luckyward_List;
    public static Map<Integer, Integer> luckWard_TicketSnumber = new HashMap<>();

    public static List<Integer> getLockyTurnTableVos(long roleId, int level, int vipLv) {
        List<Integer> voList = new ArrayList<>();
        for (LuckyTurnTableVo turnTableVo : luckyTurnTableMap.values()) {
            if (level >= turnTableVo.getMinLv() && level <= turnTableVo.getMaxLv()
                    && vipLv >= turnTableVo.getMinVipLv() && vipLv <= turnTableVo.getMaxVipLv()) {
                voList.add(turnTableVo.getId());
            }
        }
        return voList;
    }

    public static int getNeedLottery(int times) {
        if (luckWard_TicketSnumber.containsKey(times)) {
            return luckWard_TicketSnumber.get(times);
        } else {
            return -1;
        }
    }
}
