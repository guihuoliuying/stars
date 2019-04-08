package com.stars.modules.familyactivities.war;

import com.stars.modules.familyactivities.war.prodata.FamilyWarMoraleVo;
import com.stars.modules.familyactivities.war.prodata.FamilyWarRankAwardVo;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.services.ServiceHelper;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by zhaowenshuo on 2016/11/22.
 */
public class FamilyActWarManager {

    public static int numOfFighterInEliteFight = 5;//精英战场一方的参赛人数
    public static int numOfFighterInNormalFight = 5; // 小战场一方的参赛人数

    public static int originalPoints = 200;//精英赛家族原始积分
    public static int coefficientA = 100; // 精英赛系数A：获得个人积分
    public static int coefficientAA = 100; // 精英赛系数AA：获得个人积分
    public static int coefficientD = 100; // 匹配赛系数D：获得个人积分
    public static int coefficientDD = 100; // 匹配系数DD：获得个人积分
    public static int coefficientB = 100; // 系数：精英战场家族获得士气
    public static int coefficientC = 100; // 系数：匹配战场家族获得士气

    public static final int battleCount = 3;//表示一轮战斗的场数

    public static int familywar_lasttime;//填整数，秒为单位，表示家族战每场精英战持续多少时间
    public static int familywar_intervaltime;//填整数，秒为单位，表示家族战每场精英战间隔时间
    public static int familywar_pairlasttime;//填整数，秒为单位，表示家族战匹配战场每场持续时间
    public static int familywar_pairstageodd;//填整数，百分比，表示匹配战场直接匹配关卡的几率
    public static int familywar_smallwinscore;//填整数，表示匹配战场每场战斗胜利后，给家族增加的额外士气。
    public static int familywar_smallfailscore;//填整数，表示匹配战场每场战斗失败后，给家族增加的额外士气。
    public static int familywar_smallpairscore;//填整数，表示匹配战场每场战斗失败后，给家族增加的额外士气。
    public static int familywar_cycletime_min;//填整数，表示新区开服大于多少天内可以参加本服家族战
    public static int familywar_cycletime_max;//填整数，表示新区开服小于多少天内可以参加本服家族战
    public static int familywar_pvpwinscore;//填整数,分别表示每场匹配战pvp胜利后获得的积分
    public static int familywar_pvewinscore;//填整数,分别表示每场匹配战pve胜利后获得的积分
    public static int familywar_elitewinscore;//填整数，表示每场精英战胜利后获得的积分
    public static int familywar_elitefailscore;//填整数，表示每场精英战失败后获得的积分
    public static int familywar_coefficient_hp;//
    public static int familywar_coefficient_attack;//
    public static int familywar_coefficient_defense;//
    public static int familywar_coefficient_hit;//
    public static int familywar_coefficient_avoid;//
    public static int familywar_coefficient_crit;//
    public static int familywar_coefficient_anticrit;//
    public static int familywar_coefficient_hp_zz;//
    public static int familywar_coefficient_attack_zz;//
    public static int familywar_coefficient_defense_zz;//
    public static int familywar_coefficient_hit_zz;//
    public static int familywar_coefficient_avoid_zz;//
    public static int familywar_coefficient_crit_zz;//
    public static int familywar_coefficient_anticrit_zz;//
    public static int familywar_coefficient_hp_zzz;//
    public static int familywar_coefficient_attack_zzz;//
    public static int familywar_coefficient_defense_zzz;//
    public static int familywar_coefficient_hit_zzz;//
    public static int familywar_coefficient_avoid_zzz;//
    public static int familywar_coefficient_crit_zzz;//
    public static int familywar_coefficient_anticrit_zzz;//
    public static int familywar_score_elitewin;//玩家在精英战中胜利，增加个人积分
    public static int familywar_score_pairwin;//玩家在匹配战pvp中胜利，增加个人积分
    public static int familywar_score_pvewin;//玩家在匹配打关卡中胜利，增加个人积分

    public static Map<Integer, Integer> serverSizeMap;


    public static String familywar_roll_winer;//冠军的跑马灯
    public static String familywar_roll_winer2;//跨服海选的跑马灯
    public static String familywar_roll_winer3;//跨服决赛的跑马灯


