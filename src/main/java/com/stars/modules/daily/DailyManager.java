package com.stars.modules.daily;

import com.stars.modules.daily.prodata.DailyAwardVo;
import com.stars.modules.daily.prodata.DailyBallStageVo;
import com.stars.modules.daily.prodata.DailyFightScoreModuleVo;
import com.stars.modules.daily.prodata.DailyVo;

import java.util.List;
import java.util.Map;

public class DailyManager {
    /* 常量 */
    public static short DAILYID_LOGININ = 1;// 登陆游戏;
    public static short DAILYID_SWEEPDUNGEON = 2;// 扫荡关卡
    public static short DAILYID_EQUIP_QIANGHUA = 3;// 装备强化
    public static short DAILYID_EQUIP_JINJIE = 4;// 装备进阶
    public static short DAILYID_EQUIP_SHENGXING = 5;// 装备锻造/升星
    public static short DAILYID_EQUIP_FUMO = 6;// 装备附魔
    public static short DAILYID_GEM_COMPOSE = 7;// 合成宝石
    public static short DAILYID_GAME_CAVE = 8;//洞府;
    public static short DAILYID_SEARCHTREASURE = 9;// 仙山探宝;
    public static short DAILYID_CALLBOSS = 10;// 召唤boss（全民讨逆）
    public static short DAILYID_SKYTOWER = 11;// 镇妖塔;
    public static short DAILYID_TEAMDUNGEON_DEFEND = 12;// 组队副本-守护类型
    public static short DAILYID_TEAMDUNGEON_CHALLENGE = 13;// 组队副本-挑战类型
    public static short DAILYID_PRODUCEDUNGEON_ROLEEXP = 18;// 通关角色经验产出副本(资源产出副本type=1)
    public static short DAILYID_BRAVE_PRACTISE = 20;// 勇者试炼
    public static short DAILYID_MASTER_NOTICE = 21;// 皇榜悬赏
    public static short DAILYID_PRODUCEDUNGEON_STRENGTHEN_STONE = 22;// 强化石产出副本(资源产出副本type=2)
    public static short DAILYID_SIGNID = 23;//月签到
    public static short DAILYID_ESCORT = 24;//运镖
    public static short DAILYID_PRODUCEDUNGEON_RIDE = 25;//坐骑饲料产出副本
    public static short DAILYID_GUEST_MISSION = 26;    // 门客任务
    public static short DAILYID_FAMILYTASK = 31;    // 家族任务
    public static short DAILYID_ELITE_DUNGEON = 32;    // 精英副本
    public static short DAILYID_MARRY_DUNGEON = 33; //情义副本
    public static short DAILYID_RUNE_DUNGEON = 34; //符文副本
    public static short DAILYID_BOOK = 35; //典籍
    public static short DAILYID_DAILY_5V5 = 36; //日常5v5
    public static short DAILYID_FIGHTING_MASTER = 37; //巅峰对决
    public static short DAILYID_FAMILY_TREASURE = 38; //家族探宝
    public static short DAILYID_OFFLINE_PVP = 39; //演武场
    public static short DAILYID_DAREGOD = 51;//挑战女神

    public static final byte SUPER_AWARD = 1; //超级奖励
    public static final byte MUTIPLE_AWARD = 2; //多倍奖励,后改成额外奖励

    public static final byte SUPER_AWARD_COMMON = 1; //普通档超级奖励
    public static final byte SUPER_AWARD_BETTER = 2; //高级档超级奖励
    public static final byte SUPER_AWARD_BEST = 3; //超量档超级奖励

    public static final byte AWARD_PROMPT_BACK_CITY = 0; //回城才显示奖励
    public static final byte AWARD_PROMPT_IMMEDIATE = 1; //立即显示奖励

    private static Map<Short, DailyVo> dailyVoMap;
    private static Map<Integer, DailyAwardVo> dailyAwardVoMap; //日常奖励 超级奖励+多倍奖励 key:awardId value: 奖励数据
    private static List<DailyAwardVo> superAwardList; //超级奖励列表
    private static List<DailyAwardVo> multipleAwardList; //多倍奖励列表
    private static boolean sendAwardSwitch; //发送奖励开关

    //private static Map<Integer, String>dailyAwardMap;
    private static Map<String, DailyFightScoreModuleVo> dailyFightScoreModuleVoMap;
    private static Map<Byte, Map<Short, DailyVo>> dailyVoByTagMap; //日常数据：key: tagId:标签Id values：<dailyid,dailyVo>
    private static Map<Integer, DailyBallStageVo> dailyBallStageVoMap; //斗魂珠 key:level values:dailyBallStage
    private static Map<Integer, Integer> dailyBallStageMaxStarMap; //斗魂珠每个阶数的最高等级
    private static int maxDailyBallLevel; //斗魂珠当前最高等级
    private static int preOpenDayForBetterSuperAward; //高级超级奖励的检查开服天数差
    private static int preOpenDayForBestSuperAward; //超量超级奖励的检查开服天数差

    public static Map<String, DailyFightScoreModuleVo> getDailyFightScoreModuleVoMap() {
        return dailyFightScoreModuleVoMap;
    }

