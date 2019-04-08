package com.stars.services.rank;

import com.stars.services.rank.imp.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/23.
 */
public class RankConstant {
    /* 发奖类型 */
    public static byte REWARD_TYPE_OTHER = 0;// 其他发奖方式
    public static byte REWARD_TYPE_DAILY = 1;// 每日发奖
    public static byte REWARD_TYPE_WEEKLY = 2;// 每周发奖
    public static byte REWARD_TYPE_APPOINT = 3;// 指定时间发奖
    /* 邮件发奖类型 */
    public static byte REWARD_MAIL_TYPE_PERSON = 0;// 发给个人
    public static byte REWARD_MAIL_TYPE_COMPANY = 1;// 发给团队

    /* 排名(暂定) */
    public static int RANK_INIT = 0;// 初始排名值
    public static int RANK_OVER_1000 = -1;// 999<x
    public static int RANK_OVER_500 = -2;// 500<x<=999
    public static int RANK_OVER_100 = -1;// 100<x<=500

    /* 排序容器最大上限 */
    public static int RANK_VOLUME_MAX = 999;

    /* 排行榜类型 */
    public static byte RANK_TYPE_PERSON = 1;// 个人排行榜(roleId)
    public static byte RANK_TYPE_FAMILY = 2;// 家族
    public static byte RANK_TYPE_FAMILY_TREASURE = 3;//家族探宝排行榜
    public static byte RANK_TYPE_OPACT = 4;// 运营活动
    public static byte RANK_TYPE_BEST_CP = 5;// 最佳cp排行榜
    public static byte RANK_TYPE_BEST_CP_VOTER = 6;// 最佳cp投票排行榜
    public static byte RANK_TYPE_DRAGON_BOAT = 7;// 龙舟排行榜
    public static byte RANK_TYPE_CAMP_CITY_REPUTATION = 8;// 阵营城池声望排行榜
    public static byte RANK_TYPE_ACHIEVEMENT = 9; //成就排行榜

    /* 排行榜id,对应表配置 */
    public static final int RANKID_FIGHTSCORE = 101;// 战力排行榜id
    public static final int RANKID_GAMECAVESCORE = 102;// 洞府积分排行榜id
    public static final int RANKID_SKYTOWERLAYER = 103; // 镇妖塔排行榜id
    public static final int RANKID_FAMILYFIGHTSCORE = 104; // 家族总战力排行榜id
    public static final int RANKID_ROLELEVEL = 105; // 个人等级排行榜id
    public static final int RANKID_LOOTTREASURE_PVE = 904;//野外夺宝PVE伤害榜id;
    public static final int RANKID_FAMILYTREASURE = 106;//家族探宝家族排行榜id
    public static final int RANKID_ROLEFAMILYTREASURE = 107;//家族探宝玩家伤害榜id
    public static final int RANKID_OFFLINEPVP = 108;//离线竞技场排行奖励
    public static final int RANKID_TOTAL_ROLELEVEL = 109;//个人等级排行榜总榜
    public static final int RANKID_CHARGESOCRE = 110;// 充值榜
    public static final int RANKID_BEST_CP = 111;// 最佳cp排行榜
    public static final int RANKID_BEST_CP_VOTER = 112;// 最佳cp个人投票排行榜
    public static final int RANKID_DRAGON_BOAT = 113;// 龙舟投票排行榜
    public static final int RANKID_CROSS_SERVER_FAMILY_RANK = 1200;//跨服家族排行榜
    public static final int RANKID_CAMP_CITY_REPUTATION = 114;// 阵营城池声望排行榜
    public static final int RANKID_ACHIEVEMENT = 921; //成就排行榜
    /**
     * 112-122 每个cp的角色投票排行榜
     */
//    public static final int RANKID_NEWSERVER_FAMILYFIGHTSCORE = 1002;//本服家族战力冲榜

    public static final byte GM_FIGHT_TYPE = 1;
    public static final byte GM_LEVEL_TYPE = 2;


    public static Map<Integer, Class<? extends AbstractRank>> rankClazzMap = new HashMap<>();
    public static Map<Byte, List<Integer>> rankTypeMap = new HashMap<>();// 排行榜类型

    public static Map<Byte, Integer> gmRankTypeMap = new HashMap<>(); // gm类型对应排行榜类型

    static {
        gmRankTypeMap.put(GM_FIGHT_TYPE, RANKID_FIGHTSCORE);
        gmRankTypeMap.put(GM_LEVEL_TYPE, RANKID_ROLELEVEL);
    }

    static {
        registerRank(RANK_TYPE_PERSON, RANKID_FIGHTSCORE, FightScoreRank.class);
        registerRank(RANK_TYPE_PERSON, RANKID_GAMECAVESCORE, GamecaveScoreRank.class);
        registerRank(RANK_TYPE_PERSON, RANKID_SKYTOWERLAYER, SkyTowerRank.class);
        registerRank(RANK_TYPE_FAMILY, RANKID_FAMILYFIGHTSCORE, FamilyFightScoreRank.class);
        registerRank(RANK_TYPE_PERSON, RANKID_ROLELEVEL, RoleLevelRank.class);
        registerRank(RANK_TYPE_FAMILY_TREASURE, RANKID_FAMILYTREASURE, FamilyTreasureRank.class);
        registerRank(RANK_TYPE_PERSON, RANKID_ROLEFAMILYTREASURE, RoleFamilyTreasureRank.class);
        registerRank(RANK_TYPE_PERSON, RANKID_TOTAL_ROLELEVEL, RoleLevelRank.class);
        registerRank(RANK_TYPE_OPACT, RANKID_CHARGESOCRE, OpActChargeRank.class);
        registerRank(RANK_TYPE_BEST_CP, RANKID_BEST_CP, BestCpRank.class);//最佳cp排行榜
        registerRank(RANK_TYPE_BEST_CP_VOTER, RANKID_BEST_CP_VOTER, BestCpVoterRank.class);//最佳cp投票者排行榜
        registerRank(RANK_TYPE_DRAGON_BOAT, RANKID_DRAGON_BOAT, DragonBoatRank.class);//赛龙舟排行榜
        registerRank(RANK_TYPE_CAMP_CITY_REPUTATION, RANKID_CAMP_CITY_REPUTATION, CampRoleReputationRank.class);//阵营城池排行榜
        registerRank(RANK_TYPE_ACHIEVEMENT,RANKID_ACHIEVEMENT, AchievementRank.class); //成就排行
    }

    private static void registerRank(byte type, int rankId, Class<? extends AbstractRank> rankClazz) {
        rankClazzMap.put(rankId, rankClazz);
        if (!rankTypeMap.containsKey(type)) {
            rankTypeMap.put(type, new LinkedList<Integer>());
        }
        rankTypeMap.get(type).add(rankId);
    }
}
