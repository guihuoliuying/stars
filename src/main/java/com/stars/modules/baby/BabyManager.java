package com.stars.modules.baby;

import com.stars.modules.baby.prodata.BabyFashion;
import com.stars.modules.baby.prodata.BabySweepVo;
import com.stars.modules.baby.prodata.BabyVo;
import com.stars.modules.daily.DailyManager;

import java.util.*;

/**
 * Created by chenkeyu on 2017-07-20.
 */
public class BabyManager {
    public static int PAY_MULTIPLE;//付费培养的进度成长倍数
    public static int NORMAL_LUCKY_VALUE;//普通求子失败增加的幸运值
    public static int PAY_LUCKY_VALUE;//付费求子失败增加的幸运值
    public static double NORMAL_RATE;//普通求子的参数
    public static double PAY_RATE;//付费求子的参数
    public static int DELTA_ENERGY;//每次回复精力值
    public static int INTERVAL_ENERGY;//精力值恢复间隔
    public static int MAX_ENERGY;//精力上限
    public static int NORMAL_CRIT_PER;//普通培养暴击概率
    public static int PAY_CRIT_PER;//付费培养暴击概率
    public static int NORMAL_CRIT;//普通培养_暴击
    public static int PAY_CRIT;//付费培养_暴击
    public static boolean SWEEP;//扫荡
    //    public static int NORMAL_PRAY_MAX_TIMES;//每天最大求子次数
//    public static int NORMAL_FEED_MAX_TIMES;//每天最大培养次数
    public static int CHANGENAME_REQ_ITEMID;//改名字需要的itemid
    public static int CHANGENAME_REQ_ITEMCOUNT;//改名字需要的itemCount
    public static List<String> PRAY_FAIL_TIPS = new ArrayList<>();//求子失败的提示语
    public static Map<Integer, Map<Integer, List<String>>> FEED_TIPS = new HashMap<>();

    public static Map<Integer, Integer> stageLvFeedCountMap = new HashMap<>();

    public static Map<Integer, Map<Integer, BabyVo>> babyVoMap = new HashMap<>();//stage,level,BabyVo;
    public static Map<Integer, List<BabySweepVo>> babySweepVoMap = new HashMap<>();//id,BabySweepVo
    public static Map<Integer, BabyFashion> babyFashionVoMap;
    public static int defaultFashionId = 1;//默认时装id

    public static BabyVo getNextBabyVo(int preBabyStage, int preBabyLevel) {
        if (preBabyStage == BabyConst.PRAY) {
            return getNextBabyVo(BabyConst.QIANGBAO);
        } else if (preBabyStage == BabyConst.QIANGBAO) {
            return getNextBabyVo(BabyConst.YOUER);
        } else if (preBabyStage == BabyConst.YOUER) {
            return getBabyVo(BabyConst.BABY, 1);
        } else if (preBabyStage == BabyConst.BABY) {
            return getBabyVo(BabyConst.BABY, preBabyLevel + 1);
        }
        return null;
    }

    public static int getBabyMaxLevel() {
        List<Integer> levelList = new LinkedList<>();
        levelList.addAll(babyVoMap.get(BabyConst.BABY).keySet());
        return Collections.max(levelList);
    }

    public static boolean isMaxLevel(int level, int curPro) {
        if (level == getBabyMaxLevel()) {
            if (curPro >= babyVoMap.get(BabyConst.BABY).get(level).getProgress()) {
                return true;
            }
        }
        return false;
    }

    private static BabyVo getNextBabyVo(int stage) {
        Iterator<BabyVo> it = babyVoMap.get(stage).values().iterator();
        return it.next();
    }

    public static BabyVo getBabyVo(int babyStage, int babyLevel) {
        return babyVoMap.get(babyStage).get(babyLevel);
    }

    public static List<BabyVo> getBabyVoList(int babyStage, int babyLevel) {
        List<BabyVo> babyVoList = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, BabyVo>> entry : babyVoMap.entrySet()) {
            if (entry.getKey() == BabyConst.PRAY)
                continue;
            if (babyStage > entry.getKey()) {
                babyVoList.addAll(entry.getValue().values());
            }
//            if (babyStage == entry.getKey() && (babyStage == BabyConst.QIANGBAO || babyStage == BabyConst.YOUER)) {
//                babyVoList.addAll(entry.getValue().values());
//            }
            for (Map.Entry<Integer, BabyVo> voEntry : entry.getValue().entrySet()) {
                if (babyStage == entry.getKey() && babyLevel > voEntry.getKey()) {
                    babyVoList.add(voEntry.getValue());
                }
            }
        }
        return babyVoList;
    }

    public static String getFeedTips(int type, int stage) {
        List<String> tmpList = FEED_TIPS.get(type).get(stage);
        int index = new Random().nextInt(tmpList.size());
        return tmpList.get(index);
    }

    public static BabySweepVo getBabySweepVo(int id, int level, int mark) {
        for (BabySweepVo babySweepVo : babySweepVoMap.get(id)) {
            if (id != BabyConst.SEARCHTREASURE_SWEEP && babySweepVo.matchLv(level))
                return babySweepVo;
            if (id == BabyConst.SEARCHTREASURE_SWEEP && babySweepVo.matchLv(level) && babySweepVo.getLoopMark() == getNextMark(mark)) {
                return babySweepVo;
            }
        }
        return null;
    }

    public static List<BabySweepVo> getBabySweepVo(int level, int mark, boolean isMarry) {
        List<BabySweepVo> babySweepVoList = new ArrayList<>();
        for (Map.Entry<Integer, List<BabySweepVo>> entry : babySweepVoMap.entrySet()) {
            for (BabySweepVo babySweepVo : entry.getValue()) {
                if (entry.getKey() == BabyConst.MARRY_SWEEP && !isMarry)
                    continue;
                if (entry.getKey() != BabyConst.SEARCHTREASURE_SWEEP && babySweepVo.matchLv(level))
                    babySweepVoList.add(babySweepVo);
                if (entry.getKey() == BabyConst.SEARCHTREASURE_SWEEP && babySweepVo.matchLv(level) && babySweepVo.getLoopMark() == getNextMark(mark))
                    babySweepVoList.add(babySweepVo);
            }
        }
        return babySweepVoList;
    }

    public static int getNextMark(int mark) {
        switch (mark) {
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 1;
            default:
                return 1;
        }
    }

    public static short getDailyId(int id) {
        switch (id) {
            case BabyConst.RIDE_SWEEP:
                return DailyManager.DAILYID_PRODUCEDUNGEON_RIDE;
            case BabyConst.STONE_SWEEP:
                return DailyManager.DAILYID_PRODUCEDUNGEON_STRENGTHEN_STONE;
            default:
                return -1;
        }
    }

    public static int getMaxPrayOrFeedCount(int type) {
        if (stageLvFeedCountMap.containsKey(type)) {
            return stageLvFeedCountMap.get(type);
        } else {
            return Integer.MAX_VALUE;
        }
    }
}
