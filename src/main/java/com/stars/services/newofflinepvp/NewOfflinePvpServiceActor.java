package com.stars.services.newofflinepvp;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.persist.DbRowDao;
import com.stars.core.player.PlayerUtil;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.newofflinepvp.NewOfflinePvpManager;
import com.stars.modules.newofflinepvp.event.OfflinePvpClientStageFinishEvent;
import com.stars.modules.newofflinepvp.event.OfflinePvpMatchEvent;
import com.stars.modules.newofflinepvp.event.OfflinePvpSendRankEvent;
import com.stars.modules.newofflinepvp.packet.ClientNewOfflinePvp;
import com.stars.modules.newofflinepvp.prodata.OfflineInitializeVo;
import com.stars.modules.newofflinepvp.prodata.OfflineMatchVo;
import com.stars.modules.rank.RankManager;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.ServiceUtil;
import com.stars.services.activities.ActConst;
import com.stars.services.newofflinepvp.cache.BattleReport;
import com.stars.services.newofflinepvp.userdata.NewOfflinePvpRankPo;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.prodata.RankAwardVo;
import com.stars.util.LogUtil;
import com.stars.util.RandomUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by chenkeyu on 2017-03-08 15:45
 */
public class NewOfflinePvpServiceActor extends ServiceActor implements NewOfflinePvpService {
    Map<Long, NewOfflinePvpRankPo> newOfflinePvpRankPoMap;//<roleId,NewOfflinePvpRankPo>
    Map<Long, NewOfflinePvpRankPo> lockRankPoMap;//正在战斗中玩家，上锁<roleId,NewOfflinePvpRankPo>
    Map<Long, LinkedList<BattleReport>> battleReportCacheMap;//战报缓存
    private DbRowDao rankDao;
    private NewOfflinePvpFlow flow;
    private boolean offlinePvpOpen;

