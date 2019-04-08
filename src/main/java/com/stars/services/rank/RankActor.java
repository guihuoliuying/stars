package com.stars.services.rank;

import com.stars.core.dao.DbRowDao;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.dragonboat.DragonBoatManager;
import com.stars.modules.rank.RankManager;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.rank.imp.BestCpVoterRank;
import com.stars.services.rank.imp.CampRoleReputationRank;
import com.stars.services.rank.imp.DragonBoatRank;
import com.stars.services.rank.imp.FightScoreRank;
import com.stars.services.rank.prodata.RankAwardVo;
import com.stars.services.rank.prodata.RankDisplayVo;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.RoleRankPo;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by liuyuheng on 2016/8/22.
 */
public class RankActor extends ServiceActor implements RankService {
    static volatile boolean isLoadData = false;
    /* 每种排行榜一个子类实例,初始化的时候创建好 */
    private Map<Integer, AbstractRank> rankMap;

    private DbRowDao rankDao = new DbRowDao();

    public static long LastFlushFighting2FightingMaster;

    public static long FlushFighting2FightingMasterDis = 600000l;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd("rankService", this);
        registerRank();
        synchronized (RankActor.class) {
            if (!isLoadData) {
                loadUserData();// 加载排行数据
                isLoadData = true;
            }
        }
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.RankUpdate, new TimingTask(), 0, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void printState() {

    }

    @Override
    public void updateRank(byte rankType, AbstractRankPo rankPo) {
        List<Integer> rankIdList = RankConstant.rankTypeMap.get(rankType);
        updateRoleRank(rankPo, rankIdList);
    }

    @Override
    public void updateRank4BestCP(byte rankType, AbstractRankPo rankPo, int cpId) {
        if (rankType != RankConstant.RANK_TYPE_BEST_CP_VOTER) {
            return;
        }
        List<Integer> rankIdList = RankConstant.rankTypeMap.get(rankType);
        updateRoleRank4BestCP(rankPo, rankIdList, cpId);
    }


    @Override
    public void updateRank4DragonBoat(byte rankType, AbstractRankPo rankPo, Long stageTime) {
        if (rankType != RankConstant.RANK_TYPE_DRAGON_BOAT) {
            return;
        }
        List<Integer> rankIdList = RankConstant.rankTypeMap.get(rankType);
        updateRoleRank4DragonBoat(rankPo, rankIdList, stageTime);
    }

    @Override
    public List<Long> getAllStageTimeList4DragonBoat(int rankId) {
        if (rankId != RankConstant.RANKID_DRAGON_BOAT) {
            return null;
        }
        DragonBoatRank dragonBoatRank = (DragonBoatRank) rankMap.get(rankId);
        List<Long> allStageTimeList = dragonBoatRank.getAllStageTimeList();
        return allStageTimeList;
    }

    /**
     * 更新个人排行榜
     *
     * @param rankPo
     * @param rankIdList
     */
    private void updateRoleRank4DragonBoat(AbstractRankPo rankPo, List<Integer> rankIdList, Long stageTime) {
        boolean insert = true;
        for (int rankId : rankIdList) {
            AbstractRank abstractRank = rankMap.get(rankId);
            if (abstractRank == null)
                continue;
            AbstractRankPo newPo = rankPo.copy();
            newPo.setRankId(abstractRank.rankId);
            AbstractRankPo oldPo;
            DragonBoatRank dragonBoatRank = (DragonBoatRank) abstractRank;
            oldPo = dragonBoatRank.getRankPo(stageTime, newPo.getUniqueId());
            if (oldPo != null) {
                insert = false;
            }
            abstractRank.addTreeSet(oldPo, newPo);
        }
        if (insert) {
            rankDao.delete(rankPo);
            rankDao.insert(rankPo);
        } else {
            rankDao.update(rankPo);
        }
    }

