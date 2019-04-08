package com.stars.modules.achievement;

import com.stars.modules.achievement.prodata.AchievementStageVo;
import com.stars.modules.achievement.prodata.AchievementVo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhouyaohui on 2016/10/17.
 */
public class AchievementManager {
    public static final byte CONDITION_ROLE_LEVEL = 1; //玩家等级
    public static final byte CONDITION_VIP_LEVEL = 2;  //玩家VIP等级

    public static final byte COMMON_STAGE_AWARD = 1;  //一般升阶奖励
    public static final byte PERFECT_STAGE_AWARD = 2;  //完美升阶奖励

    public static  int RANK_COUNT = 50; //排行榜个数


    public static ConcurrentMap<Integer, AchievementVo> achievementVo = new ConcurrentHashMap<>();
    public static ConcurrentMap<Integer, ConcurrentMap<Integer, AchievementVo>> typeMap = new ConcurrentHashMap<>();
    private static Map<Integer, AchievementStageVo> achievementStageVoMap = new ConcurrentHashMap<>();

    public static Map<Integer, AchievementStageVo> getAchievementStageVoMap() {
        return achievementStageVoMap;
    }


    public static AchievementStageVo getAchievementStageVoByStage(int stage){
        return achievementStageVoMap.get(stage);
    }

    public static void setAchievementStageVoMap(Map<Integer, AchievementStageVo> achievementStageVoMap) {
        AchievementManager.achievementStageVoMap = achievementStageVoMap;
    }

    public static AchievementVo getAchievementVo(int achievementId) {
        return achievementVo.get(achievementId);
    }

    public static int getRankCount() {
        return RANK_COUNT;
    }

    public static void setRankCount(int rankCount) {
        RANK_COUNT = rankCount;
    }
}
