package com.stars.services.offlinepvp;

import com.stars.ExcutorKey;
import com.stars.core.schedule.SchedulerManager;
import com.stars.core.db.DBUtil;
import com.stars.modules.offlinepvp.OfflinePvpManager;
import com.stars.modules.offlinepvp.prodata.OPMatchVo;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.offlinepvp.cache.OPEnemyCache;
import com.stars.services.summary.Summary;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by liuyuheng on 2016/10/8.
 */
public class OfflinePvpServiceActor extends ServiceActor implements OfflinePvpService {

    // 使用的基准等级和战力,<roleId, [level,fightscore]>
    private Map<Long, int[]> useStandard = new ConcurrentHashMap<>();
    // 记录今日基准等级和战力,用于明天使用<roleId, [level,fightscore]>
    private Map<Long, int[]> recordStandard = new ConcurrentHashMap<>();

    // 玩家等级桶, <level, ConcurrentHashMap<roleId, cache>>
    private Map<Integer, Map<Long, OPEnemyCache>> playerBuckets = new HashMap<>();
    // 玩家-等级映射,便于移除旧桶数据, <roleId, level>
    private Map<Long, Integer> bucketsIndexMapping = new ConcurrentHashMap<>();
    private Set<Long> dummySet = new HashSet<>();
    // 玩家对手缓存
    private Map<Long, Map<Byte, OPEnemyCache>> playerEnemyMap = new HashMap<>();

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd("offlinePvpService", this);
        synchronized (OfflinePvpServiceActor.class) {
            loadFromSummary();
            SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.OfflinePvp, new UpdatePlayerFromSummaryTask(), 10, 10, TimeUnit.MINUTES);
            SchedulerManager.scheduleAtFixedRate(new LoadPlayerFromSummaryTask(), 10, 10, TimeUnit.SECONDS);
        }
    }

    @Override
    public void printState() {

    }

    // 起服从常用数据捞数据
    public void loadFromSummary() throws SQLException, IllegalArgumentException {
        List<Long> chooseRoleIdList = new LinkedList<>();
        Random random = new Random();
        int loopCount = 0;
        int randomIndex = random.nextInt(5000);
        List<Long> roleIdList = DBUtil.queryList(DBUtil.DB_USER, Long.class, "select `roleid` from `rolesummary` group by roleid limit " + randomIndex + ",1000; ");
        while ((StringUtil.isEmpty(roleIdList) || roleIdList.size() < OfflinePvpConstant.loadSummaryLimit) && loopCount < 5) {
            randomIndex -= 1000;
            if (randomIndex < 0) {
                randomIndex = 0;
            }
            roleIdList = DBUtil.queryList(DBUtil.DB_USER, Long.class, "select `roleid` from `rolesummary` group by roleid limit " + randomIndex + ",1000; ");
            loopCount++;
        }
        if (loopCount >= 5) {
            roleIdList = DBUtil.queryList(DBUtil.DB_USER, Long.class, "select `roleid` from `rolesummary` group by roleid limit 0,1000; ");
        }

        // 增加选取策略
        int inteval = roleIdList.size() <= OfflinePvpConstant.loadSummaryLimit ? 1 :
                (roleIdList.size() / OfflinePvpConstant.loadSummaryLimit);
        Long id;
        for (int i = 0; i < roleIdList.size(); i = i + inteval) {
            id = roleIdList.get(i);
            if (id == null || bucketsIndexMapping.containsKey(roleIdList.get(i)) || dummySet.contains(id)) continue;
            chooseRoleIdList.add(roleIdList.get(i));
        }
        List<Summary> summaryList = ServiceHelper.summaryService().getAllSummary(chooseRoleIdList);
        for (Summary summary : summaryList) {
            if (summary.isDummy()) {
                dummySet.add(summary.getRoleId());
                continue;
            }
            OPEnemyCache enemyCache = new OPEnemyCache(String.valueOf(summary.getRoleId()));
            Map<String, FighterEntity> entityMap = FighterCreator.createBySummary(FighterEntity.CAMP_ENEMY, summary);
            if (entityMap == null) {
                dummySet.add(summary.getRoleId());
                continue;
            }
            enemyCache.setJobId(((RoleSummaryComponent) summary.getComponent("role")).getRoleJob());
            enemyCache.setEntityMap(entityMap);
            if (!bucketsIndexMapping.containsKey(summary.getRoleId())) {
                bucketsIndexMapping.put(summary.getRoleId(), enemyCache.getRoleLevel());
                Map<Long, OPEnemyCache> playerMap = playerBuckets.get(enemyCache.getRoleLevel());
                if (playerMap == null) {
                    playerMap = new ConcurrentHashMap<>();
                    playerBuckets.put(enemyCache.getRoleLevel(), playerMap);
                }
                playerMap.put(summary.getRoleId(), enemyCache);
                useStandard.put(summary.getRoleId(), new int[]{enemyCache.getRoleLevel(), enemyCache.getFightScore()});
            }
        }
    }

    @Override
    public void updateUseStandard(long roleId, int level, int fightScore) {
        int[] record = recordStandard.get(roleId);
        if (record == null) {
            recordStandard.put(roleId, new int[]{level, fightScore});
            return;
        }
        if (level > record[0]) {
            record[0] = level;
        }
        if (fightScore > record[1]) {
            record[1] = fightScore;
        }
    }

    @Override
    public int[] getMatchAndRewardId(long roleId, int jobId) {
        int standardLevel = OfflinePvpManager.initLevel;
        int standardFightScore = OfflinePvpManager.initFightScore;
        int[] standard = useStandard.get(roleId);
        if (standard != null) {
            standardLevel = standard[0];
            standardFightScore = standard[1];
        }
        OPMatchVo matchVo = getMatchVo(jobId, standardLevel, standardFightScore);
//        LogUtil.info("test log:roleId={}, standardlevel={}, 匹配对手用标准战力={}", roleId, standardLevel, standardFightScore);
        return new int[]{matchVo.getMatchId(), standardLevel, standardFightScore};
    }

    @Override
    public Map<Byte, OPEnemyCache> getMatchEnemys(long roleId, int matchVoId, int standardLevel) {
        if (playerEnemyMap.containsKey(roleId))
            return playerEnemyMap.get(roleId);
        return refreshMatchEnemys(roleId, matchVoId, standardLevel);
    }

    @Override
    public Map<Byte, OPEnemyCache> refreshMatchEnemys(long roleId, int matchVoId, int standardLevel) {
        OPMatchVo matchVo = OfflinePvpManager.getOpMatchVo(matchVoId);
        Map<Byte, OPEnemyCache> returnEnemys = new HashMap<>();// 返回列表
        Set<String> exceptFilter = new HashSet<>();// 除外Id列表
        exceptFilter.add(String.valueOf(roleId));
        for (byte index = 1; index <= OfflinePvpManager.matchEnemyNum; index++) {
            int[] matchFightScore = matchVo.getMatchFightScore(index);
            // 先匹配玩家
            OPEnemyCache matchResult = matchEnemy(standardLevel, matchFightScore, OfflinePvpConstant.MATCH_TYPE_PLAYER,
                    exceptFilter);
            if (matchResult != null) {
                returnEnemys.put(index, matchResult);
                exceptFilter.add(matchResult.getUniqueId());
                continue;
            }
            // 匹配机器人
            matchResult = matchEnemy(standardLevel, matchFightScore, OfflinePvpConstant.MATCH_TYPE_ROBOT, exceptFilter);
            if (matchResult != null) {
                returnEnemys.put(index, matchResult);
                exceptFilter.add(matchResult.getUniqueId());
            }
        }
        // 机器人替换玩家
        replacePlayerToRobot(returnEnemys, standardLevel, matchVo, exceptFilter);
        playerEnemyMap.put(roleId, returnEnemys);
        return returnEnemys;
    }

    @Override
    public void dailyReset() {
        useStandard = recordStandard;
        recordStandard = new ConcurrentHashMap<>();
    }


    /**
     * 有几个需要注意的地方
     * 匹配没有结果的时候会返回空的list，匹配结果不足数量的时候也只会返回结果数量
     * 返回对象是OPEnemyCache，需要业务自己去转一下
     * 里面的FightEntity的阵营是 2 enemy，也需要根据业务转一下
     */
    @Override
    public List<OPEnemyCache> executeMatch(int matchLevel, int jobId, int count, List<Long> exception) {
        List<OPEnemyCache> result = new LinkedList<>();
        List<OPEnemyCache> sameJobList;
        List<OPEnemyCache> otherJobList;
        for (int index = 0; index < count; index++) {
            for (int i = 0; i < OfflinePvpConstant.levelMatchLimit; i++) {
                int useLevel = matchLevel - i;
                sameJobList = new LinkedList<>();
                otherJobList = new LinkedList<>();
                Map<Long, OPEnemyCache> bucketMap = playerBuckets.get(useLevel);
                if (bucketMap == null)
                    continue;
                for (OPEnemyCache enemyCache : bucketMap.values()) {
                    if (exception.contains(Long.parseLong(enemyCache.getUniqueId()))) {
                        continue;
                    }
                    if (!result.contains(enemyCache)) {
                        if (enemyCache.getModelId() == jobId) {
                            sameJobList.add(enemyCache);
                        } else {
                            otherJobList.add(enemyCache);
                        }
                    }
                }
                // 优先取不同的职业
                if (!otherJobList.isEmpty()) {
                    result.add(otherJobList.get(new Random().nextInt(otherJobList.size())));
                    break;
                } else if (!sameJobList.isEmpty()) {
                    result.add(sameJobList.get(new Random().nextInt(sameJobList.size())));
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 获得对应匹配vo
     *
     * @param jobId
     * @param standardLv
     * @param standardFightScore
     * @return
     */
    private OPMatchVo getMatchVo(int jobId, int standardLv, int standardFightScore) {
        OPMatchVo result = null;
        int maxFightScore = 0;
        for (OPMatchVo matchVo : OfflinePvpManager.jobMatchVoMap.get(jobId)) {
            if (matchVo.getLevelMin() <= standardLv && standardLv <= matchVo.getLevelMax() &&
                    matchVo.getFightScoreMin() <= standardFightScore &&
                    standardFightScore <= matchVo.getFightScoreMax()) {
                return matchVo;
            }
            if (matchVo.getFightScoreMax() > maxFightScore) {
                maxFightScore = matchVo.getFightScoreMax();
                result = matchVo;
            }
        }
        return result;
    }

    /**
     * 匹配对手
     *
     * @param standardLevel
     * @param matchFightScore
     * @param matchType       1=玩家;2=机器人
     * @return
     */
    private OPEnemyCache matchEnemy(int standardLevel, int[] matchFightScore, byte matchType, Set<String> exceptFilter) {
        for (int i = 0; i < OfflinePvpConstant.levelMatchLimit; i++) {
            int useLevel = standardLevel - i;
            if (useLevel < 1)
                break;
            Map<? extends Object, OPEnemyCache> enemyCacheMap;
            if (matchType == OfflinePvpConstant.MATCH_TYPE_PLAYER) {// 匹配玩家
                enemyCacheMap = playerBuckets.get(useLevel);
            } else {// 匹配机器人
                enemyCacheMap = OfflinePvpManager.robotBuckets.get(useLevel);
            }
            if (StringUtil.isEmpty(enemyCacheMap))
                continue;
            // 备选list
            List<OPEnemyCache> chooseList = new LinkedList<>();
            for (OPEnemyCache enemyCache : enemyCacheMap.values()) {
                if (matchFightScore[0] <= enemyCache.getFightScore()
                        && enemyCache.getFightScore() <= matchFightScore[1]
                        && !exceptFilter.contains(enemyCache.getUniqueId())) {
                    chooseList.add(enemyCache);
                }
            }
            if (!chooseList.isEmpty()) {
                return chooseList.get(new Random().nextInt(chooseList.size()));
            }
        }
        if (matchType == OfflinePvpConstant.MATCH_TYPE_ROBOT) {
            LogUtil.error("没有level∈[{},{}],fightscore∈[{},{}]的机器人数据", standardLevel,
                    Math.max(0, standardLevel - OfflinePvpConstant.levelMatchLimit + 1), matchFightScore[0],
                    matchFightScore[1], new IllegalArgumentException());
        }
        return null;
    }

    /**
     * 机器人替换玩家策略
     *
     * @param resultMap
     * @param standardLevel
     * @param matchVo
     */
    private void replacePlayerToRobot(Map<Byte, OPEnemyCache> resultMap, int standardLevel, OPMatchVo matchVo,
                                      Set<String> exceptFilter) {
        int addRobotNum = OfflinePvpManager.getAddRobotNum(standardLevel);// 段数
        if (addRobotNum == 0)
            return;
        int perNum = resultMap.size() / addRobotNum;// 每段个数
        int remainNum = resultMap.size() % addRobotNum;// 余数
        int flag = 1;
        for (int k = 1; k <= addRobotNum; k++) {
            int limit = flag + perNum;
            if (k == addRobotNum)
                limit = limit + remainNum;
            boolean hasRobot = false;
            for (byte i = (byte) flag; i < limit; i++) {
                if (resultMap.get(i).getUniqueId().contains("r")) {
                    hasRobot = true;
                    break;
                }
            }
            // 不存在机器人则随机选一个替换成机器人
            if (!hasRobot) {
                byte index = (byte) (new Random().nextInt(perNum) + flag);
                OPEnemyCache enemyCache = matchEnemy(standardLevel, matchVo.getMatchFightScore(index),
                        OfflinePvpConstant.MATCH_TYPE_ROBOT, exceptFilter);
                resultMap.put(index, enemyCache);
                exceptFilter.add(enemyCache.getUniqueId());
            }
            flag = flag + perNum;
        }
    }

    class UpdatePlayerFromSummaryTask implements Runnable {
        @Override
        public void run() {
            try {
                List<Summary> summaryList = ServiceHelper.summaryService().getAllOnlineSummary();
                for (Summary summary : summaryList) {
                    OPEnemyCache enemyCache = new OPEnemyCache(String.valueOf(summary.getRoleId()));
                    RoleSummaryComponent rsc = (RoleSummaryComponent) summary.getComponent("role");
                    enemyCache.setJobId(rsc.getRoleJob());
                    Map<String, FighterEntity> entityMap = FighterCreator.createBySummary(FighterEntity.CAMP_ENEMY, summary);
                    if (entityMap == null)
                        continue;
                    enemyCache.setEntityMap(entityMap);
                    // 移除旧桶的数据
                    if (bucketsIndexMapping.containsKey(summary.getRoleId())) {
                        int level = bucketsIndexMapping.get(summary.getRoleId());
                        playerBuckets.get(level).remove(summary.getRoleId());
                    }
                    // 数据加入新桶
                    Map<Long, OPEnemyCache> playerMap = playerBuckets.get(enemyCache.getRoleLevel());
                    if (playerMap == null) {
                        playerMap = new ConcurrentHashMap<>();
                        playerBuckets.put(enemyCache.getRoleLevel(), playerMap);
                    }
                    playerMap.put(summary.getRoleId(), enemyCache);
                    bucketsIndexMapping.put(summary.getRoleId(), enemyCache.getRoleLevel());
                }
            } catch (Exception e) {
                LogUtil.error("", e);
            }
        }
    }

    class LoadPlayerFromSummaryTask implements Runnable {
        @Override
        public void run() {
            if (bucketsIndexMapping.size() < 2000) {
                try {
                    loadFromSummary();
                } catch (Exception e) {
                    LogUtil.error("OfflinePvpServiceActor loadFromSummary exception:" + e.getMessage(), e);
                }
            }
        }
    }
}