    /**
     * 更新个人排行榜
     *
     * @param rankPo
     * @param rankIdList
     */
    private void updateRoleRank(AbstractRankPo rankPo, List<Integer> rankIdList) {
        boolean insert = true;
        for (int rankId : rankIdList) {
            AbstractRank abstractRank = rankMap.get(rankId);
            if (abstractRank == null)
                continue;
            AbstractRankPo newPo = rankPo.copy();
            newPo.setRankId(abstractRank.rankId);
            AbstractRankPo oldPo = abstractRank.getRankPo(newPo.getUniqueId());
            if (oldPo != null) {
                insert = false;
            }
            abstractRank.addTreeSet(oldPo, newPo);
        }
        if (insert) {
            rankDao.delete(rankPo);
            rankDao.insert(rankPo);
        } else {
            rankDao.update(rankPo);
        }
    }

    /**
     * 更新个人排行榜
     *
     * @param rankPo
     * @param rankIdList
     */
    private void updateRoleRank4BestCP(AbstractRankPo rankPo, List<Integer> rankIdList, int cpId) {
        boolean insert = true;
        for (int rankId : rankIdList) {
            AbstractRank abstractRank = rankMap.get(rankId);
            if (abstractRank == null)
                continue;
            AbstractRankPo newPo = rankPo.copy();
            newPo.setRankId(abstractRank.rankId);
            AbstractRankPo oldPo;
            BestCpVoterRank bestCpVoterRank = (BestCpVoterRank) abstractRank;
            oldPo = bestCpVoterRank.getRankPo(cpId, newPo.getUniqueId());
            if (oldPo != null) {
                insert = false;
            }
            abstractRank.addTreeSet(oldPo, newPo);
        }
        if (insert) {
            rankDao.delete(rankPo);
            rankDao.insert(rankPo);
        } else {
            rankDao.update(rankPo);
        }
    }

    /**
     * 更新个人排行榜
     *
     * @param rankPo
     * @param rankIdList
     * @param oldCityId
     */
    private void updateRoleRank4CampCity(AbstractRankPo rankPo, List<Integer> rankIdList, int newCityId, int oldCityId) {
        boolean insert = true;
        for (int rankId : rankIdList) {
            AbstractRank abstractRank = rankMap.get(rankId);
            if (abstractRank == null)
                continue;
            AbstractRankPo newPo = rankPo.copy();
            newPo.setRankId(abstractRank.rankId);
            AbstractRankPo oldPo;
            CampRoleReputationRank campRoleReputationRank = (CampRoleReputationRank) abstractRank;
            if (newCityId == oldCityId) {
                oldPo = campRoleReputationRank.getRankPo(newCityId, newPo.getUniqueId());
            } else {
                oldPo = campRoleReputationRank.getRankPo(oldCityId, newPo.getUniqueId());
                abstractRank.removeCacheRank(rankPo.getUniqueId(), oldPo);
                oldPo = oldPo.copy();
            }
            if (oldPo != null) {
                insert = false;
            }
            abstractRank.addTreeSet(oldPo, newPo);
        }
        if (insert) {
            rankDao.delete(rankPo);
            rankDao.insert(rankPo);
        } else {
            rankDao.update(rankPo);
        }
    }

    @Override
    public void removeRank(int rankId, long uniqueId, AbstractRankPo rankPo) {
        AbstractRank abstractRank = rankMap.get(rankId);
        if (abstractRank != null) {
            AbstractRankPo newPo = rankPo.copy();
            abstractRank.removeCacheRank(uniqueId, newPo);
            rankDao.delete(rankPo);
        }
    }

    @Override
    public void updateRank4CampCity(byte rankType, AbstractRankPo rankPo, int newCityId, int oldCityId) {
        if (rankType != RankConstant.RANK_TYPE_CAMP_CITY_REPUTATION) {
            return;
        }
        List<Integer> rankIdList = RankConstant.rankTypeMap.get(rankType);
        updateRoleRank4CampCity(rankPo, rankIdList, newCityId, oldCityId);
    }

