package com.stars.modules.family;

import com.stars.core.SystemRecordMap;
import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.db.DBUtil;
import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.family.event.FamilyAuthAchieveEvent;
import com.stars.modules.family.event.FamilyAuthUpdatedEvent;
import com.stars.modules.family.event.FamilyLockUpdatedEvent;
import com.stars.modules.family.event.FamilyLogEvent;
import com.stars.modules.family.packet.*;
import com.stars.modules.family.prodata.FamilySkillVo;
import com.stars.modules.family.submodules.entry.FamilyActEntryFilter;
import com.stars.modules.family.summary.FamilySummaryComponentImpl;
import com.stars.modules.family.userdata.RoleFamilyPo;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.services.ServiceHelper;
import com.stars.services.activities.ActConst;
import com.stars.services.family.FamilyAuth;
import com.stars.services.family.FamilyConst;
import com.stars.services.family.FamilyPost;
import com.stars.services.family.event.FamilyEvent;
import com.stars.services.family.main.FamilyMainServiceActor;
import com.stars.services.family.main.memdata.RecommendationFamily;
import com.stars.services.family.main.prodata.FamilyLevelVo;
import com.stars.util.DirtyWords;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by zhaowenshuo on 2016/8/24.
 */
public class FamilyModule extends AbstractModule {

    private FamilyAuth auth;
    private byte isFamilyLock;
    private long lastAppliedAllTimestamp;
    private RoleFamilyPo roleFamilyPo;
    private int redPacketCount;
    private Set<Long> applyList;
    private Set<String> attributes;
    /* 家族心法相关 */
    private Map<String, Integer> revisedSkillLevelMap; // 家族心法修正等级表
    private boolean isFreeQuota;

