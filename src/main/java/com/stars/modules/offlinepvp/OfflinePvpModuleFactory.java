package com.stars.modules.offlinepvp;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.offlinepvp.event.OfflinePvpVictoryEvent;
import com.stars.modules.offlinepvp.listener.OfflinePvpStandardListerner;
import com.stars.modules.offlinepvp.listener.OfflinePvpVictoryListener;
import com.stars.modules.offlinepvp.prodata.OPMatchVo;
import com.stars.modules.offlinepvp.prodata.OPRewardVo;
import com.stars.modules.offlinepvp.prodata.OPRobotVo;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.services.offlinepvp.cache.OPEnemyCache;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by liuyuheng on 2016/9/30.
 */
public class OfflinePvpModuleFactory extends AbstractModuleFactory<OfflinePvpModule> {
    public OfflinePvpModuleFactory() {
        super(new OfflinePvpPacketSet());
    }

    @Override
    public OfflinePvpModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new OfflinePvpModule(id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        loadCommondefine();
        loadRobotVo();
        loadMatchVo();
        loadRewardVo();
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(OfflinePvpVictoryEvent.class, new OfflinePvpVictoryListener((OfflinePvpModule) module));
        eventDispatcher.reg(FightScoreChangeEvent.class, new OfflinePvpStandardListerner((OfflinePvpModule) module));
    }

    private void loadRobotVo() throws SQLException {
        String sql = "select * from `offlinepvprobot`; ";
        Map<Integer, OPRobotVo> robotVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "robotid", OPRobotVo.class, sql);
        Map<Integer, Map<Integer, OPEnemyCache>> map = new HashMap<>();
        for (OPRobotVo robotVo : robotVoMap.values()) {
            Map<Integer, OPEnemyCache> robotMap = map.get(robotVo.getRobotLevel());
            if (robotMap == null) {
                robotMap = new HashMap<>();
                map.put(robotVo.getRobotLevel(), robotMap);
            }
            OPEnemyCache enemyCache = new OPEnemyCache("r" + robotVo.getRobotId());
            enemyCache.setJobId(robotVo.getJobId());
            enemyCache.setEntityMap(FighterCreator.createRobot(FighterEntity.CAMP_ENEMY, robotVo));
            robotMap.put(robotVo.getRobotId(), enemyCache);
        }

        OfflinePvpManager.robotVoMap = robotVoMap;
        OfflinePvpManager.robotBuckets = map;
    }

    private void loadMatchVo() throws SQLException {
        String sql = "select * from `offlinepvpmatch`; ";
        Map<Integer, OPMatchVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "matchid", OPMatchVo.class, sql);
        Map<Integer, List<OPMatchVo>> jobMatchmap = new HashMap<>();
        for (OPMatchVo vo : map.values()) {
            List<OPMatchVo> list = jobMatchmap.get(vo.getJobId());
            if (list == null) {
                list = new LinkedList<>();
                jobMatchmap.put(vo.getJobId(), list);
            }
            list.add(vo);
        }

        OfflinePvpManager.matchVoMap = map;
        OfflinePvpManager.jobMatchVoMap = jobMatchmap;
    }

    private void loadRewardVo() throws SQLException {
        String sql = "select * from `offlinepvpreward`; ";
        OfflinePvpManager.opRewardVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "level", OPRewardVo.class, sql);
    }

    private void loadCommondefine() throws Exception {
        String temp = DataManager.getCommConfig("offlinepvpmatch_buychance_max");
        OfflinePvpManager.buyChallengeLimit = Integer.parseInt(temp);
        temp = DataManager.getCommConfig("offlinepvpmatch_buychance_oncenum");
        OfflinePvpManager.perBuyChallengeNum = Integer.parseInt(temp);
        temp = DataManager.getCommConfig("offlinepvpmatch_buychance_cost");
        OfflinePvpManager.buyChallengeCost = parseCostMap(temp);
        OfflinePvpManager.initBuyChallegeCostIndex = getInitCostIndex(OfflinePvpManager.buyChallengeCost.keySet());
        temp = DataManager.getCommConfig("offlinepvpmatch_buyfresh_max");
        OfflinePvpManager.buyRefreshLimit = Integer.parseInt(temp);
        temp = DataManager.getCommConfig("offlinepvpmatch_buyfresh_cost");
        OfflinePvpManager.buyRefreshCost = parseCostMap(temp);
        OfflinePvpManager.initBuyRefreshCostIndex = getInitCostIndex(OfflinePvpManager.buyRefreshCost.keySet());
        temp = DataManager.getCommConfig("offlinepvpmatch_origndata");
        int[] tempArray = StringUtil.toArray(temp, int[].class, '+');
        OfflinePvpManager.initLevel = tempArray[0];
        OfflinePvpManager.initFightScore = tempArray[1];
        temp = DataManager.getCommConfig("offlinepvpmatch_rolenum");
        OfflinePvpManager.matchEnemyNum = Integer.parseInt(temp);
        temp = DataManager.getCommConfig("offlinepvpmatch_refreshtime");
        long[] interval = StringUtil.toArray(temp, long[].class, '+');
        OfflinePvpManager.autoRefreshInterval = new long[]{interval[0] * 60 * 1000L, interval[1] * 60 * 1000L};
        temp = DataManager.getCommConfig("offlinepvpmatch_chance");
        OfflinePvpManager.initChallengeLimit = Integer.parseInt(temp);
        temp = DataManager.getCommConfig("offlinepvpmatch_robotnum");
        OfflinePvpManager.addRobotNum = StringUtil.toLinkedHashMap(temp, Integer.class, Integer.class, '+', ',');
        temp = DataManager.getCommConfig("offlinepvpmatch_stageid");
        OfflinePvpManager.useStage = StringUtil.toLinkedHashMap(temp, Integer.class, Integer.class, '+', ',');
        temp = DataManager.getCommConfig("offlinepvpmatch_refreshchance_origin");
        OfflinePvpManager.initRefreshLimit = Integer.parseInt(temp);
    }

    private Map<Integer, Map<Integer, Integer>> parseCostMap(String str) throws Exception {
        Map<Integer, Map<Integer, Integer>> map = new HashMap<>();
        for (String temp : str.split("\\|")) {
            String[] delta = temp.split(",");
            int number = Integer.parseInt(delta[0]);
            Map<Integer, Integer> costMap = map.get(number);
            if (costMap == null) {
                costMap = new HashMap<>();
                map.put(number, costMap);
            }
            for (int i = 1; i < delta.length; i++) {
                costMap.putAll(StringUtil.toMap(delta[i], Integer.class, Integer.class, '+', ','));
            }
        }
        return map;
    }

    private int getInitCostIndex(Set<Integer> set) {
        int max = 0;
        for (int index:set) {
            if (index > max)
                max = index;
        }
        return max;
    }
}
