package com.stars.multiserver.familywar;

import com.stars.core.dao.DbRowDao;
import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.packet.ClientFamilyRank;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.FamilyRankInfo;
import com.stars.multiserver.familywar.flow.FamilyWarQualifyingFlow;
import com.stars.multiserver.familywar.flow.FamilyWarRemoteFlow;
import com.stars.multiserver.familywar.qualifying.cache.FamilyWarQualifyingFixtureCache;
import com.stars.multiserver.familywar.rank.CrossServiceFamilyRank;
import com.stars.multiserver.familywar.rank.DissolveFamily;
import com.stars.multiserver.familywar.rank.RankConst;
import com.stars.multiserver.familywar.remote.FamilyWarRemoteFamily;
import com.stars.services.SConst;
import com.stars.services.ServiceSystem;
import com.stars.services.activities.ActConst;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.FamilyRankPo;
import com.stars.util.LogUtil;
import com.stars.util.TimeUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenkeyu on 2017-06-28.
 */
public class FamilywarRankServiceActor extends ServiceActor implements FamilywarRankService {
    private LinkedHashMap<String, CrossServiceFamilyRank> familyRankList;
    private Map<Long, String> familyTitleMap;
    private Set<Integer> serverSet;
    private Map<Long, DissolveFamily> dissolveFamilyMap;
    private volatile int block = 600;
    private DbRowDao rowDao;
    private boolean isReset = false;
    private long timeStart;
    private long timeEnd;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.FamilyWarRankService, this);
        rowDao = new DbRowDao(SConst.FamilyWarRankService, DBUtil.DB_COMMON);
        serverSet = new HashSet<>();
        familyRankList = new LinkedHashMap<>();
        familyTitleMap = new HashMap<>();
        dissolveFamilyMap = new HashMap<>();
        loadCommonData();
        loadDissovleFamily();
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.FamilyWar, new Runnable() {
            @Override
            public void run() {
                callAllServerdata();
            }
        }, 30, 1, TimeUnit.SECONDS);
        timeStart = FamilyWarUtil.getBattleTimeL(FamilyWarQualifyingFlow.STEP_START_QUALIFYING, ActConst.ID_FAMILY_WAR_QUALIFYING);
        timeEnd = FamilyWarUtil.getBattleTimeL(FamilyWarRemoteFlow.STEP_END_REMOTE, ActConst.ID_FAMILY_WAR_REMOTE);
        long now = System.currentTimeMillis();
        if (now > timeStart && now < timeEnd) {
            getFamilyCount();
        }
    }

    private void getFamilyCount() {
        try {
            String sql = "select * from qualifyingfixture where markfinish = 0";
            List<FamilyWarQualifyingFixtureCache> cacheList = DBUtil.queryList(DBUtil.DB_COMMON, FamilyWarQualifyingFixtureCache.class, sql);
            FamilyWarConst.familyCount = (cacheList.size() / 5) * 2;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDissovleFamily() {
        try {
            String sql = "select * from dissolvefamily";
            Map<Long, DissolveFamily> dissolveFamilies = DBUtil.queryMap(DBUtil.DB_COMMON, "familyid", DissolveFamily.class, sql);
            this.dissolveFamilyMap = dissolveFamilies;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCommonData() {
        try {
            String sql = "select * from familywarremotefamily";
            List<FamilyWarRemoteFamily> remoteFamilyList = DBUtil.queryList(DBUtil.DB_COMMON, FamilyWarRemoteFamily.class, sql);
            if (remoteFamilyList != null) {
                for (FamilyWarRemoteFamily remoteFamily : remoteFamilyList) {
                    familyTitleMap.put(remoteFamily.getFamilyId(), RankConst.getRank(remoteFamily.getRank()));
                }
            }
            LogUtil.info("捞库|familyTitleMap:{}", familyTitleMap);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void callAllServerdata() {
        if (block < RankConst.period) {
            block++;
            return;
        }
        block = 0;
        LogUtil.info("跨服家族排行榜|serverSet:{}", serverSet);
        for (int serverId : serverSet) {
            FamilyWarRpcHelper.familyWarService().onCallMainServer(serverId);
        }

    }

    @Override
    public void updateTitle(int serverId, List<Long> familyIds, String title, int type) {
        RankConst.type = type;
        if (type == RankConst.W_TYPE_QUALIFY && !isReset) {
            TitleRestNone();
            isReset = true;
        }
        for (long familyId : familyIds) {
            familyTitleMap.put(familyId, title);
            familyRankList.get(Long.toString(familyId)).setType(title);
        }
        LogUtil.info("familyTitleMap:{}", familyTitleMap);
    }

    @Override
    public void resetTitle(int serverId, boolean loadData) {
        RankConst.type = RankConst.W_TYPE_NONE;
        TitleRestNone();
        loadCommonData();
        for (Map.Entry<Long, String> entry : familyTitleMap.entrySet()) {
            familyRankList.get(Long.toString(entry.getKey())).setType(entry.getValue());
        }
        LogUtil.info("familyTitleMap:{}", familyTitleMap);
    }

    private void TitleRestNone() {
        familyTitleMap.clear();
        for (CrossServiceFamilyRank familyRank : familyRankList.values()) {
            familyRank.setType(RankConst.NONE);
        }
        LogUtil.info("清除全部的title");
    }

    @Override
    public void connectServer(int serverId, int fromServerId) {
        LogUtil.info("connectServer|fromServerId:{}", fromServerId);
        serverSet.add(fromServerId);
    }

    @Override
    public void mainServerFamilyData(int serverId, int fromServerId, List<AbstractRankPo> familyList) {
        for (AbstractRankPo po : familyList) {
            FamilyRankPo rankPo = (FamilyRankPo) po;
            if (dissolveFamilyMap.containsKey(rankPo.getFamilyId()))
                continue;
            String familyIdStr = Long.toString(rankPo.getFamilyId());
            String familyName = rankPo.getName();
            String masterName = rankPo.getMasterName();
            if (!familyRankList.containsKey(familyIdStr)) {
                CrossServiceFamilyRank familyRank = new CrossServiceFamilyRank(familyIdStr, rankPo.getFightScore());
                familyRank.setFamilyName(familyName);
                familyRank.setMasterName(masterName);
                familyRank.setServerId(fromServerId);
                if (familyTitleMap.containsKey(rankPo.getFamilyId())) {
                    familyRank.setType(familyTitleMap.get(rankPo.getFamilyId()));
                } else if (RankConst.type == RankConst.W_TYPE_QUALIFY) {
                    familyRank.setType(RankConst.NONE_QUALIFY);
                } else if (RankConst.type == RankConst.W_TYPE_REMOTE) {
                    familyRank.setType(RankConst.NONE_REMOTE);
                } else {
                    familyRank.setType(RankConst.NONE);
                }
                familyRankList.put(familyIdStr, familyRank);
            } else {
                updateRank(fromServerId, familyIdStr, masterName, familyName, rankPo.getFightScore());
            }
        }
    }

    private void updateRank(int fromServerId, String familyIdStr, String masterName, String familyName, long fightScore) {
        CrossServiceFamilyRank familyRank = familyRankList.get(familyIdStr);
        familyRank.setServerId(fromServerId);
        familyRank.setFamilyName(familyName);
        familyRank.setMasterName(masterName);
        familyRank.setFightScore(fightScore);
    }

    @Override
    public void view(int serverId, int fromServerId, long familyId, long roleId) {
        ClientFamilyRank clientFamilyRank = new ClientFamilyRank(ClientFamilyRank.rank);
        clientFamilyRank.setDescText(getTimeDesc());
        clientFamilyRank.setInfoList(getFamilyRankInfo(familyRankList, RankConst.size, Long.toString(familyId)));
        FamilyWarRpcHelper.roleService().send(fromServerId, roleId, clientFamilyRank);
    }

    private List<FamilyRankInfo> getFamilyRankInfo(LinkedHashMap<String, CrossServiceFamilyRank> rankLinkedHashMap, int n, String familyIdStr) {
        List<FamilyRankInfo> infoList = new ArrayList<>();
        LinkedList<CrossServiceFamilyRank> rankList = new LinkedList<>();
        Collection<CrossServiceFamilyRank> rankList0 = rankLinkedHashMap.values();
        rankList.addAll(rankList0);
        Collections.sort(rankList);
        for (int i = 0; i < rankList.size() && i < n; i++) {
            CrossServiceFamilyRank familyRank = rankList.get(i);
            if (dissolveFamilyMap.containsKey(Long.parseLong(familyRank.getFamilyId())))
                continue;
            FamilyRankInfo rankInfo = createRankInfo(familyRank, i + 1);
            if (rankInfo == null)
                continue;
            infoList.add(rankInfo);
        }
        FamilyRankInfo myFamilyRankInfo = createRankInfo(rankLinkedHashMap.get(familyIdStr), rankList.indexOf(rankLinkedHashMap.get(familyIdStr)) + 1);
        if (myFamilyRankInfo != null) {
            infoList.add(myFamilyRankInfo);
        }
        return infoList;
    }

    private FamilyRankInfo createRankInfo(CrossServiceFamilyRank familyRank, int rank) {
        if (familyRank == null) {
            return null;
        }
        FamilyRankInfo rankInfo = new FamilyRankInfo();
        rankInfo.setFamilyId(familyRank.getFamilyId());
        rankInfo.setFamilyName(familyRank.getFamilyName());
        rankInfo.setMasterName(familyRank.getMasterName());
        rankInfo.setServerId(familyRank.getServerId());
        rankInfo.setType(familyRank.getType());
        rankInfo.setFightScore(familyRank.getFightScore());
        rankInfo.setRank(rank != -1 ? rank : this.familyRankList.size() + 1);
        return rankInfo;
    }

    private String getTimeDesc() {
        String tmp = DataManager.getGametext("familywar_rank_desc");
        String args1 = "";
        int args2;

        long now = System.currentTimeMillis();
        args1 = TimeUtil.getDateMMDDHHmm(timeStart);
        if (now >= timeStart && now <= timeEnd) {
            args2 = FamilyWarConst.familyCount;
        } else {
            args2 = FamilyActWarManager.getFamilyCount(serverSet.size());
        }
        return String.format(tmp, args1, args2);
    }

    @Override
    public void delete(int serverId, long familyId) {
        familyRankList.remove(Long.toString(familyId));
        DissolveFamily dissolveFamily = new DissolveFamily();
        dissolveFamily.setFamilyId(familyId);
        rowDao.insert(dissolveFamily);
        rowDao.flush();
        dissolveFamilyMap.put(familyId, dissolveFamily);
        LogUtil.info("跨服家族排行榜|{} 家族解散了 keySet:{} ", familyId, dissolveFamilyMap.keySet());
    }

    @Override
    public void debug(int serverId, int period, int size) {
        RankConst.period = period;
        RankConst.size = size;
        LogUtil.info("跨服家族排行榜|调整定时器 period:{},size:{}", RankConst.period, size);
    }

    @Override
    public void printState() {

    }


}