    @Override
    public void sendRankList(int rankId, long uniqueId) {
        AbstractRank rankImp = rankMap.get(rankId);
        List<AbstractRankPo> list = new LinkedList<>();
        RankDisplayVo rankDisplayVo = RankManager.getRankDisplayVo(rankId);
        if (rankDisplayVo == null) return;
        int count = 0;
        Iterator<AbstractRankPo> iterator = rankImp.getTreeSet().iterator();
        while (iterator.hasNext() && count < rankDisplayVo.getDisCount()) {
            list.add(iterator.next());
            count++;
        }
        AbstractRankPo selfRankPo = rankImp.getRankPo(uniqueId);
        if (selfRankPo != null) {
            list.add(rankImp.getRankPo(uniqueId));
        }
        // 交给子类send
        rankImp.sendRankList(uniqueId, list);
        for (AbstractRankPo rankPo : list) {
            RoleRankPo roleRankPo = (RoleRankPo) rankPo;
            if (SpecialAccountManager.isSpecialAccount(roleRankPo.getRoleId())) {
                ServiceHelper.roleService().notice(uniqueId, new SpecialAccountEvent(uniqueId, "出现在" + rankId + "排行榜上", true));
            }
        }
    }


    @Override
    public void sendRankList(int rankId, long roleId, long familyId) {
        AbstractRank rankImp = rankMap.get(rankId);
        List<AbstractRankPo> list = new LinkedList<>();
        RankDisplayVo rankDisplayVo = RankManager.getRankDisplayVo(rankId);
        if (rankDisplayVo == null) return;
        int count = 0;
        Iterator<AbstractRankPo> iterator = rankImp.getTreeSet().iterator();
        while (iterator.hasNext() && count < rankDisplayVo.getDisCount()) {
            list.add(iterator.next());
            count++;
        }
        AbstractRankPo selfRankPo = rankImp.getRankPo(familyId);
        if (selfRankPo != null) {
            list.add(rankImp.getRankPo(familyId));
        }
        rankImp.sendRankList(roleId, list);
    }

    @Override
    public void sendRankList4BestCPVoter(int rankId, long uniqueId, int cpId) {
        if (rankId != RankConstant.RANKID_BEST_CP_VOTER) {
            return;
        }
        AbstractRank rankImp = rankMap.get(rankId);
        List<AbstractRankPo> list = new LinkedList<>();
        int count = 0;
        int disCount = 99;
        Iterator<AbstractRankPo> iterator = null;
//        disCount = BestCPManager.bestCPRankDisplayMap.get(cpId);
        BestCpVoterRank bestCpVoterRank = (BestCpVoterRank) rankImp;
        TreeSet treeSet = bestCpVoterRank.getTreeSet(cpId);
        if (treeSet != null) {
            iterator = treeSet.iterator();
        }
        while (iterator != null && iterator.hasNext() && count < disCount) {
            list.add(iterator.next());
            count++;
        }
        AbstractRankPo selfRankPo = null;
        selfRankPo = bestCpVoterRank.getRankPo(cpId, uniqueId);
        if (selfRankPo != null) {
            list.add(selfRankPo);
        }
        // 交给子类send
        rankImp.sendRankList(uniqueId, list);
    }

    @Override
    public void sendRankList4DragonBoat(int rankId, long uniqueId) {
        if (rankId != RankConstant.RANKID_DRAGON_BOAT) {
            return;
        }
        AbstractRank rankImp = rankMap.get(rankId);
        int disCount = 0;

        disCount = DragonBoatManager.rankRewardMap.size();
        DragonBoatRank dragonBoatRank = (DragonBoatRank) rankImp;
        Map<Long, TreeSet<AbstractRankPo>> treeSetMap = dragonBoatRank.getTreeSetMap();
        Map<Long, List<AbstractRankPo>> rankMap = new HashMap<>();
        List<Long> keys = new ArrayList(treeSetMap.keySet());
        Collections.sort(keys);
        long maxTime = 0;
        if (keys.size() > 0) {
            maxTime = keys.get(keys.size() - 1);
            if (maxTime <= System.currentTimeMillis() / 1000 * 1000) {
                maxTime = -1;
            }
        }
        for (Map.Entry<Long, TreeSet<AbstractRankPo>> entry : treeSetMap.entrySet()) {
            if (maxTime == entry.getKey()) {
                continue;
            }
            int count = 0;
            Iterator<AbstractRankPo> iterator = null;
            List<AbstractRankPo> list = new LinkedList<>();
            TreeSet<AbstractRankPo> treeSet = entry.getValue();
            if (treeSet != null) {
                iterator = treeSet.iterator();
            }
            while (iterator != null && iterator.hasNext() && count < disCount) {
                list.add(iterator.next());
                count++;
            }
            rankMap.put(entry.getKey(), list);

        }
        // 交给子类send
        dragonBoatRank.sendRankMap(uniqueId, rankMap);
    }


