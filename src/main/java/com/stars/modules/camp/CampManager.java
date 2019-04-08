package com.stars.modules.camp;

import com.stars.modules.camp.pojo.CampCityResetMoment;
import com.stars.modules.camp.pojo.CampEquilibrium;
import com.stars.modules.camp.pojo.CampFightOfficerAttr;
import com.stars.modules.camp.pojo.CampFightSingleScoreReward;
import com.stars.modules.camp.prodata.*;
import com.stars.util.RandomUtil;

import java.util.*;

/**
 * Created by huwenjun on 2017/6/27.
 */
public class CampManager {
    public static final int CAMPTYPE_QIN = 1;//秦国
    public static final int CAMPTYPE_CHU = 2;//楚国
    public static final int STAGE_ID_CAMP_FIGHT = 9201;
    public static final int BUFF_ID_NB = 4404010;//无敌buff
    public static final int BUFF_ID_SPEED_UP = 4405010;//加速buff
    public static final int BUFF_ID_NB_LONG = 4412030;//无敌buff 长
    public static final int BUFF_ID_SPEED_UP_LONG = 4413030;//加速buff 长
    /**
     * 声望重置记录key
     */
    public static final String campReputationResetTimestamp = "campReputationResetTimestamp";
    public static Map<Integer, CampAtrVo> campAtrMap;//阵营
    public static Map<Integer, CampCityVo> campCityMap;
    public static Map<Integer, CommonOfficerVo> commonOfficerMap;
    public static Map<Integer, RareOfficerVo> rareOfficerMap;
    public static Map<Integer, DesignateOfficerVo> designateOfficerMap;
    public static Map<Integer, CampActivityVo> campActivityMap;
    public static Map<Integer, CampMissionVo> campMissionMap;
    public static Integer[] campTypeArr = new Integer[2];
    /**
     * 阵营平衡开启阙值
     */
    public static Integer campEquilibriumnum;
    public static List<CampEquilibrium> campEquilibriumList;
    public static float campAddLimitNum;
    /**
     * 随机进入阵营系数
     */
    public static int randomEnterCampConst;
    /**
     * 随机进入阵营奖励
     */
    public static Map<Integer, Integer> randomEnterReward;
    /**
     * <camptype,<level,camplevelvo>>
     */
    public static Map<Integer, Map<Integer, CampLevelVo>> campTypeLevelMap;
    public static Map<Integer, List<CampCityVo>> campCityListMap;
    /**
     * 所有阵营城市集合
     */
    public static List<CampCityVo> campCityVoList;
    public static Map<Integer, Map<Integer, CampCityVo>> campCityLvListMap;
    /**
     * 普通官职等级增序列表
     */
    public static List<CommonOfficerVo> commonOfficerVoList;
    public static CampCityResetMoment campCityResetMoment;
    public static Map<Integer, CommonOfficerVo> levelCommonOfficerVoMap;
    public static Map<Integer, List<CampRankAwardVo>> campRankArardMap;
    /**
     * 阵营活动列表
     */
    public static List<CampActivityVo> campActivityList;
    /**
     * 阵营任务列表
     */
    public static List<CampMissionVo> campMissionList;

    /* 齐楚之争 */
    public static int[] cityLvLimit;//可挑战城池等级限制

    public static int[] robotFightValue;//机器人战力范围（百分比）

    public static List<int[]> commonOfficerMark;//普通击杀积分

    public static List<int[]> rareOfficerMark;//特殊官职击杀积分

    public static String[] myPosition;//己方出生点 

    public static String[] enemyPosition;//敌方出生点 

    public static int Camp_Fight_DungeonId = 0;//关卡id

    public static Map<Integer, Integer> winFinishAward;//胜利保底

    public static Map<Integer, Integer> loseFinishAward;//失败保底

    public static Map<Integer, Integer> highCityLevelMark;//积分比例调整

