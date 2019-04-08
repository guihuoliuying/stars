package com.stars.modules.daregod;

import com.stars.modules.daregod.prodata.SsbBoss;
import com.stars.modules.daregod.prodata.SsbBossTarget;
import com.stars.modules.daregod.prodata.SsbRankAward;
import com.stars.modules.daregod.prodata.VipBuyTimeForDareGod;
import com.stars.modules.data.DataManager;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenkeyu on 2017-08-23.
 */
public class DareGodManager {
    public static Map<Integer, List<SsbBoss>> ssbBossMap;//区 , list of ssbBoss
    public static Map<Integer, List<SsbRankAward>> rankAwardMap;//type, list of ssbRankAward
    public static Map<Integer, SsbBossTarget> ssbBossTargetMap;//id ,SsbBossTarget
    private static List<VipBuyTimeForDareGod> dareGods;// list of vip buy times
    public static int DARE_FREE_TIMES;//免费挑战次数
    public static int BUY_TIMES_REQ_ITEM_COUNT;//购买次数花费元宝数量
    public static Map<Integer, String> fightTypeDescMap;
    public static Map<Integer, String> fightTypeShapeMap;
    public static Set<Integer> fightTypeSet;

    public static int getCanBuyTimes(int vipLv) {
        for (VipBuyTimeForDareGod dareGod : dareGods) {
            if (dareGod.matchVipLv(vipLv))
                return dareGod.getTimes();
        }
        return 0;
    }

    public static SsbBoss getSsbBossByFightScore(int subServer, int fightScore) {
        List<SsbBoss> ssbBossList = ssbBossMap.get(subServer);
        if (ssbBossList == null) return null;
        for (SsbBoss ssbBoss : ssbBossList) {
            if (ssbBoss.matchFightScore(fightScore)) {
                return ssbBoss;
            }
        }
        return null;
    }

    public static SsbBoss getSsbBossByType(int subServer, int fightType) {
        List<SsbBoss> ssbBossList = ssbBossMap.get(subServer);
        if (ssbBossList == null) return null;
        for (SsbBoss ssbBoss : ssbBossList) {
            if (ssbBoss.getFightingType() == fightType) {
                return ssbBoss;
            }
        }
        return null;
    }

    public static SsbRankAward getSsbRankAward(int fightType, int rank) {
        List<SsbRankAward> ssbRankAwardList = rankAwardMap.get(fightType);
        if (ssbRankAwardList == null) return null;
        for (SsbRankAward ssbRankAward : ssbRankAwardList) {
            if (ssbRankAward.matchRank(rank)) {
                return ssbRankAward;
            }
        }
        return null;
    }

    public static int getSsbRankAwardId(int fightType, int rank) {
        List<SsbRankAward> ssbRankAwardList = rankAwardMap.get(fightType);
        if (ssbRankAwardList == null) return 0;
        for (SsbRankAward ssbRankAward : ssbRankAwardList) {
            if (ssbRankAward.matchRank(rank)) {
                return ssbRankAward.getAward();
            }
        }
        return 0;
    }

    public static String getFightTypeDesc(int fightType) {
        if (fightTypeDescMap.containsKey(fightType)) {
            return DataManager.getGametext(fightTypeDescMap.get(fightType));
        }
        return "";
    }

    public static String getFightTypeShape(int fightType) {
        if (fightTypeShapeMap.containsKey(fightType)) {
            return fightTypeShapeMap.get(fightType);
        }
        return "";
    }

    public static void setSsbBossMap(Map<Integer, List<SsbBoss>> ssbBossMap) {
        DareGodManager.ssbBossMap = ssbBossMap;
    }

    public static void setRankAwardMap(Map<Integer, List<SsbRankAward>> rankAwardMap) {
        DareGodManager.rankAwardMap = rankAwardMap;
    }

    public static void setDareGods(List<VipBuyTimeForDareGod> dareGods) {
        DareGodManager.dareGods = dareGods;
    }

    public static void setFightTypeDescMap(Map<Integer, String> fightTypeDescMap) {
        DareGodManager.fightTypeDescMap = fightTypeDescMap;
    }

    public static void setFightTypeShapeMap(Map<Integer, String> fightTypeShapeMap) {
        DareGodManager.fightTypeShapeMap = fightTypeShapeMap;
    }

    public static void setFightTypeSet(Set<Integer> fightTypeSet) {
        DareGodManager.fightTypeSet = fightTypeSet;
    }

    public static void setSsbBossTargetMap(Map<Integer, SsbBossTarget> ssbBossTargetMap) {
        DareGodManager.ssbBossTargetMap = ssbBossTargetMap;
    }
}