    @Override
    public AbstractRankPo getRank(int rankId, long uniqueId) {
        AbstractRank rankImp = rankMap.get(rankId);
        return rankImp.getRankPo(uniqueId);
    }

    @Override
    public AbstractRankPo getRank(int rankId, long uniqueId, Object... args) {
        AbstractRank rankImp = rankMap.get(rankId);
        switch (rankId) {
            case RankConstant.RANKID_BEST_CP_VOTER: {
                BestCpVoterRank bestCpVoterRank = (BestCpVoterRank) rankImp;
                Integer cpId = (Integer) args[0];
                return bestCpVoterRank.getRankPo(cpId, uniqueId);
            }
            case RankConstant.RANKID_DRAGON_BOAT: {
                DragonBoatRank dragonBoatRank = (DragonBoatRank) rankImp;
                Long stageTime = (Long) args[0];
                return dragonBoatRank.getRankPo(stageTime, uniqueId);
            }
            case RankConstant.RANKID_CAMP_CITY_REPUTATION: {
                CampRoleReputationRank campRoleReputationRank = (CampRoleReputationRank) rankImp;
                int cityId = (int) args[0];
                return campRoleReputationRank.getRankPo(cityId, uniqueId);
            }
            default: {
                return rankImp.getRankPo(uniqueId);
            }
        }
    }

    @Override
    public long getRankMatching(long roleId, int rankId, int section) {
        return 0;
    }

    @Override
    public void dailyReset() {
        for (AbstractRank abstractRank : rankMap.values()) {
            abstractRank.dailyReset(rankDao);
        }
    }

    @Override
    public void rewardHandler(byte rewardType) {
        List<Integer> rankIdList = RankManager.getRankIdByRewardType(rewardType);
        if (rankIdList.isEmpty())
            return;
        for (int rankId : rankIdList) {
            reward(rankId);
        }
    }

    @Override
    public void offline(byte rankType, long uniqueId) {
        List<Integer> rankIdList = RankConstant.rankTypeMap.get(rankType);

        for (int rankId : rankIdList) {
            AbstractRank rank = rankMap.get(rankId);
            if (rank == null) {
                continue;
            }
            AbstractRankPo rankPo = rank.getRankPo(uniqueId);
            if (rankPo != null && rank.treeSet.contains(rankPo)) {
                rank.removeCacheRank(uniqueId);
            }
        }
    }

    @Override
    public void save() {
        rankDao.flush();
    }

    private void loadUserData() throws SQLException {
        for (byte rankType : RankConstant.rankTypeMap.keySet()) {
            if (rankType == RankConstant.RANK_TYPE_PERSON) {
                List<Integer> rankIdList = RankConstant.rankTypeMap.get(rankType);
                List<RoleRankPo> list = DBUtil.queryList(DBUtil.DB_USER, RoleRankPo.class, "select * from `allrank`; ");
                for (int rankId : rankIdList) {
                    AbstractRank rank = rankMap.get(rankId);
                    for (AbstractRankPo roleRankPo : list) {
                        AbstractRankPo newPo = roleRankPo.copy();
                        newPo.setRankId(rank.rankId);
                        rank.addTreeSet(null, newPo);
                    }
                }
            } else {
                List<Integer> rankIdList = RankConstant.rankTypeMap.get(rankType);
                for (int rankId : rankIdList) {
                    AbstractRank rank = rankMap.get(rankId);
                    rank.loadData(); // 如果存在共用的情况在重构
                }
            }
        }
    }

