package com.stars.multiserver.familywar;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.player.PlayerUtil;
import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.event.FamilyWarFighterAddingSucceededEvent;
import com.stars.modules.familyactivities.war.packet.ClientFamilyWarMainIcon;
import com.stars.modules.familyactivities.war.packet.ui.ClientFamilyWarUiFixtures;
import com.stars.modules.familyactivities.war.packet.ui.ClientFamilyWarUiMinPointsAward;
import com.stars.modules.familyactivities.war.prodata.FamilyWarRankAwardVo;
import com.stars.modules.pk.packet.ServerOrder;
import com.stars.modules.rank.RankManager;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFamilyWarEliteFight;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterPK;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.data.FamilyWarFixture;
import com.stars.multiserver.familywar.flow.FamilyWarFlow;
import com.stars.multiserver.familywar.flow.FamilyWarKnockoutFlow;
import com.stars.multiserver.familywar.flow.FamilyWarQualifyingFlow;
import com.stars.multiserver.familywar.flow.FamilyWarRemoteFlow;
import com.stars.multiserver.familywar.knockout.FamilyWarKnockout;
import com.stars.multiserver.familywar.knockout.FamilyWarKnockoutBattle;
import com.stars.multiserver.familywar.knockout.KnockoutFamilyInfo;
import com.stars.multiserver.familywar.knockout.KnockoutFamilyMemberInfo;
import com.stars.multiserver.fight.ServerOrders;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.activities.ActConst;
import com.stars.services.family.FamilyConst;
import com.stars.services.family.FamilyPost;
import com.stars.services.family.main.FamilyData;
import com.stars.services.family.main.userdata.FamilyMemberPo;
import com.stars.services.localservice.LocalService;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.prodata.RankAwardVo;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.util.TimeUtil;
import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.stars.modules.familyactivities.war.FamilyActWarManager.*;

/**
 * Created by zhaowenshuo on 2016/12/6.
 */
public class FamilyWarLocalServiceActor extends ServiceActor implements FamilyWarLocalService {

//    public byte state = W_STATE_LOCAL; // 未开始/本服赛/海选战/跨服战/已结束