    public static int normalFightWinMorale = 100;

    public static int stageIdOfEliteFight = 9301;
    public static int stageIdOfNormalFight = 9302;
    public static int stageIdOfStageFight = 9303;
    public static int stageIdOfQualifyingFight = 0;
    public static int stageIdOfSafe;

    public static int timeLimitOfNormalFight;
    public static int timeLimitOfStageFight = 60;

    public static int moraleDeltaOfDestoryTower = 0; // 破坏塔之后增加的士气
    public static int moraleDeltaOfLosingTower = 0; // 丢失塔之后减掉的士气
    public static int moraleDeltaOfDestoryCrystal = 0; // 破坏水晶之后增加的士气
    public static int moraleDeltaOfKillFighterInEliteFight = 0; // 杀人之后增加的士气
    public static int moraleDeltaOfKillFighterInNormalFight = 0; // 杀人之后增加的士气
//    public static int moraleDeltaOfWinningNormalFight = 0; // 匹配战赢了之后的增加的士气
//    public static int moraleDeltaOfWinningStageFight = 100;//匹配关卡赢了之后增加的士气

    public static int pointsDeltaOfDestoryTower = 0; // 破坏塔之后增加的积分
    public static int pointsDeltaOfDestoryCrystal = 0; // 破坏水晶之后增加的积分
    //    public static int pointsDeltaOfKillFighter = 0; // 杀人之后增加的积分
    public static double damageRatioThresholdOfPersonalPoints = 0.1; // 开始计算个人积分的百分比
    public static int killCountThresholdOfNotice = 1; // 连杀通知的阈值

    public static int dropIdOfEliteFightWinAward = 0;
    public static int dropIdOfEliteFightLoseAward = 0;
    public static int dropIdOfEliteQualifyFightWinAward = 0;
    public static int dropIdOfEliteQualifyFightLoseAward = 0;
    public static int dropIdOfEliteRemoteFightWinAward = 0;
    public static int dropIdOfEliteRemoteFightLoseAward = 0;
    public static int dropIdOfNormalFightWinAward = 0;
    public static int dropIdOfNormalFightLoseAward = 0;
    public static int dropIdOfStageFightWinAward = 0;

    public static int emailTemplateIdOfEliteFightWinAward = 10916; // 邮件模板id（精英战胜利邮件）
    public static int emailTemplateIdOfEliteFightLoseAward = 10917; // 邮件模板id（精英战失败邮件）
    public static int emailTemplateIdOfNormalFightAward = 10918;
    public static int emailTemplateIdOfAddingToTeamSheet = 0;
    public static int emailTemplateIdOfDeletingFromTeamSheet = 0;
    public static int emailTemplateIdOfMinPointsAward = 0;
    public static int emailTemplateIdOfFamilyBeChosen = 0;//家族获得资格
    public static int emailTemplateIdOfCancelFromTeamSheet = 10994;
    public static int emailTemplateIdOfTellTeamSheetToMaster = 10997;

    /* 排行奖励id */
    public static int rankAwardIdOfLocalEliteFight = 906;//本服精英
    public static int rankAwardIdOfLocalNormalFight = 907;//本服匹配
    public static int rankAwardIdOfQualifyEliteFight = 908;//跨服海选精英
    public static int rankAwardIdOfQualifyNormalFight = 909;//跨服海选匹配
    public static int rankAwardIdOfRemoteEliteFight = 918;//跨服决赛精英
    public static int rankAwardIdOfRemoteNormalFight = 919;//跨服决赛匹配

    public static List<Integer> timelineOfKnockout = new ArrayList<>();
    public static Map<Integer, List<Integer>> timeLineOfRemote = new HashMap<>();

    // elite fight
    public static String camp1CrystalId;
    public static String camp2CrystalId;
    public static Set<String> crystalIdSet = new HashSet<>();

    public static String camp1TopTowerId; // 上路
    public static String camp1MidTowerId; // 中路
    public static String camp1BotTowerId; // 下路
    public static String camp2TopTowerId; // 上路
    public static String camp2MidTowerId; // 中路
    public static String camp2BotTowerId; // 下路
    public static Set<String> towerIdSet = new HashSet<>(); //