    public static Map<Integer, Integer> lowCityLevelMark;//积分比例调整

    public static List<int[]> awardScaleList;//收益比例  挑战次数相关

    public static int Max_Scale_Cha_Num;

    public static int[] teamMark;//组队积分加成      [加成比例，默认加成值]

    public static int[] matchTime;

    public static List<int[]> markAwardList;//积分奖励   [min,max,dropid]

    public static int Cha_CityPlayNum_Limit = 0;//城池人数大于等于此值才可挑战

    public static int enemyUid = 1;//唯一id

    /* 常量 */
    public static byte minMemberCount = 1;//齐楚之战  组队成员数量最小值
    public static byte maxMemberCount = 5;//齐楚之战  组队成员数量最大值
    public static int CITY_PLAYER_SAVE_NUM = 200;//个城市保存的玩家数据数量（为镜像服务）
    public static int CITY_PLAYER_IMAGE_NUM = 100;//镜像数量限制
    /**
     * 阵营日捐献元宝最大值
     */
    public static int CampDonateMaxCount;
    /**
     * 阵营兑换单位,表示1元宝对应可兑换的声望数量
     */
    public static int campDonateUnit;
    /**
     * 齐楚大作战积分奖励
     * 《score，《itemid，count》》
     */
    public static Map<Integer, Map<Integer, Integer>> campActivity2ScoreMap;
    /**
     * 房间人数下限
     */
    public static int campActivity2RoomLowNum;
    /**
     * 房间合并间隔时间
     */
    public static int campActivity2Mergetime;
    /**
     * 匹配间隔时间
     */
    public static int campActivity2Matchtime;
    /**
     * 阵营大作战等级产品信息
     */
    private static Map<Integer, CampGrade> campGradeMap;
    /**
     * 阵营大作战
     * 《jobid，《level，Campgrade》》
     */
    private static Map<Integer, CampGrade> levelCampGradeMap;
    public static Map<Integer, CampSkill> campSkillMap;
    /**
     * 达到指定人数才会创建房间
     */
    public static int campActivity2MatchNum;
    /**
     * 官职额外加成
     */
    public static List<CampFightOfficerAttr> campFightOfficerAttrs;
    public static List<CampFightSingleScoreReward> campFightSingleScoreRewards;
    public static Map<Integer, Integer> campActivity2Defendaward;
    /**
     * 房间匹配的最大时间
     */
    public static int campActivity2MatchMaxTime;
    public static List<String> campActivity2Positions;
    /**
     * 每日单场奖励可领取次数
     */
    public static int campActivity2MaxSingleScoreTime;
    /**
     * 机器人削弱属性
     */
    public static int campActivity2AiAttr;
    /**
     * 房间赶人时间（分）
     */
    public static int campActivity2RoomExpelTime;
    /**
     * 房间销毁时间（分）
     */
    public static int campActivity2RoomDestroyTime;

    /**
     * 获取当前合理的平衡奖励
     *
     * @param currentScale
     * @return
     */
    public static CampEquilibrium getReasonableCampEquilibrium(float currentScale) {
        for (CampEquilibrium campEquilibrium : campEquilibriumList) {
            if (currentScale >= campEquilibrium.getMin() && currentScale <= campEquilibrium.getMax()) {
                return campEquilibrium;
            }
        }
        return null;
    }

    /**
     * 根据type和等级获取camplevevo
     *
     * @param campType
     * @param level
     * @return
     */
    public static CampLevelVo getCampLevelVo(Integer campType, Integer level) {
        return campTypeLevelMap.get(campType).get(level);
    }

    /**
     * 根据type获取所有城池
     *
     * @param campType
     * @return
     */
    public static List<CampCityVo> getAllCampCityList(Integer campType) {
        return campCityListMap.get(campType);
    }