    @Override
    public void init() throws Throwable {
        rankDao = new DbRowDao(SConst.NewOfflinePvpService);
        ServiceSystem.getOrAdd(SConst.NewOfflinePvpService, this);
        newOfflinePvpRankPoMap = new HashMap<>();
        lockRankPoMap = new HashMap<>();
        battleReportCacheMap = new HashMap<>();
        flow = new NewOfflinePvpFlow();
        synchronized (NewOfflinePvpServiceActor.class) {
            loadData();
            flow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(ActConst.ID_OFFLINEPVP));
        }
    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出:{},lockRankPoMap:{},newOfflinePvpRankPoMap:{},battleReportCacheMap:{}", this.getClass().getSimpleName(), lockRankPoMap.size(), newOfflinePvpRankPoMap.size(), battleReportCacheMap.size());
    }

    @Override
    public void dealExitFight(byte victoryOrDefeat, long otherRoleId, long selfRoleId, int level,
                              int jobId, String roleName, int fightScore) {
        NewOfflinePvpRankPo selfRank = newOfflinePvpRankPoMap.get(selfRoleId);
        NewOfflinePvpRankPo otherRank = newOfflinePvpRankPoMap.get(otherRoleId);
        unLockRole(selfRoleId, otherRoleId);
        if (victoryOrDefeat == NewOfflinePvpManager.victory) {
            if (selfRank == null) {
                if (otherRank.getRank() == NewOfflinePvpManager.coachRobotRank) {
                    dealPacketToClient(NewOfflinePvpManager.victory, selfRoleId,
                            5001, 0);
                    dealBattleReport(selfRoleId, NewOfflinePvpManager.active, NewOfflinePvpManager.victory,
                            0, otherRank.getRoleName());
                    dealBattleReport(otherRoleId, NewOfflinePvpManager.passivity, NewOfflinePvpManager.defeat,
                            0, roleName);
                } else {
                    selfRank = initNewOfflinePvpRankPo(selfRoleId, otherRank.getRank(), jobId, level,
                            roleName, NewOfflinePvpManager.role, fightScore, otherRoleId, System.currentTimeMillis());
                    updateOfflinePvpRank(otherRank, selfRank, true);
                    dealBattleReport(selfRoleId, NewOfflinePvpManager.active, NewOfflinePvpManager.victory,
                            selfRank.getRank(), otherRank.getRoleName());
                    dealBattleReport(otherRoleId, NewOfflinePvpManager.passivity, NewOfflinePvpManager.defeat,
                            5001, selfRank.getRoleName());
                }
                return;
            }
            int selfRankId = selfRank.getRank();
            int otherRankId = otherRank.getRank();
            if (selfRankId == otherRankId) {
                LogUtil.error("竞技场用户数据有问题！！！|selfRankId:{},otherRankId:{}", selfRankId, otherRankId);
            } else if (selfRankId > otherRankId) {
                //数值越大，排名越小
                updateOfflinePvpRank(otherRank, selfRank, false);
                dealBattleReport(selfRoleId, NewOfflinePvpManager.active, NewOfflinePvpManager.victory,
                        selfRank.getRank(), otherRank.getRoleName());
                dealBattleReport(otherRoleId, NewOfflinePvpManager.passivity, NewOfflinePvpManager.defeat,
                        otherRank.getRank(), selfRank.getRoleName());
            } else {
                //不需要更新排名
                dealBattleReport(selfRoleId, NewOfflinePvpManager.active, NewOfflinePvpManager.victory,
                        0, otherRank.getRoleName());
                dealBattleReport(otherRoleId, NewOfflinePvpManager.passivity, NewOfflinePvpManager.defeat,
                        0, selfRank.getRoleName());
                dealPacketToClient(NewOfflinePvpManager.victory, selfRoleId, selfRank.getRank(), 0);
            }
        } else {
            //不需要更新排名
            dealBattleReport(selfRoleId, NewOfflinePvpManager.active, NewOfflinePvpManager.defeat,
                    0, otherRank.getRoleName());
            dealBattleReport(otherRoleId, NewOfflinePvpManager.passivity, NewOfflinePvpManager.victory,
                    0, roleName);
            dealPacketToClient(NewOfflinePvpManager.defeat, selfRoleId, selfRank != null ? selfRank.getRank() : 5001, 0);
        }
    }

    /**
     * 给客户端发包
     *
     * @param finish
     * @param selfRoleId
     * @param myRank
     * @param updateRank
     */
    private void dealPacketToClient(byte finish, long selfRoleId, int myRank, int updateRank) {
        ServiceHelper.roleService().notice(selfRoleId, new OfflinePvpClientStageFinishEvent(finish, myRank, updateRank));
    }

    /**
     * 更新排行榜数据
     *
     * @param otherRank 对方
     * @param selfRank  自己
     * @param isNew     自己是否是新人(不在排行榜中)
     */
    private void updateOfflinePvpRank(NewOfflinePvpRankPo otherRank, NewOfflinePvpRankPo selfRank, boolean isNew) {
        int oldRank = selfRank.getRank();
        if (!newOfflinePvpRankPoMap.containsKey(otherRank.getRoleId())) {
            //没有对方排行榜
            return;
        }
        if (isNew) {
            newOfflinePvpRankPoMap.remove(otherRank.getRoleId());
            newOfflinePvpRankPoMap.put(selfRank.getRoleId(), selfRank);
            rankDao.insert(selfRank);
            rankDao.delete(otherRank);
        } else {
            int otherRankId = otherRank.getRank();
            otherRank.setRank(selfRank.getRank());
            selfRank.setRank(otherRankId);
            rankDao.update(otherRank);
            rankDao.update(selfRank);
        }
        dealPacketToClient(NewOfflinePvpManager.victory, selfRank.getRoleId(),
                selfRank.getRank(), isNew ? 5001 - selfRank.getRank() : oldRank - selfRank.getRank());
    }

    /**
     * 处理战报的问题
     *
     * @param selfRoleId
     * @param initiativeOrPassivity
     * @param victoryOfDefeat
     * @param rank
     * @param otherName
     */
    private void dealBattleReport(long selfRoleId, byte initiativeOrPassivity, byte victoryOfDefeat,
                                  int rank, String otherName) {
        LinkedList<BattleReport> selfBattleReports = battleReportCacheMap.get(selfRoleId);
        if (selfBattleReports == null) {
            selfBattleReports = new LinkedList<>();
            battleReportCacheMap.put(selfRoleId, selfBattleReports);
        }
        BattleReport selfBattleReport = newInstance(selfRoleId, initiativeOrPassivity, victoryOfDefeat, rank, otherName);
        addBattleReport(selfBattleReports, selfBattleReport);
    }

    /**
     * 处理过时记录清除，并加上新纪录
     *
     * @param battleReports
     * @param battleReport
     */
    private void addBattleReport(LinkedList<BattleReport> battleReports, BattleReport battleReport) {
        if (battleReports.size() >= NewOfflinePvpManager.maxBattleReport) {
            for (int i = 0; i < battleReports.size() - NewOfflinePvpManager.maxBattleReport + 1; i++) {
                battleReports.remove(Collections.min(battleReports));
            }
            battleReports.add(battleReport);
        } else {
            battleReports.add(battleReport);
        }
    }

    @Override
    public void changeRoleLevel(long roleId, int level) {
        NewOfflinePvpRankPo rankPo = newOfflinePvpRankPoMap.get(roleId);
        if (rankPo != null) {
            rankPo.setLevel(level);
            rankDao.update(rankPo);
        }
    }
    @Override
    public void changeRoleName(long roleId, String newName) {
        NewOfflinePvpRankPo rankPo = newOfflinePvpRankPoMap.get(roleId);
        if (rankPo != null) {
            rankPo.setRoleName(newName);
            rankDao.update(rankPo);
        }
    }
    @Override
    public void changeRoleJob(long roleId, int jobId) {
        NewOfflinePvpRankPo rankPo = newOfflinePvpRankPoMap.get(roleId);
        if (rankPo != null) {
            rankPo.setJobId(jobId);
            rankDao.update(rankPo);
        }
    }
    @Override
    public void changeRoleFightScore(long roleId, int fightScore) {
        NewOfflinePvpRankPo rankPo = newOfflinePvpRankPoMap.get(roleId);
        if (rankPo != null) {
            rankPo.setFightScore(fightScore);
            rankDao.update(rankPo);
        }
    }

    /**
     * 新的战报
     *
     * @param roleId
     * @param initiativeOrPassivity
     * @param rank
     * @param otherName
     * @return
     */
    private BattleReport newInstance(long roleId, byte initiativeOrPassivity, byte victoryOfDefeat,
                                     int rank, String otherName) {
        BattleReport battleReport = new BattleReport();
        battleReport.setRoleId(roleId);
        battleReport.setTimeStamp(System.currentTimeMillis());
        battleReport.setInitiativeOrPassivity(initiativeOrPassivity);
        battleReport.setVictoryOrDefeat(victoryOfDefeat);
        battleReport.setRank(rank);
        battleReport.setOtherName(otherName);
        return battleReport;
    }

    /**
     * 加载数据
     *
     * @throws SQLException
     */
    private void loadData() throws SQLException {
        newOfflinePvpRankPoMap = DBUtil.queryMap(DBUtil.DB_USER, "roleid", NewOfflinePvpRankPo.class, "select * from `offlinepvprank`; ");
        if (newOfflinePvpRankPoMap == null || newOfflinePvpRankPoMap.size() < 1000) {
            newOfflinePvpRankPoMap = new HashMap<>();
            for (Map.Entry<Long, OfflineInitializeVo> initializeVoEntry : NewOfflinePvpManager.getOfflineInitializeVoMap().entrySet()) {
                OfflineInitializeVo initializeVo = initializeVoEntry.getValue();
                NewOfflinePvpRankPo rankPo = initNewOfflinePvpRankPo(initializeVo.getInitializeId(), (int) initializeVo.getInitializeId(),
                        initializeVo.getJobId(), initializeVo.getRobotLevel(), initializeVo.getRobotName(),
                        NewOfflinePvpManager.robot, initializeVo.getRobotFightScore(), 0, 0);
                newOfflinePvpRankPoMap.put(initializeVoEntry.getKey(), rankPo);
                rankDao.insert(rankPo);
            }
        }
    }

    @Override
    public void openOfflinePvp(long roleId, int level, int jobId, String roleName, int fightScore) {
        if (newOfflinePvpRankPoMap.containsKey(roleId)) {
            LogUtil.error("数据有问题，存在相同的两个玩家");
            return;
        }
        if (newOfflinePvpRankPoMap.size() < NewOfflinePvpManager.maxRank) {
            NewOfflinePvpRankPo rankPo = initNewOfflinePvpRankPo(roleId, getMaxRank() + 1, jobId,
                    level, roleName, NewOfflinePvpManager.role, fightScore, 0, 0);
            newOfflinePvpRankPoMap.put(rankPo.getRoleId(), rankPo);
            rankDao.insert(rankPo);
        }
    }

    @Override
    public boolean lockRole(long selfRoleId, long otherRoleId) {
        if (!offlinePvpOpen) {
            ServiceUtil.sendText(selfRoleId, "offline_tips_cantfight");
            return false;
        }
        // 判断自己是否上锁，及是否超时?
        NewOfflinePvpRankPo selfRankPo = lockRankPoMap.get(selfRoleId);
        if (selfRankPo != null) {
            if (selfRankPo.getLastFightTimestamp() == 0L) {
                selfRankPo.setLastFightTimestamp(System.currentTimeMillis());
            }
            if ((System.currentTimeMillis() - selfRankPo.getLastFightTimestamp()) / 1000 < 300) {
                ServiceUtil.sendText(selfRoleId, "offline_desc_selfbusy");
                LogUtil.info("演武场|上锁失败|selfRoleId:{}|timeDelta:{}",
                        selfRoleId, (System.currentTimeMillis() - selfRankPo.getLastFightTimestamp()) / 1000);
                return false;
            }
        }
        // 判断对方是否上锁，及是否超时?
        NewOfflinePvpRankPo otherRankPo = lockRankPoMap.get(otherRoleId);
        if (otherRoleId != NewOfflinePvpManager.coachRobotId && otherRankPo != null) {
            if (otherRankPo.getLastFightTimestamp() == 0L) {
                otherRankPo.setLastFightTimestamp(System.currentTimeMillis());
            }
            if ((System.currentTimeMillis() - otherRankPo.getLastFightTimestamp()) / 1000 < 300) {
                ServiceUtil.sendText(selfRoleId, "offline_desc_targetbusy");
                LogUtil.info("演武场|上锁失败|otherRoleId:{}|timeDelta:{}",
                        otherRoleId, (System.currentTimeMillis() - otherRankPo.getLastFightTimestamp()) / 1000);
                return false;
            }
        }
        // 上锁，并设置时间戳
        selfRankPo = newOfflinePvpRankPoMap.get(selfRoleId);
        otherRankPo = newOfflinePvpRankPoMap.get(otherRoleId);
        if (selfRankPo == null) {
            selfRankPo = new NewOfflinePvpRankPo();
        }
        selfRankPo.setLastFightTimestamp(System.currentTimeMillis());
        lockRankPoMap.put(selfRoleId, selfRankPo);
        if (otherRoleId != NewOfflinePvpManager.coachRobotId) {
            otherRankPo.setLastFightTimestamp(System.currentTimeMillis());
            lockRankPoMap.put(otherRoleId, otherRankPo);
        }
        return true;
    }

    @Override
    public void unLockRole(long selfRoleId, long otherRoleId) {
        lockRankPoMap.remove(selfRoleId);
        lockRankPoMap.remove(otherRoleId);
    }

    @Override
    public void sendRankList(long roleId) {
        List<NewOfflinePvpRankPo> rankPoList = new LinkedList<>();
        for (Map.Entry<Long, NewOfflinePvpRankPo> rankPoEntry : newOfflinePvpRankPoMap.entrySet()) {
            rankPoList.add(rankPoEntry.getValue());
        }
        Collections.sort(rankPoList);
        List<NewOfflinePvpRankPo> finalList = new ArrayList<>();
        for (int i = 0; i < NewOfflinePvpManager.maxShowRank; i++) {
            finalList.add(rankPoList.get(i).copy());
        }
        OfflinePvpSendRankEvent cnop = new OfflinePvpSendRankEvent();
        if (newOfflinePvpRankPoMap.containsKey(roleId)) {
            finalList.add(newOfflinePvpRankPoMap.get(roleId).copy());
            cnop.setOnRank(NewOfflinePvpManager.onRank);
        } else {
            cnop.setOnRank(NewOfflinePvpManager.notOnRank);
        }
        cnop.setRankPoList(finalList);
        ServiceHelper.roleService().notice(roleId, cnop);
    }

    @Override
    public void match(long roleId, int maxRank, int remianFightCount, int remianBuyCount, byte first) {
        int rank = 5001;
        if (newOfflinePvpRankPoMap.containsKey(roleId)) {
            rank = newOfflinePvpRankPoMap.get(roleId).getRank();
        }
        match(roleId, maxRank, rank, remianFightCount, remianBuyCount, first);
    }

    private void match(long roleId, int maxRank, int myRank, int remianFightCount, int remianBuyCount, byte first) {
        OfflineMatchVo offlineMatchVo = NewOfflinePvpManager.getOfflineMatchVo(myRank);
        if (offlineMatchVo == null) {
            LogUtil.error("没有产品数据,排名错误:{}", myRank);
            return;
        }
        int minMatch = offlineMatchVo.getMinMatch();
        int maxMatch = offlineMatchVo.getMaxMatch();
        if (myRank <= maxMatch) {
            maxMatch = myRank - 1;
        }
        int min = myRank - minMatch;
        int max = myRank - maxMatch;
        Set<Integer> otherRankIds = match(myRank, roleId, min, max, first);
        List<NewOfflinePvpRankPo> rankPoList = new ArrayList<>();
        for (Integer rankId : otherRankIds) {
            for (NewOfflinePvpRankPo rankPo : newOfflinePvpRankPoMap.values()) {
                if (rankPo.getRank() == rankId) {
                    NewOfflinePvpRankPo newRankPo = rankPo.copy();
                    rankPoList.add(newRankPo);
                }
            }
        }
        List<Long> fightIds = new ArrayList<>();
        for (NewOfflinePvpRankPo rankPo : rankPoList) {
            fightIds.add(rankPo.getRoleId());
        }
        ServiceHelper.roleService().notice(roleId, new OfflinePvpMatchEvent(fightIds));
        ClientNewOfflinePvp clientNewOfflinePvp = new ClientNewOfflinePvp(ClientNewOfflinePvp.view);
        clientNewOfflinePvp.setMyRank(myRank);
        clientNewOfflinePvp.setMyMaxRank(maxRank);
        clientNewOfflinePvp.setRemainCount(remianFightCount);
        clientNewOfflinePvp.setRemainBuyCount(remianBuyCount);
        clientNewOfflinePvp.setOtherPlayerDatas(rankPoList);
        PlayerUtil.send(roleId, clientNewOfflinePvp);
    }

    /**
     * @param myRank
     * @param min
     * @param max
     * @param first
     * @return
     */
    private Set<Integer> match(int myRank, long roleId, int min, int max, byte first) {
        Set<Integer> otherRankIds = new HashSet<>();
        boolean isLow = false;
        boolean isRunnerup = true;
        long startTime = System.currentTimeMillis();
        if (first == NewOfflinePvpManager.first) {
            otherRankIds.add(NewOfflinePvpManager.coachRobotRank);
        }
        while (otherRankIds.size() < 3 && (System.currentTimeMillis() - startTime) / 1000 < 5) {
            //排名越高，值越小
            int rankId = RandomUtil.rand(max, min);
            if (rankId == myRank || newOfflinePvpRankPoMap.size() - 1 < rankId) {
                continue;
            }
            if (!newOfflinePvpRankPoMap.containsKey(roleId)) {
                otherRankIds.add(rankId);
            }
            if (!isLow && myRank == getMaxRank()) {
                isLow = true;
            }
            if (!isLow && myRank != getMaxRank() && rankId > myRank) {
                otherRankIds.add(rankId);
                isLow = true;
            }
            if (isRunnerup && myRank == NewOfflinePvpManager.runnerup && rankId > myRank) {
                if (!otherRankIds.contains(rankId)) {
                    isRunnerup = false;
                    otherRankIds.add(rankId);
                }
            }
            if (isLow) {
                if (myRank == NewOfflinePvpManager.champion) {
                    otherRankIds.add(rankId);
                } else if (rankId < myRank) {
                    otherRankIds.add(rankId);
                }
            }
        }
        return otherRankIds;
    }

    @Override
    public void sendBattleReport(long roleId) {
        ClientNewOfflinePvp clientNewOfflinePvp = new ClientNewOfflinePvp(ClientNewOfflinePvp.battleReport);
        if (battleReportCacheMap.containsKey(roleId)) {
            LinkedList<BattleReport> battleReports = battleReportCacheMap.get(roleId);
            Collections.sort(battleReports);
            clientNewOfflinePvp.setBattleReports(battleReports);
        } else {
            clientNewOfflinePvp.setBattleReports(new LinkedList<BattleReport>());
        }
        PlayerUtil.send(roleId, clientNewOfflinePvp);
    }

    @Override
    public void closeOfflinePvp() {
        offlinePvpOpen = false;
    }

    @Override
    public void openOfflinePvp() {
        offlinePvpOpen = true;
    }

    @Override
    public void sendRankAward() {
        List<RankAwardVo> rankAwardVos = RankManager.getRankAward(RankConstant.RANKID_OFFLINEPVP);
        for (RankAwardVo awardVo : rankAwardVos) {
            for (Map.Entry<Long, NewOfflinePvpRankPo> rankPoEntry : newOfflinePvpRankPoMap.entrySet()) {
                NewOfflinePvpRankPo rankPo = rankPoEntry.getValue();
                if (rankPo.getRank() >= awardVo.getSections()[0] && rankPo.getRank() <= awardVo.getSections()[1] && rankPo.getRoleOrRobot() == NewOfflinePvpManager.role) {
                    ServiceHelper.emailService().sendToSingle(rankPo.getRoleId(), awardVo.getEmail(), 0L, "系统", awardVo.getRewardMap(), String.valueOf(rankPo.getRank()));
                }
            }
        }
    }

    @Override
    public int getRank(long roleId) {
        if (newOfflinePvpRankPoMap.containsKey(roleId)) {
            return newOfflinePvpRankPoMap.get(roleId).getRank();
        } else {
            return NewOfflinePvpManager.maxRank + 1;
        }
    }

    /**
     * 新排行榜实例
     *
     * @param roleId             玩家id
     * @param rank               排名
     * @param level              玩家等级
     * @param roleName           玩家名字
     * @param roleOrRobot        真人还是机器人
     * @param fightScore         战斗力
     * @param lastFightObject    上一次对战对象
     * @param lastFightTimestamp 上一次战斗时间戳
     * @return
     */
    private NewOfflinePvpRankPo initNewOfflinePvpRankPo(long roleId, int rank, int jobId, int level,
                                                        String roleName, byte roleOrRobot, int fightScore,
                                                        long lastFightObject, long lastFightTimestamp) {
        NewOfflinePvpRankPo rankPo = new NewOfflinePvpRankPo();
        rankPo.setRoleId(roleId);
        rankPo.setRank(rank);
        rankPo.setJobId(jobId);
        rankPo.setLevel(level);
        rankPo.setRoleName(roleName);
        rankPo.setRoleOrRobot(roleOrRobot);
        rankPo.setFightScore(fightScore);
        rankPo.setLastFightObject(lastFightObject);
        rankPo.setLastFightTimestamp(lastFightTimestamp);
        return rankPo;
    }

    private int getMaxRank() {
        Map<Long, NewOfflinePvpRankPo> tmpMap = new HashMap<>(newOfflinePvpRankPoMap);
        tmpMap.remove(NewOfflinePvpManager.coachRobotId);
        Collection<NewOfflinePvpRankPo> rankPo = tmpMap.values();
        return Collections.max(rankPo).getRank();
    }

    @Override
    public void save() {
        rankDao.flush();
    }
}
