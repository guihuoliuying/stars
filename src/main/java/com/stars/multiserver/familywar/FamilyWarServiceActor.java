package com.stars.multiserver.familywar;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.SchedulerHelper;
import com.stars.bootstrap.ServerManager;
import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.core.persist.DbRowDao;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.ExcutorKey;
import com.stars.core.schedule.SchedulerManager;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.gm.FamilyWarRemoteGmHandler;
import com.stars.modules.familyactivities.war.packet.ClientFamilyWarMainIcon;
import com.stars.modules.familyactivities.war.packet.ui.ClientFamilyWarUiApply;
import com.stars.modules.familyactivities.war.packet.ui.ClientFamilyWarUiFlowInfo;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.PktAuxFamilyWarApplicant;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.data.FamilyWarFixture;
import com.stars.multiserver.familywar.flow.FamilyWarFlow;
import com.stars.multiserver.familywar.flow.FamilyWarFlowInfo;
import com.stars.multiserver.familywar.flow.FamilyWarKnockoutFlow;
import com.stars.multiserver.familywar.knockout.KnockoutFamilyInfo;
import com.stars.multiserver.familywar.knockout.KnockoutFamilyMemberInfo;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.activities.ActConst;
import com.stars.services.family.FamilyPost;
import com.stars.services.family.main.FamilyData;
import com.stars.services.family.main.userdata.FamilyMemberPo;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.util._HashMap;
import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static com.stars.modules.familyactivities.war.FamilyActWarManager.*;

/**
 * Created by chenkeyu on 2017-04-27 20:55
 */