    public FamilyModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("家族", id, self, eventDispatcher, moduleMap);
    }

    public FamilyAuth getAuth() {
        RoleModule roleModule = module(MConst.Role);
        if (auth != null) {
//            LogUtil.info("auth不为空，直接返回auth, roleId={}, familyId={}, roleName={}", id(), auth.getFamilyId(), roleModule.getRoleRow().getName());
            return auth;
        } else {
//            LogUtil.info("auth为空，同步获取auth, roleId={}", id());
            try {
                auth = ServiceHelper.familyRoleService().getFamilyAuth(id());
                auth.setRoleName(roleModule.getRoleRow().getName());
            } catch (Throwable t) {
                auth = new FamilyAuth(0, "", 0, id(), null, FamilyPost.MASSES);
            }
            return auth;
        }
    }

    public RoleFamilyPo getRoleFamilyPo() {
        return roleFamilyPo;
    }

    public void setFreeQuota(boolean isFreeQuota) {
        this.isFreeQuota = isFreeQuota;
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        roleFamilyPo = new RoleFamilyPo(id(), FamilyManager.donateLimit, FamilyManager.rmbDonateLimit);
        context().insert(roleFamilyPo);
    }

    public void setRedPacketCount(int redPacketCount) {
        this.redPacketCount = redPacketCount;
        signCalRedPoint(MConst.Family, RedPointConst.FAMILY_RED_PACKET);
    }

    @Override
    public void onDataReq() throws Throwable {
        roleFamilyPo = DBUtil.queryBean(DBUtil.DB_USER, RoleFamilyPo.class,
                "select * from `rolefamily` where `roleid`=" + id());
        if (roleFamilyPo == null) {
            roleFamilyPo = new RoleFamilyPo(id(), FamilyManager.donateLimit, FamilyManager.rmbDonateLimit);
            context().insert(roleFamilyPo);
        }
        applyList = new HashSet<>();
        attributes = new HashSet<>();
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        ServiceHelper.familyRoleService().online(id());
        getAuth();
        getContribution();
        doFamilyTreasure(auth.getFamilyId());
        RoleModule roleModule = module(MConst.Role);
        int newJobId = roleModule.getRoleRow().getJobId();
        /**
         * 避免出现转职后，角色与家族成员角色职业不一致，登陆进行一次校准
         */
        if (auth.hasFamily()) {
            ServiceHelper.familyMainService().updateMemberJob(
                    auth.getFamilyId(), auth.getRoleId(), newJobId);
        }
    }

    @Override
    public void onSyncData() throws Throwable {
        if(auth.hasFamily()) {
            eventDispatcher().fire(new FamilyAuthAchieveEvent(
                    FamilyAuthAchieveEvent.TYPE_NEW, id(), auth.getFamilyId(), auth.getFamilyName(), auth.getFamilyLevel(), auth.getPost(), 0));
        }
    }

    @Override
    public void onOffline() throws Throwable {
        auth = null;
        ServiceHelper.familyRoleService().offline(id());
        com.stars.util.LogUtil.info("家族模块|离线|roleId:{}", id());
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        roleFamilyPo.setDonateResidue(FamilyManager.donateLimit);
        roleFamilyPo.setRmbDonateResidue(FamilyManager.rmbDonateLimit);
        context().update(roleFamilyPo);
    }

    private void doFamilyTreasure(long familyId) {
        if (familyId == 0) {
            com.stars.util.LogUtil.info("离线状态下被踢出家族，清理家族探宝伤害");
        }
    }

    public void addApplyList(Set<Long> applys) {
        if (applyList == null) {
            applyList = new HashSet<>();
        }
        this.applyList.addAll(applys);
        signCalRedPoint(MConst.Family, RedPointConst.FAMILY_APPLY);
    }

    public void addApplyList(long applyId) {
        if (applyList == null) {
            applyList = new HashSet<>();
        }
        this.applyList.add(applyId);
        signCalRedPoint(MConst.Family, RedPointConst.FAMILY_APPLY);
    }

    public void removeApplyList(long applyId) {
        if (applyList == null) {
            applyList = new HashSet<>();
        }
        if (!applyList.contains(applyId)) {
            return;
        }
        this.applyList.remove(applyId);
        signCalRedPoint(MConst.Family, RedPointConst.FAMILY_APPLY);
    }

    public void removeAllApply() {
        if (applyList == null) {
            applyList = new HashSet<>();
        }
        applyList.clear();
        signCalRedPoint(MConst.Family, RedPointConst.FAMILY_APPLY);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.FAMILY_APPLY))) {
            applyRedPoint(redPointMap);
        }
        if (redPointIds.contains(Integer.valueOf(RedPointConst.FAMILY_MIND))) {
            mindRedPoint(redPointMap);
        }
        if (redPointIds.contains(Integer.valueOf(RedPointConst.FAMILY_RED_PACKET))) {
            redPacketPoint(redPointMap);
        }
    }

    private void redPacketPoint(Map<Integer, String> redPointMap) {
        if (redPacketCount > 0) {
            redPointMap.put(RedPointConst.FAMILY_RED_PACKET, redPacketCount + "");
        } else if (redPacketCount == 0) {
            redPointMap.put(RedPointConst.FAMILY_RED_PACKET, null);
        } else {
            return;
        }
    }

    private void mindRedPoint(Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        if (attributes != null) {
            Iterator<String> iterator = attributes.iterator();
            while (iterator.hasNext()) {
                builder.append(iterator.next()).append("+");
            }
            redPointMap.put(RedPointConst.FAMILY_MIND, builder.toString().isEmpty() ? null : builder.toString());
        } else {
            redPointMap.put(RedPointConst.FAMILY_MIND, null);
        }
    }

    private void applyRedPoint(Map<Integer, String> redPointMap) {
        checkRedPoint(redPointMap, applyList, RedPointConst.FAMILY_APPLY);
    }


    private void checkRedPoint(Map<Integer, String> redPointMap, Set<Long> list, int redPointConst) {
        StringBuilder builder = new StringBuilder("");
        if (!list.isEmpty()) {
            Iterator<Long> iterator = list.iterator();
            while (iterator.hasNext()) {
                builder.append(iterator.next()).append("+");
            }
            redPointMap.put(redPointConst, builder.toString().isEmpty() ? null : builder.toString());
        } else {
            redPointMap.put(redPointConst, null);
        }
    }

    public void handleEvent(Event event) {
        if (event instanceof FamilyAuthUpdatedEvent) {
            handleFamilyAuthUpdatedEvent((FamilyAuthUpdatedEvent) event);

        } else if (event instanceof RoleLevelUpEvent) {
            handleRoleLevelUpEvent((RoleLevelUpEvent) event);

        } else if (event instanceof FightScoreChangeEvent) {
            handleFightScoreChangeEvent((FightScoreChangeEvent) event);

        } else if (event instanceof FamilyLockUpdatedEvent) {
            handleFamilyLockUpdatedEvent((FamilyLockUpdatedEvent) event);
        } else if (event instanceof FamilyLogEvent) {
            handleFamilyLogEvent((FamilyLogEvent) event);
        } else if (event instanceof RoleRenameEvent) {
            onRoleRename((RoleRenameEvent) event);
        }
    }

    /**
     * 角色改名触发
     *
     * @param roleRenameEvent
     */
    private void onRoleRename(RoleRenameEvent roleRenameEvent) {
    }



    private void handleFamilyLogEvent(FamilyLogEvent event) {
        ServerLogModule logger = module(MConst.ServerLog);
        byte opType = event.getOpType();
        if (opType == FamilyLogEvent.RED_SEND) {
            logger.log_personal_family_red_send(event.getType(), event.getItemType(), event.getMoney(), event.getRoleId());
        } else if (opType == FamilyLogEvent.EXCHANGE) {
            logger.log_personal_family_exchange(event.getType(), event.getItemId(), event.getNum(), event.getRoleId());
        } else if (opType == FamilyLogEvent.FAMILY_QUIT) {
            logger.log_personal_family_quit(event.getFamilyId(), event.getRoleId());
        }
    }

    private void handleFamilyAuthUpdatedEvent(FamilyAuthUpdatedEvent event) {
        if (event.getType() != FamilyAuthUpdatedEvent.TYPE_CREATED
                && event.getType() != FamilyAuthUpdatedEvent.TYPE_DISSOLVE) {

            RoleModule roleModule = module(MConst.Role);
            auth = new FamilyAuth(event.getFamilyId(), event.getFamilyName(), event.getFamilyLevel(), event.getRoleId(), roleModule.getRoleRow().getName(), event.getPost());
            send(new ClientFamilyAuth(auth));
            if (event.getType() == FamilyAuthUpdatedEvent.TYPE_NEW) {
                ServiceHelper.familyMainService().online(auth.getFamilyId(), id(), false);
            }
            // 授权改变时，强制同步一次数据到家族
            if (auth.getFamilyId() > 0) {
                Role rolePo = roleModule.getRoleRow();
                ServiceHelper.familyMainService().updateMemberLevel(
                        auth.getFamilyId(), auth.getRoleId(), rolePo.getLevel());
                ServiceHelper.familyMainService().updateMemberFightScore(
                        auth.getFamilyId(), auth.getRoleId(), rolePo.getFightScore());
            }
        }
        recalcFamilySkillFightScore(event.getType() != FamilyAuthUpdatedEvent.TYPE_LOGIN); // 重新计算属性和战力
        SceneModule sceneModule = module(MConst.Scene);
        RoleModule roleModule = module(MConst.Role);
        if (sceneModule.getScene() != null && sceneModule.getScene().getSceneType() == SceneManager.SCENETYPE_FAMIL) {
            /**在家族场景中*/
            if (event.getFamilyId() == 0) {
                /** 没有家族了，踢出场景 */
                roleModule.initSafeStage();
                sceneModule.enterScene(SceneManager.SCENETYPE_CITY, roleModule.getSafeStageId(), "");
            }
        }
        updateFamilySummary();/* 更新玩家的摘要数据 */
    }

    private void handleRoleLevelUpEvent(RoleLevelUpEvent event) {
        if (auth != null && auth.getFamilyId() > 0) {
            ServiceHelper.familyMainService().updateMemberLevel(
                    auth.getFamilyId(), auth.getRoleId(), event.getNewLevel());
        }
    }

    private void handleFightScoreChangeEvent(FightScoreChangeEvent event) {
        if (auth != null && auth.getFamilyId() > 0) {
            ServiceHelper.familyMainService().updateMemberFightScore(
                    auth.getFamilyId(), auth.getRoleId(), event.getNewFightScore());
        }
    }

    private void handleFamilyLockUpdatedEvent(FamilyLockUpdatedEvent event) {
        if (auth != null && (event.isLockAll() || auth.getFamilyId() == event.getFamilyId())) {
            isFamilyLock = event.getIsLock();
            ClientFamilyManagement packet = new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_LOCK);
            packet.setFamilyId(event.getFamilyId());
            packet.setLock(event.getIsLock());
            send(packet);
        }
    }

    public void sendFamilyInfo() {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyMainService().sendFamilyInfo(auth); // 发送家族信息
        // 耦合, 耦合, 耦合（sb策划
//        FamilyActExpeditionModule expeModule = module(MConst.FamilyActExpe);
//        expeModule.sendView(ServerFamilyActExpedition.SUBTYPE_VIEW);
        fireSpecialAccountLogEvent("发送家族信息");
    }

    public void sendMemberList() {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyMainService().sendMemberList(auth); // 发送家族列表
    }

    public void sendApplicationList() {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyMainService().sendApplicationList(auth);
    }

    public void createFamily(String name, String notice) {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        if (!checkFamilyName(name)) { // 检查家族名
            return;
        }
        if (!checkFamilyNotice(notice)) { // 检查家族公告
            return;
        }
        ToolModule toolModule = module(MConst.Tool);
        RoleModule roleModule = module(MConst.Role);
        if (!isFreeQuota && !toolModule.contains(ToolManager.GOLD, FamilyManager.creationCost)) { // 先判断够不够钱
            ItemVo itemVo = ToolManager.getItemVo(ToolManager.GOLD);
            warn("family_tips_noreqitem", itemVo.getName(), Integer.toString(FamilyManager.creationCost));
            return;
        }
        Role rolePo = roleModule.getRoleRow();
        FamilyAuth tmpAuth = ServiceHelper.familyMainService().create(id(), auth.getPost(), name, notice,
                rolePo.getJobId(), rolePo.getName(), rolePo.getLevel(), rolePo.getFightScore());
        if (tmpAuth != null) {
            if (!isFreeQuota) {
                if (!toolModule.deleteAndSend(ToolManager.GOLD, FamilyManager.creationCost, EventType.CREATFAMILY.getCode())) {
                    com.stars.util.LogUtil.error("扣减创建家族的费用失败"); // 应该不可能出现
                }
            } else {
                isFreeQuota = false;
            }
            auth = tmpAuth;
            ServiceHelper.familyRedPacketService().addFamily(auth.getFamilyId());
            ServiceHelper.familyRedPacketService().addMember(auth.getFamilyId(), id());
            send(new ClientFamilyAuth(auth));
            send(new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_CREATE, true, null));
            eventDispatcher().fire(new FamilyAuthUpdatedEvent(
                    FamilyAuthUpdatedEvent.TYPE_CREATED, id(), auth.getFamilyId(), auth.getFamilyName(), auth.getFamilyLevel(), auth.getPost(), 0));
            eventDispatcher().fire(new FamilyAuthAchieveEvent(
                    FamilyAuthAchieveEvent.TYPE_CREATED, id(), auth.getFamilyId(), auth.getFamilyName(), auth.getFamilyLevel(), auth.getPost(), 0));
        } else {
            send(new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_CREATE, false, null));
        }
        fireSpecialAccountLogEvent("创建家族");
    }

    private boolean checkFamilyName(String name) {
//        if (StringUtil.hasSensitiveWordExt1(name)) {
        if (DirtyWords.checkName(name)) {
            warn("family_tips_nocreate");
            return false;
        }
        if (name == null || name.length() == 0) {
            warn(I18n.get("family.management.reqNotEmptyName"));
            return false;
        }
        if (name.length() > FamilyManager.nameLenLimit) {
            warn("family_tips_longname");
            return false;
        }
        if (!StringUtil.isValidString(name)) {
            warn("family_tips_nocreate");
            return false;
        }
//        for (int i = 0; i < name.length(); i++) {
//            char c = name.charAt(i);
//            if (!StringUtil.isChineseWithoutPunctuation(c) && !Character.isLetterOrDigit(c)) {
//                warn("family_tips_nocreate");
//                return false;
//            }
//        }
        return true;
    }

    private boolean checkFamilyNotice(String notice) {
//        if (StringUtil.hasSensitiveWordExt1(notice)) {
        if (DirtyWords.checkNotice(notice)) {
            warn("family_tips_nocreate");
            return false;
        }
        if (notice.length() > FamilyManager.noticeLenLimit) {
            warn("family_tips_longinput");
            return false;
        }
        for (int i = 0; i < notice.length(); i++) {
            char c = notice.charAt(i);
            if (!StringUtil.isChinese(c) && !(c >= 0 && c <= 256)) {
                warn("family_tips_nocreate");
                return false;
            }
        }
        return true;
    }

    public void dissolve() {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        FamilyAuth tmpAuth = ServiceHelper.familyMainService().dissolve(auth);
        if (tmpAuth != null) {
            long prevFamilyId = auth.getFamilyId();
            auth = tmpAuth;
            send(new ClientFamilyAuth(auth));
            send(new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_DISSOLVE, true, null));
            eventDispatcher().fire(new FamilyAuthUpdatedEvent(
                    FamilyAuthUpdatedEvent.TYPE_DISSOLVE, id(), 0, "", 0, FamilyPost.MASSES, prevFamilyId));
        } else {
            send(new ClientFamilyManagement(ClientFamilyManagement.SUBTYPE_DISSOLVE, false, null));
        }
    }

    public void editNotice(String notice) {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        if (!checkFamilyNotice(notice)) { // 检查家族公告
            return;
        }
        ServiceHelper.familyMainService().editNotice(auth, notice);
    }

    public void search(String pattern) {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyRoleService().searchFamily(id(), pattern);
    }

    public void apply(long familyId) {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        RoleModule roleModule = module(MConst.Role);
        Role rolePo = roleModule.getRoleRow();
        ServiceHelper.familyMainService().apply(familyId, true, id(), auth.getPost(),
                rolePo.getJobId(), rolePo.getName(), rolePo.getLevel(), rolePo.getFightScore());
    }

    public void applyAll() {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        if (System.currentTimeMillis() - lastAppliedAllTimestamp < 1000 * 60) {
            warn(I18n.get("family.management.reqTooOften"));
            return;
        }
        int count = 0;
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        Role rolePo = roleModule.getRoleRow();
        List<RecommendationFamily> list = FamilyMainServiceActor.recommList;
        Set<Long> appliedSet = ServiceHelper.familyRoleService().getAppliedFamilyIdSet(id());
        if (list != null) {
            for (RecommendationFamily recomm : list) {
                if (!appliedSet.contains(recomm.getFamilyId())) {
                    ServiceHelper.familyMainService().apply(recomm.getFamilyId(), false, id(), auth.getPost(),
                            rolePo.getJobId(), rolePo.getName(), rolePo.getLevel(), rolePo.getFightScore());
                    count++;
                    if (count >= FamilyManager.applyAllLimitPerTime) {
                        break;
                    }
                }
            }
        }
        warn(I18n.get("family.management.apply.alreadySent"));
    }

    public void verify(long applicantId, boolean isApproved) {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyMainService().verify(auth, applicantId, isApproved);
    }

    public void verify(List<Long> applicantIdList, boolean isApproved) {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        if (applicantIdList == null || applicantIdList.size() == 0) {
            warn("family_tips_noapplylist");
        } else {
            for (Long applicantId : applicantIdList) {
                ServiceHelper.familyMainService().verify(auth, applicantId, isApproved);
            }
        }
    }

    public void cancel(long familyId) {
        // todo:
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyMainService().cancel(familyId, id());
    }

    public void invite(long inviteeId) {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyMainService().invite(auth, inviteeId);
    }

    public void acceptInvitation(long familyId) {
        // fixme: 需要邀请吗
        ServiceHelper.familyMainService().acceptInvitation(familyId, id());
    }

    public void refuseInvitation(long familyId) {
        ServiceHelper.familyMainService().refuseInvitation(familyId, id());
    }

    public void poach(long inviteeId) {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyMainService().poach(auth, inviteeId);
    }

    public void acceptPoaching(long familyId, long inviteeId) {
        ServiceHelper.familyMainService().acceptPoaching(familyId, inviteeId);
    }

    public void refusePoaching(long familyId, long inviteeId) {
        ServiceHelper.familyMainService().refusePoaching(familyId, inviteeId);
    }

    public void kickOut(long memberId) {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyMainService().kickOut(auth, memberId);
    }

    public void leave() {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyMainService().leave(auth);
    }

    public void abdicate(long memberId) {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyMainService().abdicate(auth, memberId);
    }

    public void appoint(long memberId, byte postId) {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyMainService().appoint(auth, memberId, postId);
    }

    public void setAppAllowance(boolean isAllowed) {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyMainService().setApplicationAllowance(auth, isAllowed);
    }

    public void setAppQualification(int minLevel, int minFightScore, boolean isAutoVerified) {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyMainService().setApplicationQualification(auth, minLevel, minFightScore, isAutoVerified);
    }

    public void sendDonateInfo() {
        ClientFamilyDonate packet = new ClientFamilyDonate(ClientFamilyDonate.SUBTYPE_INFO);
        packet.setDonateResidue(roleFamilyPo.getDonateResidue());
        packet.setDonateRmbResidue(roleFamilyPo.getRmbDonateResidue());
        send(packet);
        fireSpecialAccountLogEvent("请求捐献数据");
    }

    public void donate() {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        if (auth.getFamilyId() <= 0) {
            warn(I18n.get("family.management.noSuchFamily"));
            return;
        }
        byte residue = roleFamilyPo.getDonateResidue();
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        if (residue > 0 && toolModule.deleteAndSend(ToolManager.MONEY, FamilyManager.donateReqValue, EventType.FAMILYDONATE.getCode())) {
            roleFamilyPo.setDonateResidue((byte) (residue - 1));
            context().update(roleFamilyPo);
            ServiceHelper.familyRoleService().addAndSendContribution(id(), FamilyManager.donateGainedContribution);
            ServiceHelper.familyMainService().addMoneyAndUpdateContribution(
                    auth, id(), FamilyManager.donateGainedFamilyMoney, FamilyManager.donateGainedContribution, SystemRecordMap.dateVersion, 0);
            sendDonateInfo();
            warn("common_tips_getaward", ToolManager.getItemName(ToolManager.FAMILY_MONEY), toString(FamilyManager.donateGainedFamilyMoney));
            warn("common_tips_getaward", ToolManager.getItemName(ToolManager.FAMILY_CONTRIBUTION), toString(FamilyManager.donateGainedContribution));
            //捐献日志
            ServerLogModule logger = module(MConst.ServerLog);
            logger.log_personal_family_donate((byte) 0, FamilyManager.donateReqValue);
        } else {
            warn("bag_buyitem_nomoney", ToolManager.getItemName(ToolManager.MONEY));
        }
        fireSpecialAccountLogEvent("家族金币捐献");
    }

    public void donateRmb() {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        if (auth.getFamilyId() <= 0) {
            warn(I18n.get("family.management.noSuchFamily"));
            return;
        }
        byte residue = roleFamilyPo.getRmbDonateResidue();
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        if (residue > 0 && toolModule.deleteAndSend(ToolManager.GOLD, FamilyManager.rmbDonateReqValue, EventType.FAMILYDONATE.getCode())) {
            roleFamilyPo.setRmbDonateResidue((byte) (residue - 1));
            context().update(roleFamilyPo);
            ServiceHelper.familyRoleService().addAndSendContribution(id(), FamilyManager.rmbDonateGainedContribution);
            ServiceHelper.familyMainService().addMoneyAndUpdateContribution(
                    auth, id(), FamilyManager.rmbDonateGainedFamilyMoney, FamilyManager.rmbDonateGainedContribution, SystemRecordMap.dateVersion, FamilyManager.rmbDonateReqValue);
            ServiceHelper.familyEventService().logEvent(auth.getFamilyId(), FamilyEvent.W_RMB_DONATE, auth.getRoleName());
            sendDonateInfo();
            warn("common_tips_getaward", ToolManager.getItemName(ToolManager.FAMILY_MONEY), toString(FamilyManager.rmbDonateGainedFamilyMoney));
            warn("common_tips_getaward", ToolManager.getItemName(ToolManager.FAMILY_CONTRIBUTION), toString(FamilyManager.rmbDonateGainedContribution));
            ServiceHelper.familyEventService().sendEvent(auth, ServerFamilyEvent.SUBTYPE_DONATE); // 刷新一次捐献记录
            //捐献日志
            ServerLogModule logger = module(MConst.ServerLog);
            logger.log_personal_family_donate((byte) 1, FamilyManager.rmbDonateReqValue);
        } else {
            warn("bag_buyitem_nomoney", ToolManager.getItemName(ToolManager.GOLD));
        }
        fireSpecialAccountLogEvent("家族元宝捐献");
    }

    public void sendUpgradeInfo() {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyMainService().sendUpgradeInfo(auth);
    }

    public void upgrade() {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        ServiceHelper.familyMainService().upgrade(auth);
    }

    /* 心法 */
    public void upgradeSkillLevel(String attribute, int nextLevel) {
        if (auth == null) {
            warn("common_tips_loading");
            return;
        }
        FamilyLevelVo familyLevelVo = FamilyManager.levelVoMap.get(auth.getFamilyLevel());
        if (familyLevelVo == null) {
            warn(I18n.get("family.management.lackProductData"));
            return;
        }
        if (nextLevel > familyLevelVo.getSkillLimit()) {
            warn("family_tips_nofamilylevel");
            return;
        }
        Map<String, Integer> skillLevelMap = roleFamilyPo.getSkillLevelMap();
        if ((skillLevelMap.get(attribute) == null && nextLevel != 1)
                || (skillLevelMap.get(attribute) != null && skillLevelMap.get(attribute) + 1 != nextLevel)) {
            warn(I18n.get("family.management.skill.incorrectData"));
            return;
        }
        FamilySkillVo nextLevelSkillVo = FamilyManager.skillVoMap.get(attribute).get(nextLevel);
        if (nextLevelSkillVo == null) {
            warn(I18n.get("family.management.skill.alreadyMaxLevel"));
            return;
        }
        RoleModule roleModule = module(MConst.Role);
        if (roleModule.getLevel() < nextLevelSkillVo.getReqRoleLevel()) {
            warn("family_tips_nolevel", Integer.toString(nextLevelSkillVo.getReqRoleLevel()));
            return;
        }
        boolean flag = ServiceHelper.familyRoleService().addAndSendContribution(id(), -nextLevelSkillVo.getReqContribution());
        if (!flag) {
            warn("family_tips_reqfamilydonate");
            return;
        }
        ServerLogModule log = (ServerLogModule) module(MConst.ServerLog);
        log.Log_core_item(51, nextLevelSkillVo.getReqContribution(), EventType.FAMILYBUFF.getCode(), (byte) 0);
        skillLevelMap.put(attribute, nextLevel);
        context().update(roleFamilyPo);
        recalcFamilySkillFightScore(Boolean.TRUE);
        updateFamilySummary();/* 更新玩家的摘要数据 */
        // 下发数据
        ClientFamilySkill packet = new ClientFamilySkill(ClientFamilySkill.SUBTYPE_UPGRADE);
        packet.setSingleSkillLevelMap(attribute, nextLevel);
        send(packet);
        fireSpecialAccountLogEvent("家族心法升级");
        //心法日志
        StringBuffer logStr = new StringBuffer();
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (String attr : skillLevelMap.keySet()) {
            map.put(getAttributeIndex(attr), skillLevelMap.get(attr));
        }
        Integer state = null;
        for (int i = 1; i <= 7; i++) {
            state = map.get(i);
            if (state == null) {
                state = 0;
            }
            if (i == 1) {
                logStr.append(i).append("@").append(state);
            } else {
                logStr.append("&").append(i).append("@").append(state);
            }
        }
        ServerLogModule logger = module(MConst.ServerLog);
        logger.log_personal_family_spell(logStr.toString());
    }

    private int getAttributeIndex(String attribute) {
        if (attribute.equals("attack")) {
            return 1;
        } else if (attribute.equals("hp")) {
            return 2;
        } else if (attribute.equals("defense")) {
            return 3;
        } else if (attribute.equals("hit")) {
            return 4;
        } else if (attribute.equals("avoid")) {
            return 5;
        } else if (attribute.equals("crit")) {
            return 6;
        } else if (attribute.equals("anticrit")) {
            return 7;
        }
        return 0;
    }


    public void getContribution() {
        int contribution = ServiceHelper.familyRoleService().getContribution(id());
        RoleModule roleModule = module(MConst.Role);
        if (auth == null) {
            return;
        }
        FamilyLevelVo familyLevelVo = FamilyManager.levelVoMap.get(auth.getFamilyLevel());
        calAttributes(contribution, roleModule, familyLevelVo);
        signCalRedPoint(MConst.Family, RedPointConst.FAMILY_MIND);
    }

    private void calAttributes(int contribution, RoleModule roleModule, FamilyLevelVo familyLevelVo) {
        FamilySkillVo nextLevelSkillVo;
        Map<String, Integer> skillLevelMap = roleFamilyPo.getSkillLevelMap();
        if (familyLevelVo != null) {
            for (String attribute : FamilyManager.skillVoMap.keySet()) {
                if (skillLevelMap.get(attribute) != null) {
                    nextLevelSkillVo = FamilyManager.skillVoMap.get(attribute).get(skillLevelMap.get(attribute) + 1);
                    calAttributes(contribution, nextLevelSkillVo, roleModule, attribute, familyLevelVo, skillLevelMap.get(attribute) + 1);
                }
                //可以激活的心法
                else {
                    nextLevelSkillVo = FamilyManager.skillVoMap.get(attribute).get(1);
                    calAttributes(contribution, nextLevelSkillVo, roleModule, attribute, familyLevelVo, 1);
                }
            }
        }
    }

    private void calAttributes(int contribution, FamilySkillVo nextLevelSkillVo, RoleModule roleModule, String attribute, FamilyLevelVo familyLevelVo, int nextLevel) {
        if (attributes == null) {
            attributes = new HashSet<>();
        }
        if (nextLevelSkillVo != null) {
            if (roleModule.getLevel() >= nextLevelSkillVo.getReqRoleLevel()
                    && contribution >= nextLevelSkillVo.getReqContribution()
                    && nextLevel <= familyLevelVo.getSkillLimit()) {
                //满足需求，下发红点
                attributes.add(attribute);
            } else {
                //不满足需求，下发空红点
                if (attributes.contains(attribute)) {
                    attributes.remove(attribute);
                }
            }
        } else {
            //不满足需求，下发空红点
            if (attributes.contains(attribute)) {
                attributes.remove(attribute);
            }
        }
    }


    // amap --> as many as possible
    public void upgradeSkillLevelAmap() {
        int ownedContribution = ServiceHelper.familyRoleService().getContribution(id());
        int usedContribution = 0;
        Map<String, Integer> upgradedSkillLevelMap = new HashMap<>();
        boolean isUpgrade = false;
        for (int i = 0; i < FamilyManager.upgradeSkillLevelAmapMaxLoop; i++) { // 防止死循环
            FamilySkillVo familySkillVo = findNextAvailableAndMinRequirementSkillVo(ownedContribution - usedContribution, upgradedSkillLevelMap);
            if (familySkillVo == null) {
                if (upgradedSkillLevelMap.size() == 0) {
                    sendTipsOfFailureToUpgradeSkillLevelAmap(ownedContribution);
                    //心法日志
                    log_skill(isUpgrade);
                    return;
                }
                boolean flag = ServiceHelper.familyRoleService().addAndSendContribution(id(), -usedContribution);
                ServerLogModule log = (ServerLogModule) module(MConst.ServerLog);
                log.Log_core_item(51, usedContribution, EventType.FAMILYBUFF.getCode(), (byte) 0);
                if (flag) {
                    isUpgrade = true;
                    Map<String, Integer> skillLevelMap = roleFamilyPo.getSkillLevelMap();
                    for (Map.Entry<String, Integer> entry : upgradedSkillLevelMap.entrySet()) {
                        skillLevelMap.put(entry.getKey(), entry.getValue());
                    }
                    context().update(roleFamilyPo);
                    recalcFamilySkillFightScore(Boolean.TRUE);
                    updateFamilySummary();/* 更新玩家的摘要数据 */
                    // 下发数据
                    ClientFamilySkill packet = new ClientFamilySkill(ClientFamilySkill.SUBTYPE_UPGRADE);
                    packet.setPartSkillLevelMap(upgradedSkillLevelMap);
                    send(packet);
                }
                //心法日志
                log_skill(isUpgrade);
                return;
            } else {
                isUpgrade = true;
                upgradedSkillLevelMap.put(familySkillVo.getAttribute(), familySkillVo.getLevel());
                usedContribution += familySkillVo.getReqContribution();
            }
        }
        fireSpecialAccountLogEvent("家族amap");
        //心法日志
        log_skill(isUpgrade);
    }

    public void log_skill(boolean isUpgrade) {
        if (isUpgrade) {
            Map<String, Integer> skillLevelMap = roleFamilyPo.getSkillLevelMap();
            StringBuffer logStr = new StringBuffer();
            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            for (String attr : skillLevelMap.keySet()) {
                map.put(getAttributeIndex(attr), skillLevelMap.get(attr));
            }
            Integer state = null;
            for (int j = 1; j <= 7; j++) {
                state = map.get(j);
                if (state == null) {
                    state = 0;
                }
                if (j == 1) {
                    logStr.append(j).append("@").append(state);
                } else {
                    logStr.append("&").append(j).append("@").append(state);
                }
            }
            ServerLogModule log = (ServerLogModule) module(MConst.ServerLog);
            log.log_personal_family_spell(logStr.toString());
        }
    }

    /* 家族活动入口 */
    public void sendActEntryList() {
        Map<Integer, Integer> maskMap = new HashMap<>();
        for (Map.Entry<Integer, FamilyActEntryFilter> entry : FamilyManager.actEntryFilters.entrySet()) {
            int activityId = entry.getKey();
            FamilyActEntryFilter filter = entry.getValue();
            int mask = filter.getMask(activityId, moduleMap());
            if (mask != FamilyConst.ACT_BTN_MASK_ALL) {
                maskMap.put(activityId, mask);
            }
        }
        List<Integer> notShowList = new ArrayList<>();
        ForeShowModule foreShowModule = module(MConst.ForeShow);
        if (!foreShowModule.isOpen(ForeShowConst.FAMILY_TASK)) {
            notShowList.add(ActConst.ID_FAMILY_TASK);
        }
        ServiceHelper.familyActEntryService().sendEntryList(id(), maskMap, notShowList);
        fireSpecialAccountLogEvent("进入家族活动");
    }

    private FamilySkillVo findNextAvailableAndMinRequirementSkillVo(int remainingContribution, Map<String, Integer> upgradedSkillLevelMap) {
        FamilyLevelVo familyLevelVo = FamilyManager.levelVoMap.get(auth.getFamilyLevel());
        if (familyLevelVo == null) {
            warn(I18n.get("family.management.lackProductData"));
            return null;
        }
        // 先计算下一级心法等级（依据产品数据进行升级）
        Map<String, Integer> nextSkillLevelMap = new HashMap<>(FamilyManager.skillVoMap.size());
        for (String skillName : FamilyManager.skillVoMap.keySet()) {
            Integer skillLevel = upgradedSkillLevelMap.get(skillName);
            skillLevel = skillLevel == null ? roleFamilyPo.getSkillLevelMap().get(skillName) : skillLevel; //
            if (skillLevel != null) {
                nextSkillLevelMap.put(skillName, skillLevel + 1);
            } else {
                nextSkillLevelMap.put(skillName, 1);
            }
        }
        // 筛选可行升级项
        Iterator<Map.Entry<String, Integer>> iterator = nextSkillLevelMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String skillName = entry.getKey();
            int skillLevel = entry.getValue();
            if (!FamilyManager.skillVoMap.containsKey(skillName)) { // 找不到对应心法
                iterator.remove();
                continue;
            }
            FamilySkillVo familySkillVo = FamilyManager.skillVoMap.get(skillName).get(skillLevel);
            if (familySkillVo == null) { // 满级或者策划配错数据的情况
                iterator.remove();
                continue;
            }
            if (skillLevel > familyLevelVo.getSkillLimit()) { // 心法等级不能超过家族等级
                iterator.remove();
                continue;
            }
            if (familySkillVo.getReqContribution() > remainingContribution) { // 判断帮贡够不够
                iterator.remove();
                continue;
            }
            RoleModule roleModule = (RoleModule) module(MConst.Role);
            if (familySkillVo.getReqRoleLevel() > roleModule.getLevel()) { // 判断人物等级
                iterator.remove();
                continue;
            }
        }
        // 获取最小需求/消耗的
        FamilySkillVo minReqFamilySkillVo = null;
        for (Map.Entry<String, Integer> entry : nextSkillLevelMap.entrySet()) {
            FamilySkillVo familySkillVo = FamilyManager.skillVoMap.get(entry.getKey()).get(entry.getValue());
            if (minReqFamilySkillVo == null
                    || familySkillVo.getReqContribution() < minReqFamilySkillVo.getReqContribution()
                    || (familySkillVo.getReqContribution() == minReqFamilySkillVo.getReqContribution()
                    && FamilyManager.skillUpgradeWeightMap.get(familySkillVo.getAttribute()) > FamilyManager.skillUpgradeWeightMap.get(minReqFamilySkillVo.getAttribute()))) {
                minReqFamilySkillVo = familySkillVo;
            }
        }
        return minReqFamilySkillVo;
    }

    private void sendTipsOfFailureToUpgradeSkillLevelAmap(int remainingContribution) {
        FamilyLevelVo familyLevelVo = FamilyManager.levelVoMap.get(auth.getFamilyLevel());
        if (familyLevelVo == null) {
            warn(I18n.get("family.management.lackProductData"));
            return;
        }
        Map<String, Integer> nextSkillLevelMap = new HashMap<>(FamilyManager.skillVoMap.size());
        for (String skillName : FamilyManager.skillVoMap.keySet()) {
            Integer skillLevel = roleFamilyPo.getSkillLevelMap().get(skillName); //
            if (skillLevel != null) {
                nextSkillLevelMap.put(skillName, skillLevel + 1);
            } else {
                nextSkillLevelMap.put(skillName, 1);
            }
        }
        // 等级
        RoleModule roleModule = module(MConst.Role);
        for (Map.Entry<String, Integer> entry : nextSkillLevelMap.entrySet()) {
            FamilySkillVo familySkillVo = FamilyManager.skillVoMap.get(entry.getKey()).get(entry.getValue());
            if (familySkillVo == null) { // no this level
                continue;
            }
            if (familySkillVo.getReqRoleLevel() > roleModule.getLevel()) {
                warn("family_tips_nolevel", Integer.toString(familySkillVo.getReqRoleLevel()));
                return;
            }
        }
        // 材料
        for (Map.Entry<String, Integer> entry : nextSkillLevelMap.entrySet()) {
            FamilySkillVo familySkillVo = FamilyManager.skillVoMap.get(entry.getKey()).get(entry.getValue());
            if (familySkillVo == null) { // no this level
                continue;
            }
            if (familySkillVo.getReqContribution() > remainingContribution) {
                warn("family_tips_reqfamilydonate", Integer.toString(familySkillVo.getReqContribution()));
                return;
            }
        }
        // 家族等级
        for (Map.Entry<String, Integer> entry : nextSkillLevelMap.entrySet()) {
            FamilySkillVo familySkillVo = FamilyManager.skillVoMap.get(entry.getKey()).get(entry.getValue());
            if (familySkillVo == null) { // no this level
                continue;
            }
            if (familySkillVo.getLevel() > familyLevelVo.getSkillLimit()) {
                warn("family_tips_nofamilylevel");
                return;
            }
        }
        // no-op
    }

    private void recalcFamilySkillFightScore(boolean isShow) {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        Attribute attr = new Attribute();
        Map<String, Integer> skillLevelMap = roleFamilyPo.getSkillLevelMap();
        FamilyLevelVo levelVo = FamilyManager.levelVoMap.get(auth.getFamilyLevel());
        if (levelVo == null) {
            roleModule.updatePartAttr("familySkill", attr);
            roleModule.updatePartFightScore("familySkill", FormularUtils.calFightScore(attr));
            roleModule.sendRoleAttr();
            roleModule.sendUpdateFightScore(isShow);
            return;
        }
        revisedSkillLevelMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : skillLevelMap.entrySet()) {
            if (entry.getValue() > levelVo.getSkillLimit()) {
                revisedSkillLevelMap.put(entry.getKey(), levelVo.getSkillLimit());
            } else {
                revisedSkillLevelMap.put(entry.getKey(), entry.getValue());
            }
        }
        // 计算战力
        for (Map.Entry<String, Integer> entry : revisedSkillLevelMap.entrySet()) {
            if (skillLevelMap.containsKey(entry.getKey())) {
                FamilySkillVo skillVo = FamilyManager.skillVoMap.get(entry.getKey()).get(entry.getValue());
                if (skillVo != null) {
                    attr.setSingleAttr(skillVo.getAttribute(), skillVo.getValue());
                }
            }
        }
        // 更新
        roleModule.updatePartAttr("familySkill", attr);
        roleModule.updatePartFightScore("familySkill", FormularUtils.calFightScore(attr));
        roleModule.sendRoleAttr();
        roleModule.sendUpdateFightScore(isShow);
    }

    /**
     * 更新玩家的摘要数据
     */
    private void updateFamilySummary() {
        try {
            ServiceHelper.summaryService().updateSummaryComponent(
                    id(), new FamilySummaryComponentImpl(auth.getFamilyId(), auth.getFamilyName(), auth.getPost().getId(), roleFamilyPo.getSkillLevelMap()));
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    public void sendEmailToMember(String text) {
        if (text.isEmpty()) {
            warn("chat_noempty");
            return;
        }
        if (auth.getPost().getId() != FamilyPost.MASTER_ID) {
            warn("族长才有权限群发邮件");
            return;
        }
        String title = DataManager.getGametext("emailtitle_10000");
        ServiceHelper.familyMainService().sendEmailToMember(auth.getFamilyId(), id(), title, text);
    }

    private void fireSpecialAccountLogEvent(String content) {
        if (SpecialAccountManager.isSpecialAccount(id())) {
            eventDispatcher().fire(new SpecialAccountEvent(id(), content, true));
        }
    }
}