    private void registerRank() throws IllegalAccessException, InstantiationException {
        rankMap = new HashMap<>();
        for (Map.Entry<Integer, Class<? extends AbstractRank>> entry : RankConstant.rankClazzMap.entrySet()) {
            AbstractRank abstractRank = entry.getValue().newInstance();
            abstractRank.setRankId(entry.getKey());
            rankMap.put(entry.getKey(), abstractRank);
        }
    }


    /**
     * 发奖方法
     *
     * @param rankId
     */
    private void reward(int rankId) {
        if (rankMap.get(rankId) == null) {
            return;
        }
        Iterator<AbstractRankPo> iterator = rankMap.get(rankId).treeSet.iterator();
        int count = 0;// 已发
        int awardRankMax = 0;// 奖励最大排名
        do {
            if (!iterator.hasNext()) break;
            RoleRankPo roleRankPo = (RoleRankPo) iterator.next();
            for (RankAwardVo awardVo : RankManager.getRankAward(rankId)) {
                if (awardRankMax < awardVo.getSections()[1])
                    awardRankMax = awardVo.getSections()[1];
                if (awardVo.getSections()[0] <= roleRankPo.getRank() && roleRankPo.getRank() <= awardVo.getSections()[1]) {
                    // 调用邮件接口发奖
                    // todo:团队奖励(家族排行榜)邮件发奖接口写好时修改
                    ServiceHelper.emailService().sendToSingle(
                            roleRankPo.getRoleId(), awardVo.getEmail(), Long.valueOf(rankId), "排行榜",
                            awardVo.getRewardMap(), String.valueOf(count + 1));
                    break;
                }
            }
            count++;
        } while (count <= awardRankMax && iterator.hasNext());
    }

    /**
     * 遍历排名
     */
    @Override
    public void sortRank() {
        for (AbstractRank rankImp : rankMap.values()) {
            rankImp.sort();
        }
    }

    /**
     * 指定时间发奖检查
     */
    @Override
    public void appointReward() {
        // 配置有定时发奖类型才跑发奖线程
        if (RankManager.getRankIdByRewardType(RankConstant.REWARD_TYPE_APPOINT).isEmpty())
            return;
        for (int rankId : RankManager.getRankIdByRewardType(RankConstant.REWARD_TYPE_APPOINT)) {
            RankAwardVo awardVo = RankManager.getRankAward(rankId).get(0);
            if (awardVo.getRewardType() == RankConstant.REWARD_TYPE_APPOINT
                    && System.currentTimeMillis() >= awardVo.getAppointTime()) {
                reward(rankId);
            }
        }
    }

    /**
     * 根据rankId获取对应排行榜列表数据
     */
    @Override
    public List<AbstractRankPo> getRankingList(int rankId) {
        List<AbstractRankPo> list = null;
        AbstractRank abstractRank = rankMap.get(rankId);
        if (abstractRank != null) {
            TreeSet<AbstractRankPo> treeSet = abstractRank.getTreeSet();
            list = new ArrayList<>(treeSet);
        } else {
            list = new ArrayList<>();
        }
        return list;
    }

    @Override
    public List<AbstractRankPo> getFrontRank(int rankId, int frontCount) {
        AbstractRank abstractRank = rankMap.get(rankId);
        if (abstractRank != null) {
            return abstractRank.getFrontRankPo(frontCount);
        }
        return null;
    }

    @Override
    public List<AbstractRankPo> getFrontRank(int rankId, int frontCount, Object... args) {
        AbstractRank abstractRank = rankMap.get(rankId);
        if (abstractRank != null) {
            switch (rankId) {
                case RankConstant.RANKID_BEST_CP_VOTER: {
                    BestCpVoterRank bestCpVoterRank = (BestCpVoterRank) abstractRank;
                    return bestCpVoterRank.getFrontRankPo(frontCount, args);
                }
                case RankConstant.RANKID_DRAGON_BOAT: {
                    DragonBoatRank dragonBoatRank = (DragonBoatRank) abstractRank;
                    return dragonBoatRank.getFrontRankPo(frontCount, args);
                }
                case RankConstant.RANKID_CAMP_CITY_REPUTATION: {
                    CampRoleReputationRank campRoleReputationRank = (CampRoleReputationRank) abstractRank;
                    return campRoleReputationRank.getFrontRankPo(frontCount, args);
                }
                default: {
                    return abstractRank.getFrontRankPo(frontCount);
                }
            }

        }
        return null;
    }