    public static Map<Integer, Byte> towerTypeMap = new HashMap<>();

    public static Map<Long, Map<Integer, Integer>> eliteMinPointsAwardMap; // (最低积分, (itemId, count))
    public static Map<Long, Map<Integer, Integer>> eliteMinPointsAwardMapForQualify; // (最低积分, (itemId, count))
    public static Map<Long, Map<Integer, Integer>> eliteMinPointsAwardMapForRemote; // (最低积分, (itemId, count))
    public static Map<Long, Map<Integer, Integer>> normalMinPointsAwardMap; // (最低积分, (itemId, count))
    public static Map<Long, Map<Integer, Integer>> normalMinPointsAwardMapForQualify; // (最低积分, (itemId, count))
    public static Map<Long, Map<Integer, Integer>> normalMinPointsAwardMapForRemote; // (最低积分, (itemId, count))

    public static Map<Integer, FamilyWarRankAwardVo> familyRankAwardVoMap;
    public static Map<Integer, List<FamilyWarRankAwardVo>> familyRankAwardVoMapByPeriod;

    public static List<FamilyWarMoraleVo> moraleVoList;

    public static int[] timeLinePoint = {1, 2, 4, 8, 12, 14};//赛程界面时间线上的点
    public static Map<Integer, List<Integer>> remoteTimeLinePoint;


    /* 点赞相关 */
    public static int maxSupportCount;//角色最大点赞次数
    public static int dropIdOfSupportAward;//点赞奖励掉落组

    /**
     * 动态阻挡时间(秒)
     */
    public static int DYNAMIC_BLOCK_TIME = 30;

    /* 复活相关 */
    public static int homeReviveTime;//复活时间
    public static int payReviveTime;//付费复活时间
    public static int totalPayRevive;//付费复活次数
    public static int revivePay;//复活费用
    public static byte homeRevive = 0;
    public static byte payRevive = 1;

    /**
     * 无敌buffId
     */
    public static int invincibleBuffId = 999400;

    /**
     * 匹配战场等待时间（秒）
     */
    public static int timeOfNoramlFightWaiting = 10;

    public static int fighterStateTimeout = 180;//战斗状态持续时间，秒

    /* 匹配相关 */
    public static int normalMatchInterval;//普通战场匹配时间
    public static ScheduledExecutorService matchScheduler = null; //匹配定时器
    public static boolean isMatchRunnable = true;//是否可以执行匹配
    public static Runnable matchTask = new Runnable() {//匹配任务
        @Override
        public void run() {
            if (isMatchRunnable) {
                ServiceHelper.familyWarLocalService().match();
            }
        }
    };

    public static Runnable matchTaskQualifying = new Runnable() {
        @Override
        public void run() {
            if (isMatchRunnable) {
                ServiceHelper.familyWarQualifyingService().match(FamilyWarUtil.getFamilyWarServerId());
            }
        }
    };

    public static Runnable matchTaskRemote = new Runnable() {
        @Override
        public void run() {
            if (isMatchRunnable) {
                ServiceHelper.familyWarRemoteService().match(FamilyWarUtil.getFamilyWarServerId());
            }
        }
    };

    public static FamilyWarRankAwardVo getFamilyRankAwardVo(int period, int rank, int objType) {
        List<FamilyWarRankAwardVo> list = familyRankAwardVoMapByPeriod.get(period);
        if (list != null) {
            for (FamilyWarRankAwardVo vo : list) { // 量非常少，遍历就够了
                if (rank >= vo.getMinRank() && rank <= vo.getMaxRank()
                        && objType == vo.getObjType()) {
                    return vo;
                }
            }
        }
        return null;
    }

    public static FamilyWarMoraleVo getMoraleVo(int morale) {
        for (FamilyWarMoraleVo vo : moraleVoList) {
            if (morale >= vo.getMinMorale() && morale <= vo.getMaxMorale()) {
                return vo;
            }
        }
        return null;
    }

    public static int getFamilyCount(int serverSize) {
        List<Integer> size = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : serverSizeMap.entrySet()) {
            if (entry.getKey() <= serverSize) {
                size.add(entry.getValue());
            }
        }
        return Collections.max(size);
    }

}