    private FamilyWarKnockout knockout;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.FamilyWarLocalService, this);

        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.FamilyWar, new Runnable() {
            @Override
            public void run() {
                ServiceHelper.familyWarLocalService().syncBattleFightUpdateInfo();
            }
        }, 10, 1, TimeUnit.SECONDS);
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.FamilyWar, new Runnable() {
            @Override
            public void run() {
                ServiceHelper.familyWarLocalService().checkAndEndTimeout();
            }
        }, 1, 1, TimeUnit.SECONDS);
        generateStepList();
    }

    @Override
    public void printState() {
        if (knockout == null) return;
        knockout.printState();
    }

    @Override
    public void AsyncFihterEntity() {
        if (knockout == null) return;
        knockout.AsyncFihterEntity();
    }

    @Override
    public void sendApplicationSheet(long familyId, long roleId) {
        switch (FamilyWarConst.STEP_OF_GENERAL_FLOW) {
            case FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START:
                if (knockout == null) {
                    ServiceHelper.roleService().warn(MultiServerHelper.getServerId(), roleId, "比赛尚未开始");
                    return;
                }
                knockout.sendApplicationSheet(MultiServerHelper.getServerId(), familyId, roleId);
                break;
            case FamilyWarFlow.STEP_REMOTE_QUALIFYING_START:
                break;
            default:
                ServiceHelper.roleService().warn(MultiServerHelper.getServerId(), roleId, "比赛尚未开始");
                break;
        }
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "请求家族战信息", true));
        }
    }

    @Override
    public void apply(long familyId, long roleId) {
        switch (FamilyWarConst.STEP_OF_GENERAL_FLOW) {
            case FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START:
                knockout.applyEliteFightSheet(MultiServerHelper.getServerId(), familyId, roleId);
                break;
            case FamilyWarFlow.STEP_REMOTE_QUALIFYING_START:
                break;
        }
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "报名家族战", true));
        }
    }

    @Override
    public void cancelApply(long familyId, long roleId) {
        switch (FamilyWarConst.STEP_OF_GENERAL_FLOW) {
            case FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START:
                knockout.cancelApplyEliteFightSheet(MultiServerHelper.getServerId(), familyId, roleId);
                break;
            case FamilyWarFlow.STEP_REMOTE_QUALIFYING_START:
                break;
        }
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "取消报名家族战", true));
        }
    }

    @Override
    public void confirmTeamSheet(long familyId, long roleId, Set<Long> teamSheet) {
        switch (FamilyWarConst.STEP_OF_GENERAL_FLOW) {
            case FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START:
                knockout.confirmTeamSheet(MultiServerHelper.getServerId(), familyId, roleId, teamSheet);
                break;
            case FamilyWarFlow.STEP_REMOTE_QUALIFYING_START:
                break;
        }
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "确认家族战名单", true));
        }
    }

    @Override
    public void sendFixtures(long familyId, long roleId) {
        if (knockout == null) {
            ClientFamilyWarUiFixtures packet = new ClientFamilyWarUiFixtures(ClientFamilyWarUiFixtures.SUBTYPE_ALL);
            packet.setWarType(ClientFamilyWarUiFixtures.T_LOCAL);
            ServiceHelper.roleService().send(roleId, packet);
            return;
        }
        knockout.sendFixtures(MultiServerHelper.getServerId(), familyId, roleId);
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "发送家族战赛程信息", true));
        }
    }

    @Override
    public void sendUpdatedFixtures(long familyId, long roleId) {
        knockout.sendUpdatedFixtures(MultiServerHelper.getServerId(), familyId, roleId);
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "更新家族战赛程信息", true));
        }
    }

    @Override
    public void enterFight(long familyId, long roleId, FighterEntity fighterEntity) {
        LogUtil.info("familywar|进入战斗场景|总赛程阶段:{}|子赛程阶段:{}", FamilyWarConst.STEP_OF_GENERAL_FLOW, FamilyWarConst.STEP_OF_SUB_FLOW);
        switch (FamilyWarConst.STEP_OF_GENERAL_FLOW) {
            case FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START:
                LogUtil.info("familywar|serverActor==家族:{}的玩家:{}进入战场", familyId, roleId);
                knockout.enter(MultiServerHelper.getServerId(), MultiServerHelper.getServerId(),
                        familyId, roleId, fighterEntity);
                break;
            case FamilyWarFlow.STEP_REMOTE_QUALIFYING_START:
                break;
        }
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "进入家族战", true));
        }
    }

    @Override
    public void enterSafeScene(long roleId) {
        LogUtil.info("familywar|进入备战场景|总赛程阶段:{}|子赛程阶段:{}", FamilyWarConst.STEP_OF_GENERAL_FLOW, FamilyWarConst.STEP_OF_SUB_FLOW);
        switch (FamilyWarConst.STEP_OF_GENERAL_FLOW) {
            case FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START:
                knockout.enterSafeScene(MultiServerHelper.getServerId(), MultiServerHelper.getServerId(),
                        roleId);
                break;
            case FamilyWarFlow.STEP_REMOTE_QUALIFYING_START:
                break;
        }
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "进入家族战安全区", true));
        }
    }

    @Override
    public void enterSafeScene(long familyId, long roleId) {
        LogUtil.info("familywar|进入备战场景|总赛程阶段:{}|子赛程阶段:{}", FamilyWarConst.STEP_OF_GENERAL_FLOW, FamilyWarConst.STEP_OF_SUB_FLOW);
        switch (FamilyWarConst.STEP_OF_GENERAL_FLOW) {
            case FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START:
                knockout.enterSafeScene(MultiServerHelper.getServerId(), MultiServerHelper.getServerId(),
                        familyId, roleId);
                break;
            case FamilyWarFlow.STEP_REMOTE_QUALIFYING_START:
                break;
        }
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "进入家族战安全区", true));
        }
    }

    @Override
    public void getFixtures(boolean isStr, String text) {
        LogUtil.info("familywar|发送赛程=========isStr:{},text:{}", isStr, text);
        if (FamilyWarConst.STEP_OF_SUB_FLOW == FamilyWarKnockoutFlow.STEP_END_FINALS) {
            String time = FamilyWarUtil.getBattleTimeStr(FamilyWarKnockoutFlow.STEP_END_KNOCKOUT, ActConst.ID_FAMILY_WAR_LOCAL);
            String tmpStr = String.format(DataManager.getGametext("familywar_desc_awardtime"), time);
            ServiceHelper.familyActEntryService().setOptions(ActConst.ID_FAMILY_WAR_GENERAL,
                    FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, tmpStr);
            return;
        }
        if (FamilyWarConst.STEP_OF_SUB_FLOW == FamilyWarKnockoutFlow.STEP_END_KNOCKOUT) {
            ServiceHelper.familyActEntryService().setOptions(ActConst.ID_FAMILY_WAR_GENERAL,
                    FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, "已结束");
            return;
        }
        if (isStr) {
            ServiceHelper.familyActEntryService().setOptions(ActConst.ID_FAMILY_WAR_GENERAL,
                    FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, text);
        } else {
            long time = FamilyWarUtil.getNearBattleTimeL(ActConst.ID_FAMILY_WAR_LOCAL);
            StringBuilder tmpStr0 = TimeUtil.getChinaShow(time);
            String tmpStr = String.format(DataManager.getGametext("familywar_desc_fightbegintime"), tmpStr0.toString());
            ServiceHelper.familyActEntryService().setOptions(ActConst.ID_FAMILY_WAR_GENERAL,
                    FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, tmpStr);
            LogUtil.info("familywar|timStr:{}", tmpStr);
        }
    }

    @Override
    public void cancelFight(long familyId, long roleId) {
        switch (FamilyWarConst.STEP_OF_GENERAL_FLOW) {
            case FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START:
                knockout.cancelNormalFightWaitingQueue(MultiServerHelper.getServerId(), MultiServerHelper.getServerId(),
                        familyId, roleId);
                break;
            case FamilyWarFlow.STEP_REMOTE_QUALIFYING_START:
                break;
        }
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "取消进入家族战", true));
        }
    }

    @Override
    public void sendPointsRank(long roleId, byte subtype) {
        if (knockout == null) return;
        switch (FamilyWarConst.STEP_OF_GENERAL_FLOW) {
            case FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START:
                knockout.sendPointsRank(MultiServerHelper.getServerId(), roleId, subtype, ClientFamilyWarUiFixtures.T_LOCAL);
                break;
            case FamilyWarFlow.STEP_REMOTE_QUALIFYING_START:
                break;
        }
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "发送家族战积分排行榜", true));
        }
    }

    @Override
    public void checkAndEndTimeout() {
        if (knockout != null) {
            knockout.checkAndEndTimeout();
        }
    }

    @Override
    public void sendPointsRankAward(int mainServerId, boolean isLocal, boolean isElite, Map<Long, Integer> rankMap) {
        int rankId = 0;
        if (isElite) {
            rankId = rankAwardIdOfLocalEliteFight;
        }
        if (!isElite) {
            rankId = rankAwardIdOfLocalNormalFight;
        }
        for (Map.Entry<Long, Integer> entry : rankMap.entrySet()) {
            RankAwardVo vo = RankManager.getRankAwardVo(rankId, entry.getValue());
            if (vo != null) {
                try {
                    LogUtil.info("familywar|本服个人积分发奖|roleId:{},rank:{},emailId:{}", entry.getKey(), entry.getValue(), vo.getEmail());
                    ServiceHelper.emailService().sendToSingle(
                            entry.getKey(), vo.getEmail(), 0L, "系统", vo.getRewardMap(), entry.getValue().toString());
                } catch (Exception e) {
                    LogUtil.error("familywar|个人积分发奖失败:{},异常信息:{}", entry.getKey(), e);
                }
            }
        }
    }

    @Override
    public void viewMinPointsAward(long roleId) {
        if (knockout == null) {
            ClientFamilyWarUiMinPointsAward packet = new ClientFamilyWarUiMinPointsAward(
                    ClientFamilyWarUiMinPointsAward.SUBTYPE_VIEW, FamilyWarConst.MIN_AWARD_ELITE, 0, null);
            ServiceHelper.roleService().send(MultiServerHelper.getServerId(), roleId, packet);
            return;
        }
        switch (FamilyWarConst.STEP_OF_GENERAL_FLOW) {
            case FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START:
                knockout.viewMinPointsAward(MultiServerHelper.getServerId(), roleId);
                break;
            case FamilyWarFlow.STEP_REMOTE_QUALIFYING_START:
                break;
        }
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "查看家族战最小积分奖励", true));
        }
    }

    @Override
    public void acquireMinPointsAward(long roleId, long acquirePoints) {
        if (knockout == null) return;
        switch (FamilyWarConst.STEP_OF_GENERAL_FLOW) {
            case FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START:
                knockout.acquireMinPointsAward(MultiServerHelper.getServerId(), roleId, acquirePoints);
                break;
            case FamilyWarFlow.STEP_REMOTE_QUALIFYING_START:
                break;
        }
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "家族战最小达标积分奖励", true));
        }
    }

    @Override
    public void sendFamilyRankAward(int mainServerId, String familyName, int rank, Map<Long, Integer> rankAwardMap) {
        for (Map.Entry<Long, Integer> entry : rankAwardMap.entrySet()) {
            long roleId = entry.getKey();
            int voId = entry.getValue();
            FamilyWarRankAwardVo vo = familyRankAwardVoMap.get(voId);
            if (vo != null) {
                ServiceHelper.emailService().sendToSingle(roleId, vo.getTemplateId(), 0L, "系统", vo.getToolMap());
            }
        }
    }

    @Override
    public void start(int mainServerId) {
        List<FamilyData> familyDataList = getFamilyData();
//        FamilyWarFixture fixtures = new FamilyWarFixture(FamilyWarConst.W_TYPE_LOCAL, MultiServerHelper.getServerId());
//        ServiceHelper.familyWarService().addFamilyWarData(mainServerId, FamilyWarConst.W_TYPE_LOCAL, fixtures);
        knockout = generateKnockout(familyDataList);
        knockout.setFightService(MainRpcHelper.fightBaseService());
        knockout.setRoleService(ServiceHelper.roleService());
        knockout.setFamilyWarLocalService(ServiceHelper.familyWarLocalService());
        knockout.setLocalService((LocalService) ServiceHelper.getServiceByName(SConst.LocalService));
        knockout.generateFixtures();
        knockout.sendMainIconToMaster(FamilyWarConst.W_TYPE_LOCAL);
    }

    private List<FamilyData> getFamilyData() {
        List<FamilyData> familyDataList = new ArrayList<>();
        List<AbstractRankPo> list = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_FAMILYFIGHTSCORE, FamilyWarConst.KNOCKOUT_FAMILY_COUNT);
        for (AbstractRankPo rankPo : list) {
            try {
                FamilyData familyData = ServiceHelper.familyMainService().getFamilyDataClone(rankPo.getUniqueId());
                if (familyData != null && StringUtil.isNotEmpty(familyData.getMemberPoMap())) {
                    familyDataList.add(familyData);
                    ServiceHelper.familyMainService().halfLockFamily(rankPo.getUniqueId());
                }
            } catch (Exception e) {
                LogUtil.info("familywar| {} 家族数据获取异常", rankPo.getUniqueId());
            }
        }
        return familyDataList;
    }

    /**
     * 意外关服的处理
     */
    public void startByDisaster(int mainServerId) {
        LogUtil.info("familywar|Gm拉起家族战");
        FamilyWarFixture fixture = ServiceHelper.familyWarService().getFixture(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_LOCAL);
        Collection<Long> familyIds = fixture.getFixtureFamilyMap().values();
        LogUtil.info("familywar|familyIds:{}", familyIds);
        Set<Long> set = new HashSet<>(familyIds);
        List<FamilyData> familyDataList = getFamilyData(set);
        knockout = generateKnockout(familyDataList);
        knockout.generateFixtures(fixture.getFixtureFamilyMap());
        knockout.setFightService(MainRpcHelper.fightBaseService());
        knockout.setRoleService(ServiceHelper.roleService());
        knockout.setFamilyWarLocalService(ServiceHelper.familyWarLocalService());
        knockout.setLocalService((LocalService) ServiceHelper.getServiceByName(SConst.LocalService));
        knockout.sendMainIconToMaster(FamilyWarConst.W_TYPE_LOCAL);
        getFixtures(false, "");
    }

    private List<FamilyData> getFamilyData(Set<Long> familyIds) {
        List<FamilyData> familyDataList = new ArrayList<>();
        for (Long familyId : familyIds) {
            if (familyId == 0L)
                continue;
            FamilyData familyData = ServiceHelper.familyMainService().getFamilyDataClone(familyId);
            if (familyData != null && StringUtil.isNotEmpty(familyData.getMemberPoMap())) {
                familyDataList.add(familyData);
                ServiceHelper.familyMainService().halfLockFamily(familyId);
            }
        }
        return familyDataList;
    }

    /**
     * 初始化家族信息
     *
     * @param dataList
     * @return
     */
    private FamilyWarKnockout generateKnockout(List<FamilyData> dataList) {
        FamilyWarKnockout knockout = new FamilyWarKnockout();
        for (int i = 0; i < dataList.size(); i++) {
            KnockoutFamilyInfo familyInfo = generateFamilyInfo(dataList.get(i), i);
            knockout.addFamilyInfo(familyInfo);
        }
        LogUtil.info("familywar|家族战参战人员名单:{}", knockout.getMemberMap().keySet());
        return knockout;
    }

    /**
     * 初始化家族战的对战家族及成员信息
     *
     * @param data
     * @return
     */
    private KnockoutFamilyInfo generateFamilyInfo(FamilyData data, int rank) {
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
    public void end(int mainServerId) {

    }

    @Override
    public void onNormalFightCreationSucceeded(int mainServerId, int fightServerId, String battleId, String fightId) {
        knockout.onNormalFightCreationSucceeded(mainServerId, fightServerId, battleId, fightId);
    }

    @Override
    public void onNormalFightStarted(int mainServerId, int fightServerId, String battleId, String fightId) {
        knockout.onNormalFightStarted(mainServerId, fightServerId, battleId, fightId);
    }

    @Override
    public void onFightCreateFail(int mainServerId, int fightServerId, String battleId, String fightId, int warType) {
        knockout.onFightCreatFail(battleId, fightId, warType);
    }

    @Override
    public void generateFixtures() {
        knockout.generateFixtures();
    }

    @Override
    public void startQuarterFinals() {
        knockout.startQuarterFinals();
    }

    @Override
    public void endQuarterFinals() {
        knockout.endQuarterFinals();
    }

    @Override
    public void startSemiFinals() {
        knockout.startSemiFinals();
    }

    @Override
    public void endSemiFinals() {
        knockout.endSemiFinals();
    }

    @Override
    public void startFinal() {
        knockout.startFinal();
    }

    @Override
    public void endFinal() {
        knockout.endFinals();
    }

    @Override
    public void sendAward_ResetPoints_NoticeMater(int step, long countdown) {
        knockout.sendAward_ResetPoints_NoticeMater(step, countdown);
    }

    @Override
    public void sendTeamSheetChangedEmail(int mainServerId, long familyId, Set<Long> addTeamSheet, Set<Long> delTeamSheet) {
        //
        for (Long roleId : addTeamSheet) {
            ServiceHelper.emailService().sendToSingle(
                    roleId, emailTemplateIdOfAddingToTeamSheet, 0L, "系统", null);
        }
        //
        for (Long roleId : delTeamSheet) {
            ServiceHelper.emailService().sendToSingle(
                    roleId, emailTemplateIdOfDeletingFromTeamSheet, 0L, "系统", null);
        }
    }

    @Override
    public void sendEliteFightAward(int mainServerId, boolean isWin, int type, int count, String opponentFamilyName, Map<Long, Map<Integer, Integer>> fighterAwardMap) {
        if (isWin) {
            for (Map.Entry<Long, Map<Integer, Integer>> entry : fighterAwardMap.entrySet()) {
                ServiceHelper.emailService().sendToSingle(
                        entry.getKey(), emailTemplateIdOfEliteFightWinAward, 0L, "系统", entry.getValue(), String.valueOf(count), opponentFamilyName);
            }
        } else {
            for (Map.Entry<Long, Map<Integer, Integer>> entry : fighterAwardMap.entrySet()) {
                ServiceHelper.emailService().sendToSingle(
                        entry.getKey(), emailTemplateIdOfEliteFightLoseAward, 0L, "系统", entry.getValue(), String.valueOf(count), opponentFamilyName);
            }
        }
    }

    @Override
    public void sendNormalFightAward(int mainServerId, Map<Long, Map<Integer, Integer>> fighterAwardMap) {
        for (Map.Entry<Long, Map<Integer, Integer>> entry : fighterAwardMap.entrySet()) {
            ServiceHelper.emailService().sendToSingle(
                    entry.getKey(), emailTemplateIdOfNormalFightAward, 0L, "系统", entry.getValue());
        }

    }

    @Override
    public void handleEliteFightDamage(int mainServerId, String battleId, String fightId, Map<String, HashMap<String, Integer>> damageMap) {
        knockout.handleDamage(battleId, fightId, damageMap);
    }

    @Override
    public void handleEliteFightDead(int mainServerId, String battleId, String fightId, Map<String, String> deadMap) {
        knockout.handleDead(battleId, fightId, deadMap);
    }

    @Override
    public void handleNormalFightDamage(int mainServerId, String battleId, String fightId, Map<String, HashMap<String, Integer>> damageMap) {
        knockout.handleDamage(battleId, fightId, damageMap);
    }

    @Override
    public void handleNormalFightDead(int mainServerId, String battleId, String fightId, Map<String, String> deadMap) {
        knockout.handleDead(battleId, fightId, deadMap);
    }

    @Override
    public void handleStageFightDead(int mainServerId, String battleId, String fightId, Map<String, String> deadMap) {
        knockout.handleDead(battleId, fightId, deadMap);
    }

    @Override
    public void handleFighterQuit(int mainServerId, int fightServerId, String fightId, String battleId, long roleId, short type) {
        knockout.handleFighterQuit(battleId, roleId, fightId);
        ArrayList<String> entityKey = new ArrayList<>();
        entityKey.add(Long.toString(roleId));
        ServerOrder order = ServerOrders.newAiOrder(ServerOrder.OPEN_AI, entityKey);
        MainRpcHelper.fightBaseService().addServerOrder(fightServerId, type,
                mainServerId, fightId, order);
    }

//    @Override
//    public void handleFighterQuit(int mainServerId, String battleId, long roleId) {
//        knockout.handleFighterQuit(battleId, roleId);
//    }

    @Override
    public void onPremittedToEnter(int mainServerId, int fightServerId, String fightId, byte camp, long roleId) {
        StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfEliteFight);
        Summary summary = ServiceHelper.summaryService().getSummary(roleId);
        FighterEntity fighterEntity = FighterCreator.createBySummary((byte) 1, summary).get(Long.toString(roleId)); // fixme: camp id
        fighterEntity.setCamp(camp);
        if (camp == FamilyWarConst.K_CAMP1) {
            fighterEntity.setPosition(stageVo.getPosition());
            fighterEntity.setRotation(stageVo.getRotation());
        } else {
            fighterEntity.setPosition(stageVo.getEnemyPos(0));
            fighterEntity.setRotation(stageVo.getEnemyRot(0));
        }
        List<FighterEntity> list = new ArrayList<>();
        list.add(fighterEntity);
        ArrayList<String> entityKey = new ArrayList<>();
        entityKey.add(fighterEntity.getUniqueId());
        ServerOrder order = ServerOrders.newAiOrder(ServerOrder.CLOSE_AI, entityKey);
        LogUtil.info("添加玩家成功");
        MainRpcHelper.fightBaseService().addFighter(
                fightServerId,
                FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                mainServerId,
                fightId,
                list);
        MainRpcHelper.fightBaseService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                mainServerId, fightId, order);
    }

    @Override
    public void onFighterAddingSucceeded(int mainServerId, int fightServerId, String battleId, String fightId, byte camp, long roleId) {
        LogUtil.info("familywar|onFighterAddingSucceeded|battleId:{},battleMap:{}", battleId, knockout.getBattleMap().keySet());
        if (battleId == null) {
            long familyId = knockout.getMemberMap().get(roleId).getFamilyId();
            battleId = knockout.getFamilyMap().get(familyId).getBattleId();
            LogUtil.info("familywar|onFighterAddingSucceeded|a exception ,fightServer return a error battleId , get a new one:{}", battleId);
        }
        if (knockout.getFighterReviveState(battleId, fightId, Long.toString(roleId)) == 1) return;
        // 发进入包
        ClientEnterFamilyWarEliteFight packet = new ClientEnterFamilyWarEliteFight();
        packet.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_ELITE_FIGHT);
        packet.setStageId(FamilyActWarManager.stageIdOfEliteFight);
        packet.setLimitTime(getBattleEndRemainderTime(battleId));
        packet.setStartRemainderTime(getDynamicBlockRemainderTime(battleId));

        StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfEliteFight);

        Summary summary = ServiceHelper.summaryService().getSummary(roleId);
        FighterEntity fighterEntity = FighterCreator.createBySummary((byte) 1, summary).get(Long.toString(roleId));
        fighterEntity.setCamp(camp);
        if (camp == FamilyWarConst.K_CAMP1) {
            fighterEntity.setPosition(stageVo.getPosition());
            fighterEntity.setRotation(stageVo.getRotation());
        } else {
            fighterEntity.setPosition(stageVo.getEnemyPos(0));
            fighterEntity.setRotation(stageVo.getEnemyRot(0));
        }
        List<FighterEntity> list = new ArrayList<>();
        list.add(fighterEntity);
        packet.setFighterEntityList(list);
        Map<String, FighterEntity> nonPlayerEntity = getMonsterFighterEntity(stageIdOfEliteFight);

        packet.addMonsterVoMap(stageVo.getMonsterVoMap());
        /* 动态阻挡数据 */
        Map<String, Byte> blockStatus = new HashMap<>();
        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
            if (dynamicBlock.getShowSpawnId() == 0 && needDynamicBlock(battleId)) {
                blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
            }
        }
        LogUtil.info("动态阻挡数据Actor:{}", blockStatus);
        packet.setBlockMap(stageVo.getDynamicBlockMap());
        packet.addBlockStatusMap(blockStatus);

        PlayerUtil.send(roleId, packet);

        // 记住fightServerId
        // 抛事件
        ServiceHelper.roleService().notice(mainServerId, roleId, new FamilyWarFighterAddingSucceededEvent(fightServerId, SceneManager.SCENETYPE_FAMILY_WAR_ELITE_FIGHT));
        modifyConnectorRoute(mainServerId, roleId, fightServerId);
        knockout.onEliteFighterAddingSucceeded(mainServerId, fightServerId, battleId, fightId, roleId);
    }

    @Override
    public void onNormalFighterAddingSucceeded(int mainServerId, int fightServerId, String battleId, String fightId, byte camp, long roleId) {
        LogUtil.info("familywar|onNormalFighterAddingSucceeded|roleId:{},battleId:{},battleMap:{}", roleId, battleId, knockout.getBattleMap().keySet());
        if (battleId == null) {
            long familyId = knockout.getMemberMap().get(roleId).getFamilyId();
            battleId = knockout.getFamilyMap().get(familyId).getBattleId();
            LogUtil.info("familywar|onFighterAddingSucceeded|a exception ,fightServer return a error battleId , get a new one:{}", battleId);
        }
        if (knockout.getFighterReviveState(battleId, fightId, Long.toString(roleId)) == 1) return;

        StageinfoVo stageVo = SceneManager.getStageVo(stageIdOfNormalFight);
        ClientEnterPK enterPacket = new ClientEnterPK();
        enterPacket.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_NORMAL_FIGHT);
        enterPacket.setStageId(stageIdOfNormalFight);
        enterPacket.setLimitTime(getNormalFightRemainTime(battleId, fightId));
        enterPacket.setCountdownOfBegin(/*timeOfNoramlFightWaiting*/0);
        enterPacket.setSkillVoMap(FamilyWarUtil.getAllRoleSkillVoMap());
        enterPacket.addMonsterVoMap(stageVo.getMonsterVoMap());
        Summary summary = ServiceHelper.summaryService().getSummary(roleId);
        FighterEntity fighterEntity = FighterCreator.createBySummary((byte) 1, summary).get(Long.toString(roleId));
        fighterEntity.setCamp(camp);
        if (camp == FamilyWarConst.K_CAMP1) {
            fighterEntity.setPosition(stageVo.getPosition());
            fighterEntity.setRotation(stageVo.getRotation());
        } else {
            fighterEntity.setPosition(stageVo.getEnemyPos(0));
            fighterEntity.setRotation(stageVo.getEnemyRot(0));
        }
        List<FighterEntity> list = new ArrayList<>();
        list.add(fighterEntity);
        enterPacket.setFighterEntityList(list);

        // 记住fightServerId
        // 抛事件
        ServiceHelper.roleService().notice(mainServerId, roleId, new FamilyWarFighterAddingSucceededEvent(fightServerId, SceneManager.SCENETYPE_FAMILY_WAR_NORMAL_FIGHT));
        modifyConnectorRoute(mainServerId, roleId, fightServerId);
        knockout.onEliteFighterAddingSucceeded(mainServerId, fightServerId, battleId, fightId, roleId);
        PlayerUtil.send(roleId, enterPacket);
    }

    private int getNormalFightRemainTime(String battleId, String fightId) {
        FamilyWarKnockoutBattle battle = knockout.getBattleMap().get(battleId);
        return battle.getNormalFightRemainTime(fightId);
    }

    /**
     * 是否需要动态阻挡
     *
     * @return
     */
    public boolean needDynamicBlock(String battleId) {
        int remainderTime = getDynamicBlockRemainderTime(battleId);
        return remainderTime > 0 && remainderTime <= FamilyActWarManager.DYNAMIC_BLOCK_TIME;
    }

    /**
     * 离比赛开始的剩余时间（秒）
     *
     * @return
     */
    public int getDynamicBlockRemainderTime(String battleId) {
        int knockoutFlowStep = FamilyWarConst.STEP_OF_SUB_FLOW;
        if (knockoutFlowStep != FamilyWarKnockoutFlow.STEP_START_QUARTER_FINALS
                && knockoutFlowStep != FamilyWarKnockoutFlow.STEP_START_SEMI_FINALS
                && knockoutFlowStep != FamilyWarKnockoutFlow.STEP_START_FINALS) {
            return 0;
        }
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_LOCAL);
        if (StringUtil.isEmpty(flowMap) || !flowMap.containsKey(knockoutFlowStep)) {
            return 0;
        }
        FamilyWarKnockoutBattle battle = knockout.getBattleMap().get(battleId);
        long startTime = battle.getStartFightTimeStamp();
        long remainderTime = startTime + FamilyActWarManager.DYNAMIC_BLOCK_TIME * 1000 - System.currentTimeMillis();
        return (int) ((remainderTime < 0 ? 0 : remainderTime) / 1000);
    }

    /**
     * 离比赛结束的剩余时间
     *
     * @return
     */
    public int getBattleEndRemainderTime(String battleId) {
        int knockoutFlowStep = FamilyWarConst.STEP_OF_SUB_FLOW;
        if (knockoutFlowStep != FamilyWarKnockoutFlow.STEP_START_QUARTER_FINALS
                && knockoutFlowStep != FamilyWarKnockoutFlow.STEP_START_SEMI_FINALS
                && knockoutFlowStep != FamilyWarKnockoutFlow.STEP_START_FINALS) {
            return 0;
        }
        int tempStep = knockoutFlowStep + 1;
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_LOCAL);
        if (StringUtil.isEmpty(flowMap) || !flowMap.containsKey(tempStep)) {
            return 0;
        }
        FamilyWarKnockoutBattle battle = knockout.getBattleMap().get(battleId);
        long startTime = battle.getStartFightTimeStamp();
        long remainderTime = startTime + FamilyActWarManager.familywar_lasttime * 1000 - System.currentTimeMillis();
        return (int) (remainderTime / 1000);
    }

    /**
     * 获取怪物的战斗实体
     *
     * @param stageId
     * @return
     */
    public Map<String, FighterEntity> getMonsterFighterEntity(int stageId) {
        Map<String, FighterEntity> retMap = new HashMap<>();
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        for (int monsterSpawnId : stageVo.getMonsterSpawnIdList()) {
            retMap.putAll(spawnMonster(stageId, monsterSpawnId));
        }
        return retMap;
    }

    public Map<String, FighterEntity> spawnMonster(int stageId, int monsterSpawnId) {
        Map<String, FighterEntity> resultMap = new HashMap<>();
        MonsterSpawnVo monsterSpawnVo = SceneManager.getMonsterSpawnVo(monsterSpawnId);
        if (monsterSpawnVo == null) {
            LogUtil.error("familywar|找不到刷怪组配置monsterspawnid={},请检查表", monsterSpawnId, new IllegalArgumentException());
            return resultMap;
        }
        int index = 0;
        for (MonsterAttributeVo monsterAttrVo : monsterSpawnVo.getMonsterAttrList()) {
            String monsterUniqueId = getMonsterUId(stageId, monsterSpawnId, monsterAttrVo.getStageMonsterId());
            FighterEntity monsterEntity = FighterCreator.create(FighterEntity.TYPE_MONSTER, monsterUniqueId,
                    getSpawnUId(monsterSpawnId), monsterSpawnId, monsterAttrVo, monsterSpawnVo.getAwake(),
                    monsterSpawnVo.getSpawnDelayByIndex(index++), null);
            resultMap.put(monsterUniqueId, monsterEntity);
        }
        return resultMap;
    }

    protected String getSpawnUId(int spawnId) {
        return Integer.toString(spawnId);
    }

    protected String getMonsterUId(int stageId, int spawnId, int monsterId) {
        return "m" + stageId + getSpawnUId(spawnId) + monsterId;
    }

    @Override
    public void onClientPreloadFinished(int mainServerId, int fightServerId, String battleId, String fightId, long roleId) {
        if (battleId == null) {
            long familyId = knockout.getMemberMap().get(roleId).getFamilyId();
            battleId = knockout.getFamilyMap().get(familyId).getBattleId();
            LogUtil.info("familywar|onClientPreloadFinished|a exception ,fightServer return a error battleId , get a new one:{}", battleId);
        }
        knockout.onClientPreloadFinished(MultiServerHelper.getServerId(), battleId, fightId, roleId);
    }

    /**
     * 匹配
     */
    @Override
    public void match() {
        knockout.match();
    }

    @Override
    public void syncBattleFightUpdateInfo() {
        if (knockout != null) {
            knockout.syncBattleFightUpdateInfo();
        }
    }

    @Override
    public void modifyConnectorRoute(int mainServerId, long roleId, int fightServerId) {
        MultiServerHelper.modifyConnectorRoute(roleId, fightServerId);
    }

    /**
     * 请求复活
     */
    @Override
    public void revive(int mainServerId, int fightServerId, String battleId, String fightId, long roleId, byte reqType) {
        LogUtil.info(roleId + "|familywar|=========游戏服=======请求复活");
        if (battleId == null) {
            long familyId = knockout.getMemberMap().get(roleId).getFamilyId();
            battleId = knockout.getFamilyMap().get(familyId).getBattleId();
            LogUtil.info("familywar|revive|a exception ,fightServer return a error battleId , get a new one:{}", battleId);
        }
        knockout.revive(MultiServerHelper.getServerId(), battleId, fightId, roleId, reqType);
    }

    /**
     * 付费玩家复活
     */
    @Override
    public void handleRevive(String battleId, String fightId, String fighterUid) {
        if (battleId == null) {
            long familyId = knockout.getMemberMap().get(Long.parseLong(fighterUid)).getFamilyId();
            battleId = knockout.getFamilyMap().get(familyId).getBattleId();
            LogUtil.info("familywar|handleRevive|a exception ,fightServer return a error battleId , get a new one:{}", battleId);
        }
        knockout.handleRevive(battleId, fightId, fighterUid);
    }

    @Override
    public void onStageFightCreationSucceeded(int mainServerId, int fightServerId, String battleId, String fightId) {
        Map<String, FighterEntity> entityMap = knockout.getStageFighterEntities(battleId, fightId);
        if (StringUtil.isEmpty(entityMap)) return;
        List<FighterEntity> list = new ArrayList<>();
        list.addAll(entityMap.values());
        MainRpcHelper.fightBaseService().addFighter(fightServerId, FightConst.T_FAMILY_WAR_STAGE_FIGHT, mainServerId,
                fightId, list);
    }

    @Override
    public void onStageFighterAddingSucceeded(int mainServerId, int fightServerId, String battleId, String fightId,
                                              Set<Long> entitySet) {
        Map<String, FighterEntity> entityMap = knockout.getStageFighterEntities(battleId, fightId);
        if (StringUtil.isEmpty(entityMap)) return;
        FighterEntity entity;
        for (long roleId : entitySet) {
            entity = entityMap.get(Long.toString(roleId));
            if (entity == null) continue;
            knockout.onStageFightCreateSucceeded(mainServerId, fightServerId, battleId, fightId, roleId);
            // 抛事件
            ServiceHelper.roleService().notice(roleId, new FamilyWarFighterAddingSucceededEvent(fightServerId, SceneManager.SCENETYPE_FAMILY_WAR_STAGE_FIGTH));
            MultiServerHelper.modifyConnectorRoute(roleId, fightServerId);

            ClientEnterFamilyWarEliteFight packet = new ClientEnterFamilyWarEliteFight();
            packet.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_STAGE_FIGTH);
            packet.setStageId(FamilyActWarManager.stageIdOfStageFight);
            StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfStageFight);
            packet.setLimitTime(stageVo.getFailConMap().get(SceneManager.FAIL_CONDITION_TIME) / 1000);


            entity.setPosition(stageVo.getPosition());
            entity.setRotation(stageVo.getRotation());
            LogUtil.info("familywar|position:{},rotation:{}", stageVo.getPosition(), stageVo.getRotation());

            List<FighterEntity> list = new ArrayList<>();
            list.add(entity);