    public void flushFightScore2FightingMaster() {
        AbstractRank abstractRank = rankMap.get(RankConstant.RANKID_FIGHTSCORE);

        if (abstractRank == null || !(abstractRank instanceof FightScoreRank)) {
            LogUtil.info("刷新最大战力值到巅峰对决服出错------------》");
            return;
        }
        FightScoreRank fRank = (FightScoreRank) abstractRank;
        List<AbstractRankPo> list = fRank.getFrontRankPo(10);
        RoleRankPo rankPo;
        int sum = 0;
        int counter = 0;
        for (AbstractRankPo abstractRankPo : list) {
            if (abstractRankPo == null) {
                continue;
            }
            rankPo = (RoleRankPo) abstractRankPo;
            sum = sum + rankPo.getFightScore();
            counter++;
        }
        if (counter > 0) {
            long coefficient = Long.parseLong(DataManager.getCommConfig("personpk_highmatch"));
            long average = sum * coefficient / counter / 100;
            MainRpcHelper.fightingMasterService().updateMaxFightingVal(MultiServerHelper.getFightingMasterServer(), (int) average);
            LogUtil.info("average=" + average);
        }
    }

    @Override
    public void updateRoleName(long roleId, String newName) {
        RoleRankPo rankPo_level = (RoleRankPo) getRank(RankConstant.RANKID_ROLELEVEL, roleId);
        rankPo_level.setRoleName(newName);
        rankDao.update(rankPo_level);
        RoleRankPo rankPo_fight = (RoleRankPo) getRank(RankConstant.RANKID_FIGHTSCORE, roleId);
        rankPo_fight.setRoleName(newName);
        rankDao.update(rankPo_fight);
        RoleRankPo rankPo_skytowerlayer = (RoleRankPo) getRank(RankConstant.RANKID_SKYTOWERLAYER, roleId);
        rankPo_skytowerlayer.setRoleName(newName);
        rankDao.update(rankPo_skytowerlayer);
        RoleRankPo rankPo_game = (RoleRankPo) getRank(RankConstant.RANKID_GAMECAVESCORE, roleId);
        rankPo_game.setRoleName(newName);
        rankDao.update(rankPo_game);
        RoleRankPo rankPo_familyTreasure = (RoleRankPo) getRank(RankConstant.RANKID_ROLEFAMILYTREASURE, roleId);
        rankPo_familyTreasure.setRoleName(newName);
        rankDao.update(rankPo_familyTreasure);
        RoleRankPo rankPo_totalLevel = (RoleRankPo) getRank(RankConstant.RANKID_TOTAL_ROLELEVEL, roleId);
        rankPo_totalLevel.setRoleName(newName);
        rankDao.update(rankPo_totalLevel);
    }

    @Override
    public void resetRank4Camp() {
        CampRoleReputationRank campRoleReputationRank = (CampRoleReputationRank) rankMap.get(RankConstant.RANKID_CAMP_CITY_REPUTATION);
        campRoleReputationRank.reset();
    }

    class TimingTask implements Runnable {
        @Override
        public void run() {
            ServiceHelper.rankService().sortRank();
            ServiceHelper.rankService().appointReward();
            if (LastFlushFighting2FightingMaster == 0 || System.currentTimeMillis() - LastFlushFighting2FightingMaster
                    >= FlushFighting2FightingMasterDis) {
                ServiceHelper.rankService().flushFightScore2FightingMaster();
                LastFlushFighting2FightingMaster = System.currentTimeMillis();
            }
        }


    }

}
