package com.stars.modules.offlinepvp;

import com.stars.modules.offlinepvp.prodata.OPMatchVo;
import com.stars.modules.offlinepvp.prodata.OPRewardVo;
import com.stars.modules.offlinepvp.prodata.OPRobotVo;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.services.offlinepvp.cache.OPEnemyCache;
import com.stars.util.RandomUtil;

import java.util.*;

/**
 * Created by liuyuheng on 2016/9/30.
 */
public class OfflinePvpManager {
    // 每日购买挑战次数
    public static int buyChallengeLimit;
    //每次购买的挑战次数
    public static int perBuyChallengeNum;
    // 购买挑战次数消耗, <购买次数, <itemId, number>>
    public static Map<Integer, Map<Integer, Integer>> buyChallengeCost = new HashMap<>();
    // 默认购买消耗次数(找不到次数时使用)
    public static int initBuyChallegeCostIndex;
    // 每日购买刷新次数
    public static int buyRefreshLimit;
    // 购买刷新次数消耗, <购买次数, <itemId, number>>
    public static Map<Integer, Map<Integer, Integer>> buyRefreshCost = new HashMap<>();
    // 默认购买消耗次数(找不到次数时使用)
    public static int initBuyRefreshCostIndex;
    // 匹配初始等级
    public static int initLevel;
    // 匹配初始战力
    public static int initFightScore;
    // 匹配对手数量
    public static int matchEnemyNum;
    // 每日初始挑战次数
    public static int initChallengeLimit;
    // 每日初始刷新次数
    public static int initRefreshLimit;
    // 插入机器人数量, <levelLimit, number>
    public static Map<Integer, Integer> addRobotNum = new HashMap<>();
    // 调用stageId, <levelLimit, stageId>
    public static Map<Integer, Integer> useStage = new LinkedHashMap<>();
    // 自动刷新对手与每日重置时间差值(ms)
    public static long[] autoRefreshInterval;

    // 机器人数据配置,<robotId, vo>
    public static Map<Integer, OPRobotVo> robotVoMap = new HashMap<>();
    // 机器人等级桶, <level, <robotId, cache>>
    public static Map<Integer, Map<Integer, OPEnemyCache>> robotBuckets = new HashMap<>();
    // 离线pvp奖励vo,<level, vo>
    public static Map<Integer, OPRewardVo> opRewardVoMap = new HashMap<>();
    // 匹配vo, <id, vo>
    public static Map<Integer, OPMatchVo> matchVoMap = new HashMap<>();
    // 职业匹配vo, <jobId, List<vo>>
    public static Map<Integer, List<OPMatchVo>> jobMatchVoMap = new HashMap<>();

    public static int getAddRobotNum(int roleLevel) {
        int flag = 1;
        for (Map.Entry<Integer, Integer> entry : addRobotNum.entrySet()) {
            if (flag <= roleLevel && roleLevel <= entry.getKey()) {
                return entry.getValue();
            }
            flag = flag + entry.getKey();
        }
        return 0;
    }

    public static OPRewardVo getOPRewardVo(int level) {
        return opRewardVoMap.get(level);
    }

    public static OPMatchVo getOpMatchVo(int matchId) {
        return matchVoMap.get(matchId);
    }

    public static Map<Integer, Integer> getBuyRefreshCost(int index) {
        if (!buyRefreshCost.containsKey(index))
            index = initBuyRefreshCostIndex;
        return buyRefreshCost.get(index);
    }

    public static Map<Integer, Integer> getBuyChallengeCost(int index) {
        if (!buyChallengeCost.containsKey(index))
            index = initBuyChallegeCostIndex;
        return buyChallengeCost.get(index);
    }

    public static List<FighterEntity> getRobotByLevel(int level, int count) {
        List<FighterEntity> entityList = new LinkedList<>();
        int countDownLevel = level;
        while (entityList.size() < count) {
            FighterEntity entity = getRobotByLevel(level);
            for (FighterEntity fighterEntity : entityList) {
                if (fighterEntity.getName().equals(entity.getName())) {
                    countDownLevel = Math.max(1, countDownLevel - 1);
                    entity.setName(getRobotByLevel(countDownLevel).getName());
                    break;
                }
            }
            entityList.add(entity);
        }
        return entityList;
    }

    public static FighterEntity getRobotByLevel(int level) {
        Map<Integer, OPEnemyCache> map = robotBuckets.get(level);
        OPEnemyCache[] arr = map.values().toArray(new OPEnemyCache[map.size()]);
        Random r = new Random();
        int index = r.nextInt(arr.length);
        OPEnemyCache cache = arr[index];
        return cache.getEntityMap().get(cache.getUniqueId()).copy();
    }

    public static List<FighterEntity> getRandomRobots(int count) {
        List<FighterEntity> entityList = new LinkedList<>();
        if (count > 0) {
            List<OPRobotVo> randomRobotList = RandomUtil.random(robotVoMap.values(), count);
            for (OPRobotVo opRobotVo : randomRobotList) {
                Map<String, FighterEntity> robotMap = FighterCreator.createRobot(FighterEntity.CAMP_ENEMY, opRobotVo);
                FighterEntity fighterEntity = robotMap.get("r" + opRobotVo.getRobotId());
                fighterEntity.uniqueId = opRobotVo.getRobotId() + "";
                fighterEntity.setIsRobot(true);
                fighterEntity.setFighterType(FighterEntity.TYPE_PLAYER);
                entityList.add(fighterEntity);
            }
        }
        return entityList;
    }
}