    public static void setDailyFightScoreModuleVoMap(Map<String, DailyFightScoreModuleVo> dailyFightScoreModuleVoMap) {
        DailyManager.dailyFightScoreModuleVoMap = dailyFightScoreModuleVoMap;
    }

    public static DailyFightScoreModuleVo getDailyFightScoreModuleVo(String sysName, int openDays) {
        String key = sysName + "_" + openDays;
        return DailyManager.dailyFightScoreModuleVoMap.get(key);
    }

    //获得系统的推荐战力
    public static int getRecommFightScore(String sysName, int openDays) {
        DailyFightScoreModuleVo dailyFightScoreModuleVo = getDailyFightScoreModuleVo(sysName, openDays);
        return dailyFightScoreModuleVo == null ? 0 : dailyFightScoreModuleVo.getRecommFightScore();
    }

    public static Map<Short, DailyVo> getDailyVoMap() {
        return dailyVoMap;
    }

    public static void setDailyVoMap(Map<Short, DailyVo> dailyVoMap) {
        DailyManager.dailyVoMap = dailyVoMap;
    }

    public static DailyVo getDailyVo(short dailyId) {
        return dailyVoMap.get(dailyId);
    }

//	public static Map<Integer, String> getDailyAwardMap() {
//		return dailyAwardMap;
//	}
//
//	public static void setDailyAwardMap(Map<Integer, String> dailyAwardMap) {
//		DailyManager.dailyAwardMap = dailyAwardMap;
//	}

    public static List<DailyAwardVo> getMultipleAwardList() {
        return multipleAwardList;
    }

    public static void setMultipleAwardList(List<DailyAwardVo> multipleAwardList) {
        DailyManager.multipleAwardList = multipleAwardList;
    }

    public static List<DailyAwardVo> getSuperAwardList() {
        return superAwardList;
    }

    public static void setSuperAwardList(List<DailyAwardVo> superAwardList) {
        DailyManager.superAwardList = superAwardList;
    }

    public static Map<Byte, Map<Short, DailyVo>> getDailyVoByTagMap() {
        return dailyVoByTagMap;
    }

    public static Map<Short, DailyVo> getDailyVoMapByTag(byte tag) {
        return dailyVoByTagMap.get(tag);
    }

    public static void setDailyVoByTagMap(Map<Byte, Map<Short, DailyVo>> dailyVoByTagMap) {
        DailyManager.dailyVoByTagMap = dailyVoByTagMap;
    }


    public static Map<Integer, DailyBallStageVo> getDailyBallStageVoMap() {
        return dailyBallStageVoMap;
    }

    public static DailyBallStageVo getDailyBallStageVoByLevel(int level) {
        return dailyBallStageVoMap.get(level);
    }

    public static void setDailyBallStageVoMap(Map<Integer, DailyBallStageVo> dailyBallStageVoMap) {
        DailyManager.dailyBallStageVoMap = dailyBallStageVoMap;
    }

    public static Map<Integer, Integer> getDailyBallStageMaxStarMap() {
        return dailyBallStageMaxStarMap;
    }

    public static int getDailyBallMaxStarByStage(int stage) {
        return dailyBallStageMaxStarMap.get(stage);
    }

    public static void setDailyBallStageMaxStarMap(Map<Integer, Integer> dailyBallStageMaxStarMap) {
        DailyManager.dailyBallStageMaxStarMap = dailyBallStageMaxStarMap;
    }

    public static int getMaxDailyBallLevel() {
        return maxDailyBallLevel;
    }

    public static void setMaxDailyBallLevel(int maxDailyBallLevel) {
        DailyManager.maxDailyBallLevel = maxDailyBallLevel;
    }

    public static Map<Integer, DailyAwardVo> getDailyAwardVoMap() {
        return dailyAwardVoMap;
    }

    public static void setDailyAwardVoMap(Map<Integer, DailyAwardVo> dailyAwardVoMap) {
        DailyManager.dailyAwardVoMap = dailyAwardVoMap;
    }

    public static DailyAwardVo getDailyAwardVoById(int awardId) {
        return DailyManager.dailyAwardVoMap.get(awardId);
    }

    public static int getPreOpenDayForBetterSuperAward() {
        return preOpenDayForBetterSuperAward;
    }

    public static void setPreOpenDayForBetterSuperAward(int preOpenDayForBetterSuperAward) {
        DailyManager.preOpenDayForBetterSuperAward = preOpenDayForBetterSuperAward;
    }

    public static int getPreOpenDayForBestSuperAward() {
        return preOpenDayForBestSuperAward;
    }

    public static void setPreOpenDayForBestSuperAward(int preOpenDayForBestSuperAward) {
        DailyManager.preOpenDayForBestSuperAward = preOpenDayForBestSuperAward;
    }

    public static boolean isSendAwardSwitch() {
        return sendAwardSwitch;
    }

    public static void setSendAwardSwitch(boolean sendAwardSwitch) {
        DailyManager.sendAwardSwitch = sendAwardSwitch;
    }
}