    public static List<CampCityVo> getOpenedCities(int campType, int campLevel) {
        List<CampCityVo> allCampCityList = getAllCampCityList(campType);
        Collections.sort(allCampCityList);
        List<CampCityVo> openedCities = new ArrayList<>();
        for (CampCityVo campCityVo : allCampCityList) {
            if (campCityVo.getCondition() <= campLevel) {
                openedCities.add(campCityVo);
            }
        }
        return openedCities;
    }

    /**
     * 获取开放的公共官职
     *
     * @param campLevel
     * @return
     */
    public static List<CommonOfficerVo> getOpenedOfficer(int campLevel) {
        List<CommonOfficerVo> tmpCommonOfficerVoList = new ArrayList<>();
        CommonOfficerVo last = null;
        for (CommonOfficerVo commonOfficerVo : commonOfficerVoList) {
            if (commonOfficerVo.getCamplevel() <= campLevel) {
                tmpCommonOfficerVoList.add(commonOfficerVo);
                last = commonOfficerVo;
            }
        }
        CommonOfficerVo nextLevelCommonOfficerVo = last.getNextLevelCommonOfficerVo();
        if (nextLevelCommonOfficerVo != null) {
            tmpCommonOfficerVoList.add(last.getNextLevelCommonOfficerVo());
        }
        return tmpCommonOfficerVoList;
    }

    public static CampRankAwardVo getCampRankAward(int cityId, int rank) {
        List<CampRankAwardVo> campRankAwardVos = campRankArardMap.get(cityId);
        for (CampRankAwardVo campRankAwardVo : campRankAwardVos) {
            if (campRankAwardVo.isInThisSection(rank)) {
                return campRankAwardVo;
            }
        }
        return null;
    }

    public static int getEnemyCampType(int myCampType) {
        Iterator<Integer> iterator = CampManager.campAtrMap.keySet().iterator();
        Integer campEnemyType = 0;
        for (; iterator.hasNext(); ) {
            campEnemyType = iterator.next();
            if (campEnemyType != myCampType) {
                break;
            }
        }
        return campEnemyType;
    }

    public static Object lock = new Object();

    public static synchronized int getUid() {
        return enemyUid++;
    }

    public static void setCampGradeMap(Map<Integer, CampGrade> campGradeMap) {
        CampManager.campGradeMap = campGradeMap;
        Map<Integer, CampGrade> levelCampGradeMap = new HashMap<>();
        for (CampGrade campGrade : campGradeMap.values()) {
            int level = campGrade.getLevel();
            levelCampGradeMap.put(level, campGrade);
        }
        CampManager.levelCampGradeMap = levelCampGradeMap;
    }

    /**
     * 通过等级获取对应的升级信息
     *
     * @param level
     * @return
     */
    public static CampGrade getCampGradeByLevel(int level) {
        return levelCampGradeMap.get(level);
    }

    /**
     * 根据稀有官职获取阵营大作战属性加成
     *
     * @param rareOfficerId
     * @return
     */
    public static CampFightOfficerAttr getCampFightOfficerAttr(int rareOfficerId) {
        if (rareOfficerId == 0) {
            return null;
        }
        for (CampFightOfficerAttr campFightOfficerAttr : campFightOfficerAttrs) {
            if (campFightOfficerAttr.belongTo(rareOfficerId)) {
                return campFightOfficerAttr;
            }
        }
        return null;
    }

    public static List<CampFightSingleScoreReward> getCampFightSingleScoreRewards(int myScore) {
        List<CampFightSingleScoreReward> scoreRewards = new ArrayList<>();
        for (CampFightSingleScoreReward campFightSingleScoreReward : campFightSingleScoreRewards) {
            if (myScore >= campFightSingleScoreReward.getScore()) {
                scoreRewards.add(campFightSingleScoreReward);
            }
        }
        return scoreRewards;
    }

    public static String getRandomPosition4Activity2() {
        String position = RandomUtil.random(CampManager.campActivity2Positions, 1).get(0);
        return position;
    }
}