//	        list.addAll(getMonsterFighterEntity(9301).values());

            packet.setFighterEntityList(list); //
            packet.addMonsterVoMap(stageVo.getMonsterVoMap());

            PlayerUtil.send(roleId, packet);
        }
    }

    /**
     * 赛程时间触发，给所有符合条件的玩家发送图标入口
     */
    @Override
    public void sendMainIcon2All(long countdown) {
        LogUtil.info("familywar|赛程时间触发，给所有符合条件的玩家发送图标入口");
        int iconState = FamilyWarUtil.getMainIconStateByLocalFlowStep();
        if (iconState == ActivityFlow.STEP_START_CHECK) return;
        sendMainIcon(MultiServerHelper.getServerId(), iconState, countdown);
    }

    /**
     * 登陆触发，给玩家发送图标入口
     */
    @Override
    public void sendMainIcon2Role(long roleId, boolean isMaster, long familyId) {
        int iconState = FamilyWarUtil.getMainIconStateByLocalFlowStep();
        if (iconState == ActivityFlow.STEP_START_CHECK) return;
        long countdown = 0;
        if (iconState == FamilyWarConst.STATE_START) {
            Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_LOCAL);
            if (StringUtil.isEmpty(flowMap) || !flowMap.containsKey(FamilyWarKnockoutFlow.STEP_START_QUARTER_FINALS))
                return;
            countdown = ActivityFlowUtil.remainder(System.currentTimeMillis(), flowMap.get(FamilyWarKnockoutFlow.STEP_START_QUARTER_FINALS));
        }
        if (iconState != FamilyWarConst.STATE_NOTICE_MASTER) {
            sendMainIcon(MultiServerHelper.getServerId(), roleId, iconState, countdown);
        }
        if (isMaster && (iconState == FamilyWarConst.STATE_ICON_DISAPPEAR || iconState == FamilyWarConst.STATE_NOTICE_MASTER)) {
            knockout.sendMainIconToMaster(FamilyWarConst.W_TYPE_LOCAL, familyId);
        }
    }

    /**
     * 给所有玩家下发icon
     *
     * @param mainServer
     * @param state
     * @param countdown
     */
    public void sendMainIcon(int mainServer, int state, long countdown) {
        ClientFamilyWarMainIcon mainIcon = new ClientFamilyWarMainIcon(state, countdown);
        for (Actor actor : PlayerSystem.system().getActors().values()) {
            if (actor instanceof Player) {
                Player player = (Player) actor;
                long familyId = ServiceHelper.familyRoleService().getFamilyId(player.id());
                mainIcon.setQualification(knockout.isKnockoutFamily(familyId) ? FamilyWarConst.WITH_QUALIFICATION : FamilyWarConst.WITHOUT_QUALIFICATION);
                ServiceHelper.roleService().send(player.id(), mainIcon);
            }
        }
    }

    /**
     * 给单个玩家下发icon
     *
     * @param mainServer
     * @param roleId
     * @param state
     * @param countdown
     */
    public void sendMainIcon(int mainServer, long roleId, int state, long countdown) {
        ClientFamilyWarMainIcon mainIcon = new ClientFamilyWarMainIcon(state, countdown);
        long familyId = ServiceHelper.familyRoleService().getFamilyId(roleId);
        mainIcon.setQualification(knockout.isKnockoutFamily(familyId) ? FamilyWarConst.WITH_QUALIFICATION : FamilyWarConst.WITHOUT_QUALIFICATION);
        ServiceHelper.roleService().send(roleId, mainIcon);
    }

    private void generateStepList() {
        List<Integer> stepList = new ArrayList<>();
        stepList.add(FamilyWarKnockoutFlow.STEP_START_KNOCKOUT);
        stepList.add(FamilyWarKnockoutFlow.STEP_GENERATE_TEAM_SHEET);
        stepList.add(FamilyWarKnockoutFlow.STEP_BEFORE_QUARTER_FIANLS);
        stepList.add(FamilyWarKnockoutFlow.STEP_START_QUARTER_FINALS);
        stepList.add(FamilyWarKnockoutFlow.STEP_END_QUARTER_FINALS);
        stepList.add(FamilyWarKnockoutFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_SEMI);
        stepList.add(FamilyWarKnockoutFlow.STEP_BEFORE_SEMI_FIANLS);
        stepList.add(FamilyWarKnockoutFlow.STEP_START_SEMI_FINALS);
        stepList.add(FamilyWarKnockoutFlow.STEP_END_SEMI_FINALS);
        stepList.add(FamilyWarKnockoutFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_FINALS);
        stepList.add(FamilyWarKnockoutFlow.STEP_BEFORE_END_FIANLS);
        stepList.add(FamilyWarKnockoutFlow.STEP_START_FINALS);
        stepList.add(FamilyWarKnockoutFlow.STEP_END_FINALS);
        stepList.add(FamilyWarKnockoutFlow.STEP_END_KNOCKOUT);
        FamilyWarUtil.localStepList = stepList;
        List<Integer> qualifyStepList = new ArrayList<>();
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_START_QUALIFYING);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_GENERATE_TEAM_SHEET);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_BEFORE_1ST);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_START_1ST);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_END_1ST);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_2ND);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_BEFORE_2ND);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_START_2ND);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_END_2ND);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_3RD);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_BEFORE_3RD);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_START_3RD);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_END_3RD);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_4TH);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_BEFORE_4TH);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_START_4TH);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_END_4TH);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_5TH);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_BEFORE_5TH);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_START_5TH);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_END_5TH);
        qualifyStepList.add(FamilyWarQualifyingFlow.STEP_END_QUALIFYING);
        FamilyWarUtil.qualifyingStepList = qualifyStepList;
        List<Integer> remoteStepList = new ArrayList<>();
        remoteStepList.add(FamilyWarRemoteFlow.STEP_START_REMOTE);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_GENERATE_TEAM_SHEET);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_BEFORE_32TO16);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_START_32TO16);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_END_32TO16);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_16TO8);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_BEFORE_16TO8);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_START_16TO8);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_END_16TO8);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_8TO4);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_BEFORE_8TO4);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_START_8TO4);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_END_8TO4);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_4TO2);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_BEFORE_4TO2);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_START_4TO2);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_END_4TO2);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_FINNAL);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_BEFORE_FINNAL);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_START_FINNAL);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_END_FINNAL);
        remoteStepList.add(FamilyWarRemoteFlow.STEP_END_REMOTE);
        FamilyWarUtil.remoteStepList = remoteStepList;
    }

    /**
     * 请求点赞
     */
    @Override
    public void reqSupport(long roleId, long familyId) {
        knockout.reqSupport(MultiServerHelper.getServerId(), roleId, familyId);
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "家族战请求点赞", true));
        }
    }

    /**
     * 增加家族被点赞次数
     */
    @Override
    public void addSupport(long roleId, long familyId) {
        knockout.addSupport(MultiServerHelper.getServerId(), roleId, familyId);
    }

    @Override
    public void addMember(long familyId, FamilyMemberPo memberPo, FighterEntity fighterEntity) {
        if (knockout == null) return;
        knockout.addMember(familyId, memberPo, fighterEntity);
    }

    @Override
    public void delMember(long familyId, long roleId) {
        if (knockout == null) return;
        knockout.delMember(familyId, roleId);
    }
}