public class FamilyWarServiceActor extends ServiceActor implements FamilyWarService {
    private DbRowDao dao;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, FamilyWarFixture>> familyWarFixtureMaps;
    private FamilyWarFlow generalFlow;
    private FamilyWarKnockoutFlow knockoutFlow;
    private volatile Set<Long> onlineRoleIds;
    private volatile Set<Long> offlineRoleIds;
    private Lock lock;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.FamilyWarService, this);
        generalFlow = new FamilyWarFlow();
        generalFlow.setService(this);
        generalFlow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_GENERAL));
        knockoutFlow = new FamilyWarKnockoutFlow();
        knockoutFlow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_LOCAL));
        onlineRoleIds = new HashSet<>();
        offlineRoleIds = new HashSet<>();
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.FamilyWar, new Runnable() {
            @Override
            public void run() {
                ServiceHelper.familyWarService().SyncRoleStateToFamilyWarServer();
            }
        }, 1, 1, TimeUnit.SECONDS);
        dao = new DbRowDao("FamilyWarServeice", DBUtil.DB_COMMON);
        String sql = "select wartype, serverid,fixturefamily from familywarfixture";
        List<_HashMap> hashMaps = DBUtil.queryList(DBUtil.DB_COMMON, _HashMap.class, sql);
        familyWarFixtureMaps = new ConcurrentHashMap<>();
        for (_HashMap hashMap : hashMaps) {
            int warType = hashMap.getByte("familywarfixture.wartype");
            int serverId = hashMap.getInt("familywarfixture.serverid");
            String fixtureFamilyStr = hashMap.getString("familywarfixture.fixturefamily");
            FamilyWarFixture fixture = new FamilyWarFixture(warType, serverId);
            fixture.setFixtureFamilyMap(fixtureFamilyStr);
            changeFixture(warType, fixture);
        }
    }

    private void changeFixture(int warType, FamilyWarFixture fixture) {
        ConcurrentHashMap<Integer, FamilyWarFixture> fixtureMap = familyWarFixtureMaps.get(warType);
        if (fixtureMap == null) {
            fixtureMap = new ConcurrentHashMap<>();
            familyWarFixtureMaps.put(warType, fixtureMap);
        }
        fixtureMap.put(fixture.getServerId(), fixture);
    }

    public void updateFamilyWarFixture(int serverId, int warType, Map<Integer, Long> fixFamily) {
        ConcurrentHashMap<Integer, FamilyWarFixture> fixtureMap = familyWarFixtureMaps.get(warType);
        if (fixtureMap == null) {
            fixtureMap = new ConcurrentHashMap<>();
            familyWarFixtureMaps.put(warType, fixtureMap);
        }
        if (fixtureMap.get(serverId) == null) {
            FamilyWarFixture fixture = new FamilyWarFixture(warType, serverId);
            for (Map.Entry<Integer, Long> entry : fixFamily.entrySet()) {
                fixture.updateFixture(entry.getKey(), entry.getValue());
            }
            fixtureMap.put(fixture.getServerId(), fixture);
            dao.insert(fixture);
        } else {
            FamilyWarFixture fixture = fixtureMap.get(serverId);
            for (Map.Entry<Integer, Long> entry : fixFamily.entrySet()) {
                fixture.updateFixture(entry.getKey(), entry.getValue());
            }
            dao.update(fixture);
        }
        dao.flush();
    }

    @Override
    public FamilyWarFixture getFixture(int serverId, int warType) {
        return familyWarFixtureMaps.get(warType).get(serverId);
    }

    @Override
    public void save() {
        /*dao.flush();*/
    }

    private void initFlow(boolean initKnockout) {
        try {
            generalFlow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_GENERAL));
            if (initKnockout) {
                knockoutFlow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_LOCAL));
            }
        } catch (Exception e) {
            LogUtil.info("familywar|初始化流程数据异常");
        }

    }

    /**
     * 更新比赛流程状态
     *
     * @param warType
     * @param warState
     */
    public void updateFlowInfo(int serverId, int warType, byte warState) {
        FamilyWarFlowInfo warFlowInfo = FamilyWarConst.FAMILY_WAR_FLOW_INFO_MAP.get(warType);
        warFlowInfo.setState(warState);
    }

    @Override
    public void createFamilyInfo() {
        generalFlow.createFlowInfo();
        initFlow(true);
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_GENERAL);
        if (flowMap != null && !flowMap.isEmpty()) {
            long time = ActivityFlowUtil.getTimeInMillisByCronExprByWeek(flowMap.get(FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START));
            FamilyWarFlow.serverOpenDays = DataManager.getServerDays(time);
        }
    }

    /**
     * 调试用的
     *
     * @param serverId
     */
    @Override
    public void startQualify(int serverId, int warType) {
        generalFlow.createFlowInfo();
    }

    @Override
    public void startQualifyByDisaster(int serverId) {
        FamilyWarFlow.isMultiRunning = true;
        generalFlow.createFlowInfo();
        updateFlowInfo(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_QUALIFYING, FamilyWarConst.SHOW_APPLY_BUTTON);
        int preStep = FamilyWarUtil.getPreStep(ActConst.ID_FAMILY_WAR_QUALIFYING);
        if (preStep == -1)
            throw new IllegalArgumentException("拿不到步数 step:" + preStep);
        ServiceHelper.familyWarService().AsyncBattleType(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_QUALIFYING, FamilyWarFlow.STEP_REMOTE_QUALIFYING_START, preStep, true);
        MainRpcHelper.familyWarQualifyingService().startByDisaster(FamilyWarUtil.getFamilyWarServerId());
    }

    @Override
    public void startRemoteByDisaster(int serverId) {
        FamilyWarFlow.isMultiRunning = true;
        generalFlow.createFlowInfo();
        updateFlowInfo(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_REMOTE, FamilyWarConst.SHOW_APPLY_BUTTON);
        int preStep = FamilyWarUtil.getPreStep(ActConst.ID_FAMILY_WAR_REMOTE);
        if (preStep == -1)
            throw new IllegalArgumentException("拿不到步数 step:" + preStep);
        ServiceHelper.familyWarService().AsyncBattleType(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_REMOTE, FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START, preStep, true);
        MainRpcHelper.familyWarRemoteService().startByDisaster(FamilyWarUtil.getFamilyWarServerId());
    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出:{},familyWarFixtureMaps:{}", this.getClass().getSimpleName(), familyWarFixtureMaps.size());
    }

    @Override
    public void startQualifying(int serverId) {
        //每个服取前24名(防止不足24个家族参加跨服海选)
        LogUtil.info("familywar|各服取前24名(防止不足24个家族参加跨服海选)");
        List<AbstractRankPo> list = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_FAMILYFIGHTSCORE,
                FamilyWarConst.QUALIFYING_FAMILY_COUNT);
        //跨服RPC
        BootstrapConfig config = ServerManager.getServer().getConfig();
        Properties props = config.getProps().get("familywar");
        MainRpcHelper.familyWarQualifyingService().generateFamilyData(Integer.parseInt(props.getProperty("serverId")),
                MultiServerHelper.getServerId(), new ArrayList<>(list));
    }

    @Override
    public void setOptions(int serverId, int activityId, int flag, int countdown, String text) {
        ServiceHelper.familyActEntryService().setOptions(activityId, flag, countdown, text);
    }

    @Override
    public void AsyncBattleType(int serverId, int battleType, int generalFlow, int subFlow, boolean isRunning) {
        FamilyWarConst.battleType = battleType;
        FamilyWarConst.STEP_OF_GENERAL_FLOW = generalFlow;
        if (battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW = subFlow;
            MainRpcHelper.familyWarQualifyingService().AsyncState(FamilyWarUtil.getFamilyWarServerId(), battleType, generalFlow, subFlow, isRunning);
        } else if (battleType == FamilyWarConst.W_TYPE_REMOTE) {
            FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW = subFlow;
            MainRpcHelper.familyWarRemoteService().AsyncState(FamilyWarUtil.getFamilyWarServerId(), battleType, generalFlow, subFlow, isRunning);
        }
        LogUtil.info("familywar|同步一下流程状态|battleType:{},flow:{}", FamilyWarConst.battleType, FamilyWarConst.STEP_OF_GENERAL_FLOW, isRunning);
    }

    @Override
    public void lockFamily(int serverId, long familyId) {
        // TODO: 2017-05-18 锁上家族 （跨服业务）
        ServiceHelper.familyMainService().lockFamily(familyId);
    }

    @Override
    public void halfLockFamily(int serverId, long familyId) {
        ServiceHelper.familyMainService().halfLockFamily(familyId);
    }

    @Override
    public void unLockFamily(int serverId, List<Long> familyIds) {
        for (Long familyId : familyIds) {
            ServiceHelper.familyMainService().unlockFamily(familyId);
        }
    }

    @Override
    public void sendAward(int serverId, long roleId, short eventType, int emailTemplateId, Map<Integer, Integer> toolMap) {
        ServiceHelper.localService().sendAward(serverId, roleId, eventType, emailTemplateId, toolMap);
    }

    @Override
    public void sendEmailToSingle(int serverId, long roleId, int templateId, Long senderId, String senderName, Map<Integer, Integer> affixMap, String... params) {
        ServiceHelper.emailService().sendToSingle(roleId, templateId, senderId, senderName, affixMap, params);
    }

    @Override
    public void sendApplicationSheet(int serverId, long roleId, Map<Long, PktAuxFamilyWarApplicant> applicantMap, ClientFamilyWarUiApply packet) {
        for (Map.Entry<Long, PktAuxFamilyWarApplicant> entry : applicantMap.entrySet()) {
            Summary summary = ServiceHelper.summaryService().getSummary(entry.getKey());
            if (summary == null || summary.isDummy()) continue;
            entry.getValue().setOnline(summary.isOnline());
            entry.getValue().setElapseFromOffline(summary.getOfflineTimestamp());
            packet.addApplicant(entry.getValue());
        }
        ServiceHelper.roleService().send(roleId, packet);
    }

    @Override
    public void chat(int serverId, String title, byte channel, long sender, long receiverId, String content, boolean hasObject) {
        ServiceHelper.chatService().chat(title, channel, sender, receiverId, content, hasObject);
    }

    @Override
    public void announce(int serverId, String message) {
        ServiceHelper.chatService().announce(message);
    }

    @Override
    public void sendMainIcon(int serverId, ClientFamilyWarMainIcon mainIcon, HashSet<Long> familyList, Set<Long> failFamilyList) {
        LogUtil.info("familywar|发送全服icon serverId:{},familyList:{},failFamilyList:{}", serverId, familyList, failFamilyList);
        for (Actor actor : PlayerSystem.system().getActors().values()) {
            if (actor instanceof Player) {
                Player player = (Player) actor;
                long familyId = ServiceHelper.familyRoleService().getFamilyId(player.id());
                mainIcon.setQualification(isQualifyingFamily(familyList, failFamilyList, familyId) ? FamilyWarConst.WITH_QUALIFICATION : FamilyWarConst.WITHOUT_QUALIFICATION);
                ServiceHelper.roleService().send(player.id(), mainIcon);
            }
        }
    }

    private boolean isQualifyingFamily(Collection<Long> familyList, Set<Long> failFamilyList, long familyId) {
        return familyList.contains(familyId) && !failFamilyList.contains(familyId);
    }

    @Override
    public void getOnRankFamily(int serverId, int type, List<Long> familyIds) {
        List<FamilyData> familyDataList = new ArrayList<>();
        for (long familyId : familyIds) {
            try {
                FamilyData familyData = ServiceHelper.familyMainService().getFamilyDataClone(familyId);
                if (familyData != null && StringUtil.isNotEmpty(familyData.getMemberPoMap())) {
                    familyDataList.add(familyData);
                    ServiceHelper.familyMainService().halfLockFamily(familyId);
                }
            } catch (Exception e) {
                LogUtil.info("familywar| {} 家族数据获取异常", familyId);
                e.printStackTrace();
            }
        }
        familyInfo(familyDataList, type);
    }

    @Override
    public void modifyConnectorRoute(int mainServerId, long roleId, int fightServerId) {
        MultiServerHelper.modifyConnectorRoute(roleId, fightServerId);
    }

    private void familyInfo(List<FamilyData> familyDataList, int type) {
        List<KnockoutFamilyInfo> infoList = new ArrayList<>();
        for (int i = 0; i < familyDataList.size(); i++) {
            infoList.add(generateFamilyInfo(familyDataList.get(i), i));
        }
        BootstrapConfig config = ServerManager.getServer().getConfig();
        Properties props = config.getProps().get("familywar");
        if (type == FamilyWarConst.W_TYPE_QUALIFYING) {
            MainRpcHelper.familyWarQualifyingService().onRankFamilyData(Integer.parseInt(props.getProperty("serverId")), MultiServerHelper.getServerId(), new ArrayList<>(infoList));
        } else if (type == FamilyWarConst.W_TYPE_REMOTE) {
            MainRpcHelper.familyWarRemoteService().onRankFamilyData(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), new ArrayList<>(infoList));
        }
    }

    private KnockoutFamilyInfo generateFamilyInfo(FamilyData data, int rank) {
        LogUtil.info("familyId:{}, FamilyDataMemberSize:{},member:{}", data.getFamilyPo().getFamilyId(), data.getMemberPoMap().size(), data.getMemberPoMap().keySet());
        KnockoutFamilyInfo familyInfo = new KnockoutFamilyInfo();
        familyInfo.setFamilyId(data.getFamilyPo().getFamilyId());
        familyInfo.setRank(rank);
        familyInfo.setFamilyName(data.getFamilyPo().getName());
        familyInfo.setTotalFightScore(data.getFamilyPo().getTotalFightScore());
        familyInfo.setMainServerId(MultiServerHelper.getServerId());
        familyInfo.setWinner(true);

        //按职位，战力从高到低排序
        List<FamilyMemberPo> memberList = new ArrayList<>(data.getMemberPoMap().values());
        Collections.sort(memberList, new Comparator<FamilyMemberPo>() {
            @Override
            public int compare(FamilyMemberPo o1, FamilyMemberPo o2) {
                if (o1.getPostId() > o2.getPostId()) {
                    return 1;
                } else if (o1.getPostId() < o2.getPostId()) {
                    return -1;
                } else {
                    return o2.getRoleFightScore() - o1.getRoleFightScore();
                }
            }
        });
        List<String> componentName = new ArrayList<>();
        componentName.add(MConst.Role);
        componentName.add(MConst.Skill);
        componentName.add(MConst.Deity);
        componentName.add(MConst.Buddy);
        for (FamilyMemberPo memberPo : memberList) {
            try {
                Summary summary = getSummary(memberPo);
                if (summary == null || isDummy(summary, componentName)) continue;
                if (SpecialAccountManager.isSpecialAccount(summary.getRoleId())) continue;
                KnockoutFamilyMemberInfo memberInfo = new KnockoutFamilyMemberInfo();
                memberInfo.setMainServerId(familyInfo.getMainServerId());
                memberInfo.setFamilyId(memberPo.getFamilyId());
                memberInfo.setMemberId(memberPo.getRoleId());
                memberInfo.setName(memberPo.getRoleName());
                memberInfo.setPostId(memberPo.getPostId());
                memberInfo.setLevel(memberPo.getRoleLevel());
                memberInfo.setFightScore(memberPo.getRoleFightScore());
                memberInfo.setFighterEntity(FighterCreator.createBySummary((byte) 1, summary).get(Long.toString(memberPo.getRoleId())));
                familyInfo.getMemberMap().put(memberPo.getRoleId(), memberInfo);
                ServiceHelper.emailService().sendToSingle(memberInfo.getMemberId(), emailTemplateIdOfFamilyBeChosen, 0L, "系统", null);
                if (memberInfo.getPostId() == FamilyPost.MASTER_ID
                        || memberInfo.getPostId() == FamilyPost.ASSISTANT_ID
                        || memberInfo.getPostId() == FamilyPost.ELDER_ID) {
                    familyInfo.getApplicationSheet().add(memberInfo.getMemberId());
                    if (familyInfo.getTeamSheet().size() < FamilyActWarManager.numOfFighterInEliteFight) {
                        familyInfo.getTeamSheet().add(memberInfo.getMemberId());
                        ServiceHelper.emailService().sendToSingle(memberInfo.getMemberId(), emailTemplateIdOfAddingToTeamSheet, 0L, "系统", null);
                    }
                    if (memberInfo.getPostId() == FamilyPost.MASTER_ID) {    //设置族长id
                        familyInfo.setMasterId(memberInfo.getMemberId());
                    }
                }
            } catch (Exception e) {
                LogUtil.info("familywar|成员:{} 初始化失败|:{}", memberPo.getRoleId(), e);
            }
        }
        int lackSize = FamilyActWarManager.numOfFighterInEliteFight - familyInfo.getTeamSheet().size();
        if (lackSize > 0) {
            for (FamilyMemberPo memberPo : memberList) {
                if (familyInfo.getTeamSheet().contains(memberPo.getRoleId())) continue;
                Summary summary = getSummary(memberPo);
                if (summary == null || isDummy(summary, componentName)) continue;
                familyInfo.getApplicationSheet().add(memberPo.getRoleId());
                familyInfo.getTeamSheet().add(memberPo.getRoleId());
                ServiceHelper.emailService().sendToSingle(memberPo.getRoleId(), emailTemplateIdOfAddingToTeamSheet, 0L, "系统", null);
                lackSize--;
                if (lackSize <= 0) break;
            }
        }
        StringBuffer roleNames = new StringBuffer();
        KnockoutFamilyMemberInfo memberInfo;
        for (long roleId : familyInfo.getTeamSheet()) {
            memberInfo = familyInfo.getMemberMap().get(roleId);
            if (memberInfo == null) continue;
            roleNames.append(memberInfo.getName()).append("\\n");
        }
        ServiceHelper.emailService().sendToSingle(familyInfo.getMasterId(), emailTemplateIdOfTellTeamSheetToMaster, 0L, "系统", null, roleNames.toString());
        LogUtil.info("familyId:{},familyInfoMemberSize:{},member:{}", familyInfo.getFamilyId(), familyInfo.getMemberMap().size(), familyInfo.getMemberMap().keySet());
        return familyInfo;
    }

    private Summary getSummary(FamilyMemberPo memberPo) {
        try {
            return ServiceHelper.summaryService().getSummary(memberPo.getRoleId());
        } catch (Exception e) {
            LogUtil.info("familywar|getSummaryException:{}", e.toString());
        }
        return null;
    }

    private boolean isDummy(Summary summary, List<String> componentName) {
        for (Map.Entry<String, SummaryComponent> componentEntry : summary.getComponentMap().entrySet()) {
            if (componentName.contains(componentEntry.getKey())) {
                if (componentEntry.getValue().isDummy()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void changeServerState(int serverId, byte qualificationState) {
        FamilyWarConst.qualificationState = qualificationState;
    }

    @Override
    public void containFamily(int serverId, long familyId, long roleId, byte qualificationState) {
        ClientFamilyWarUiFlowInfo flowInfo = new ClientFamilyWarUiFlowInfo();
        flowInfo.setFamilyState(qualificationState);
        ServiceHelper.roleService().send(roleId, flowInfo);
    }

    @Override
    public void updateFighterEntity(int serverId, int warType, Set<Long> roleIds) {
        Map<Long, FighterEntity> entityMap = new HashMap<>();
        for (long roleId : roleIds) {
            try {
                Summary summary = ServiceHelper.summaryService().getSummary(roleId);
                FighterEntity fighterEntity = FighterCreator.createBySummary((byte) 1, summary).get(Long.toString(roleId));
                entityMap.put(roleId, fighterEntity);
            } catch (Exception e) {
                LogUtil.info("familywar|跨服 {} 拿取 {} 玩家摘要数据出现异常|e", warType, roleId, e);
                e.printStackTrace();
            }
        }
        if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            MainRpcHelper.familyWarQualifyingService().updateFighterEntity(FamilyWarUtil.getFamilyWarServerId(), entityMap);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            MainRpcHelper.familyWarRemoteService().updateFighterEntity(FamilyWarUtil.getFamilyWarServerId(), entityMap);
        }
    }

    @Override
    public void roleOnline(long roleId) {
        onlineRoleIds.add(roleId);
    }

    @Override
    public void roleOffline(long roleId) {
        offlineRoleIds.add(roleId);
    }

    public void SyncRoleStateToFamilyWarServer() {
        SyncRoleState(onlineRoleIds, true);
        SyncRoleState(offlineRoleIds, false);
    }

    private void SyncRoleState(Set<Long> set, boolean isOnline) {
        if (set.isEmpty()) return;
        try {
            MainRpcHelper.familyWarQualifyingService().roleState(FamilyWarUtil.getFamilyWarServerId(), set, isOnline);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        set.clear();
    }

    @Override
    public void dailyCheck() {
        int serverOpenday = DataManager.getServerDays();
        int maxDay = FamilyActWarManager.familywar_cycletime_max;
        LogUtil.info("serverOpenDay:{},maxDay:{}", serverOpenday, maxDay);
        if (serverOpenday > maxDay) {
            MainRpcHelper.familywarRankService().connectServer(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId());
        }
    }

    @Override
    public void onCallMainServer(int serverId) {
        List<AbstractRankPo> list = ServiceHelper.rankService().getRankingList(RankConstant.RANKID_FAMILYFIGHTSCORE);
        MainRpcHelper.familywarRankService().mainServerFamilyData(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), new ArrayList<>(list));
    }

    /**
     * 给Gm接口调试用的
     *
     * @param serverId
     * @param battleType
     */
    @Override
    public void AsyncBattle(int serverId, int battleType) {
        FamilyWarRemoteGmHandler.battleType = battleType;
        FamilyWarConst.remoteType = battleType;
        LogUtil.info("familywar|回调给游戏服:{}", battleType);
    }
}
