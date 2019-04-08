package com.stars.modules.baseteam;

import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.baseteam.event.BaseTeamEvent;
import com.stars.modules.baseteam.handler.TeamHandler;
import com.stars.modules.baseteam.packet.ClientBaseTeamInvite;
import com.stars.modules.baseteam.packet.ClientBaseTeamMatch;
import com.stars.modules.baseteam.userdata.BaseTeamInvitor;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by liuyuheng on 2016/11/9.
 */
public class BaseTeamModule extends AbstractModule {
    private byte matchTeamType;// 匹配队伍类型
    private int matchTeamTarget;// 匹配队伍目标

    // 收到的邀请,<teamType, <invitorId, invitor>>
    private Map<Byte, Map<Long, BaseTeamInvitor>> receiveInviteMap = new HashMap<>();

    public BaseTeamModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("组队", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        matchTeamType = -1;
        matchTeamTarget = -1;

        //红点检查
        signCalRedPoint(MConst.Team, RedPointConst.NEW_TEAM_INVITE);
        signCalRedPoint(MConst.Team, RedPointConst.NEW_TEAM_APPLY);
    }

    @Override
    public void onOffline() throws Throwable {
        ServiceHelper.baseTeamService().cancelMatchTeam(id(), matchTeamType, matchTeamTarget);
        ServiceHelper.baseTeamService().leaveTeam(id());
        matchTeamType = -1;
        matchTeamTarget = -1;
    }

    /**
     * 创建队伍
     *
     * @param teamType
     * @param target
     */
    public void createTeam(byte teamType, int target) {
        TeamHandler teamHandler = BaseTeamManager.getHandler(teamType);
        // 业务判断是否可以创建队伍
        teamHandler.createTeam(moduleMap(), selfToTeamMember(teamType), teamType, target);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "请求创建队伍" + " 队伍类型:" + teamType + " 队伍目标:" + target);
        }
        ServerLogModule serverLogModule = module(MConst.ServerLog);
        serverLogModule.logTeamStyle(target, 1);
    }

    /**
     * 可申请队伍列表
     *
     * @param teamType
     */
    public void canApplyTeam(byte teamType) {
        ServiceHelper.baseTeamService().canApplyTeam(id(), teamType);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "请求可申请队伍列表");
        }
    }

    /**
     * 打开可申请队伍列表界面处理
     * 1.打开界面时判定是否满足组队需求，是则加入权限集合
     * 2.关闭界面即从权限集合中移除，即放弃组队申请
     * 3.队长同意申请时，判定申请人是否在权限集合中，不在即申请无效
     */
    public void handleOpenTeamUI(byte teamType) {
        TeamHandler teamHandler = BaseTeamManager.getHandler(teamType);
        if (teamHandler == null) return;
        teamHandler.handleOpenTeamUI(id(), moduleMap());
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "打开可申请队伍列表界面处理");
        }
    }

    /**
     * 关闭可申请队伍列表界面处理
     */
    public void handleCloseTeamUI(byte teamType) {
        TeamHandler teamHandler = BaseTeamManager.getHandler(teamType);
        if (teamHandler == null) return;
        teamHandler.handleCloseTeamUI(id());
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "关闭可申请队伍列表界面处理");
        }
    }

    /**
     * 可邀请成员列表(交由业务处理)
     *
     * @param teamType
     */
    public void canInviteList(byte teamType) {
        TeamHandler teamHandler = BaseTeamManager.getHandler(teamType);
        teamHandler.sendCanInviteList(moduleMap(), id());
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "可邀请成员列表");
        }
    }

    /**
     * 请求收到的邀请列表
     */
    public void reqReceiveInvite(byte teamType) {
        updateReceiveInvite(teamType);
        Map<Long, BaseTeamInvitor> map = receiveInviteMap.get(teamType);
        long now = System.currentTimeMillis();
        ClientBaseTeamInvite packet = new ClientBaseTeamInvite(ClientBaseTeamInvite.RECEIVE_INVITE_LIST);
        packet.setInvitors(map == null ? new ArrayList<BaseTeamInvitor>() : map.values());
        packet.setCurTimes(now);
        send(packet);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "请求收到的邀请列表");
        }
    }

    /**
     * 更新收到的邀请
     *
     * @param teamType 队伍类型
     */
    private void updateReceiveInvite(byte teamType) {
        Map<Long, BaseTeamInvitor> map = receiveInviteMap.get(teamType);
        if (map == null || map.isEmpty())
            return;
        List<BaseTeamInvitor> list = new LinkedList<>(map.values());
        for (BaseTeamInvitor invitor : list) {
            BaseTeam team = ServiceHelper.baseTeamService().getTeam(invitor.getTeamId());
            // 队伍不存在/队伍目标不同,则删除
            if (team == null || team.getTarget() != invitor.getTarget()) {
                map.remove(invitor.getInvitorId());
                continue;
            }
            invitor.setMemberCount(team.getMemberCount());
            BaseTeamMember invitorTeamMember = team.getMember(invitor.getInvitorId());
            if (invitorTeamMember != null) {
                invitor.setLevel(invitorTeamMember.getLevel());
                invitor.setFightScore(invitorTeamMember.getFightSocre());
                invitor.setJob(invitorTeamMember.getJob());
            }
        }

        //红点检查
        signCalRedPoint(MConst.Team, RedPointConst.NEW_TEAM_INVITE);
    }

    /**
     * 发起改变队伍目标
     *
     * @param target
     */
    public void changeTeamTarget(int target, boolean isForce) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
        if (team == null) {
            warn("team_noTeam");
            return;
        }
        TeamHandler teamHandler = BaseTeamManager.getHandler(team.getTeamType());
        //判断队长是否可以切换目标
        if (!teamHandler.canChangeTeamTarget(moduleMap(), id(), target)) {
            return;
        }
        //判断队员是否可以切换目标
        if (!isForce && !teamHandler.canAllChangeTeamTarget(moduleMap(), id(), target)) {
            return;
        }

        /**
         * 改变队伍目标通知申请队列中的所有人
         */
//        RoleModule rm = module(MConst.Role);
//        BaseTeamInvitor tInvitor = new BaseTeamInvitor();
//        tInvitor.setInvitorId(id());
//        tInvitor.setLevel((short) rm.getLevel());
//        tInvitor.setName(rm.getRoleRow().getName());
//        tInvitor.setJob((byte) rm.getRoleRow().getJobId());
//        tInvitor.setFightScore(rm.getRoleRow().getFightScore());
//        tInvitor.setTarget(target);
//        ServiceHelper.baseTeamService().inviteJoinTeam(tInvitor);

        ServiceHelper.baseTeamService().changeTeamTarget(id(), target);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "队伍改变目标为" + target);
        }
    }

    /**
     * 队伍改变了目标,处理事件通知
     *
     * @param newTeamTarget
     */
    public void targetChangedHandler(byte teamType, int newTeamTarget) {
        TeamHandler teamHandler = BaseTeamManager.getHandler(teamType);
        teamHandler.changeTeamTarget(moduleMap(), id(), newTeamTarget);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "队伍改变了目标" + " 队伍类型:" + teamType + " 新队伍目标:" + newTeamTarget);
        }
    }

    /**
     * 队伍改变了目标,清除该队伍的邀请
     */
    public void handleDeleteInvitEvent(BaseTeamEvent event) {
        long invitorId = event.getInvitorId();
        for (Map<Long, BaseTeamInvitor> invitors : receiveInviteMap.values()) {
            if (invitors.containsKey(invitorId)) {
                invitors.remove(invitorId);
            }
        }
    }

    /**
     * 邀请加入队伍
     *
     * @param invitee 被邀请对象
     */
    public void inviteJoinTeam(long invitee) {
        RoleModule rm = module(MConst.Role);
        BaseTeamInvitor tInvitor = new BaseTeamInvitor();
        tInvitor.setInvitorId(id());
        tInvitor.setLevel((short) rm.getLevel());
        tInvitor.setName(rm.getRoleRow().getName());
        tInvitor.setJob((byte) rm.getRoleRow().getJobId());
        tInvitor.setFightScore(rm.getRoleRow().getFightScore());
        ServiceHelper.baseTeamService().inviteJoinTeam(tInvitor, invitee);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "邀请" + invitee + "加入队伍");
        }
    }

    /**
     * 收到邀请
     *
     * @param invitor 邀请者
     */
    public void receiveInvite(BaseTeamInvitor invitor) {
        LogUtil.info("收到组队邀请| {} ", invitor);
        TeamHandler teamHandler = BaseTeamManager.getHandler(invitor.getTeamType());
        if (!teamHandler.selfJoinInTeam(moduleMap(), invitor.getInvitorId(), invitor.getTarget(), null)) {
            return;
        }
        addReceiveInvite(invitor);
        long now = System.currentTimeMillis();
        // 发给被邀请人
        ClientBaseTeamInvite packet = new ClientBaseTeamInvite(ClientBaseTeamInvite.NEW_RECEIVE_INVITE);
        packet.setInvitor(invitor);
        packet.setCurTimes(now);
        send(packet);
        PlayerUtil.send(invitor.getInvitorId(), new ClientText("邀请已发送,等待对方响应"));

        //红点检查
        signCalRedPoint(MConst.Team, RedPointConst.NEW_TEAM_INVITE);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "收到组队邀请" + " 队伍类型:" + invitor.getTeamType() + " 队伍目标:" + invitor.getTarget());
        }
    }

    /**
     * 同意邀请
     *
     * @param invitorId
     */
    public void permitInvite(byte teamType, long invitorId) {
        if (!receiveInviteMap.containsKey(teamType)) {
            warn("team_noInvite");
            return;
        }

        BaseTeamInvitor invitor = receiveInviteMap.get(teamType).get(invitorId);
        if (invitor == null) {
            warn("team_noInvite");

            reqReceiveInvite(teamType);//下发最新的邀请列表到客户端

            //send(packet);
            return;
        }
        // 下发清除信息到客户端
        ClientBaseTeamInvite packet = new ClientBaseTeamInvite(ClientBaseTeamInvite.REMOVE_INVITE);
        packet.setInvitor(invitor);

        BaseTeam baseTeam = ServiceHelper.baseTeamService().getTeam(invitor.getTeamId());
        if (baseTeam == null) {
            warn("playerteam_teaminfo_dissolve");
            send(packet);
            return;
        }
//        if(teamType != baseTeam.getTeamType()){
//            teamType = baseTeam.getTeamType();
//        }
        TeamHandler teamHandler = BaseTeamManager.getHandler(baseTeam.getTeamType());
        if (!teamHandler.selfJoinInTeam(moduleMap(), id(), baseTeam.getTarget(), baseTeam)) {
            return;
        }
        // 加入队伍
        ServiceHelper.baseTeamService().permitInvite(invitor.getTeamId(), selfToTeamMember(teamType));
        // 清除收到的邀请
        receiveInviteMap.get(teamType).remove(invitorId);

        send(packet);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "同意" + invitorId + "邀请");
        }
        //红点检查
        signCalRedPoint(MConst.Team, RedPointConst.NEW_TEAM_INVITE);

        ServerLogModule serverLogModule = module(MConst.ServerLog);
        serverLogModule.logTeamStyle(baseTeam.getTarget(), 2);
    }

    /**
     * 清空邀请列表
     */
    public void clearAllInvite(byte teamType) {
        if (receiveInviteMap.isEmpty() || !receiveInviteMap.containsKey(teamType)) {
            return;
        }
        receiveInviteMap.get(teamType).clear();
        ClientBaseTeamInvite packet = new ClientBaseTeamInvite(ClientBaseTeamInvite.REMOVE_ALL_INVITE);
        packet.setTeamType(teamType);
        send(packet);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "清空邀请列表");
        }
        //红点检查
        signCalRedPoint(MConst.Team, RedPointConst.NEW_TEAM_INVITE);
    }

    /**
     * 申请加入队伍
     *
     * @param teamId
     */
    public void applyJoinTeam(int teamId, int teamTarget, byte teamType) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(teamId);
        // teamId非法,可能队伍已解散
        if (team == null) {
            warn("team_teamNotExist");
            canApplyTeam(teamType);
            return;
        }
        if (team.getTarget() != teamTarget) {
            warn("playerteam_tiptext_switchtarget");
            canApplyTeam(teamType);
            return;
        }
        if (team.isFight()) {
            warn("playerteam_teaminfo_fighting");
            canApplyTeam(teamType);
            return;
        }
        TeamHandler teamHandler = BaseTeamManager.getHandler(team.getTeamType());
        if (!teamHandler.selfJoinInTeam(moduleMap(), id(), team.getTarget(), team)) {
            return;
        }
        ServiceHelper.baseTeamService().applyJoinTeam(teamId, selfToTeamMember(teamType));
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "申请加入队伍" + " 队伍类型:" + teamType + " 队伍目标:" + teamTarget);
        }
        ServerLogModule serverLogModule = module(MConst.ServerLog);
        serverLogModule.logTeamStyle(teamTarget, 2);
    }

    /**
     * 收到入队申请(队长)
     *
     * @param applierId
     */
    public void recieveApplyJoinTeam(long applierId) {
        //红点检查
        signCalRedPoint(MConst.Team, RedPointConst.NEW_TEAM_APPLY);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "作为队长收到入队申请");
        }
    }

    /**
     * 同意入队申请(队长)
     *
     * @param target
     */
    public void permitApply(long target) {
        if (SpecialAccountManager.isSpecialAccount(target)) {
            return;
        }
        ServiceHelper.baseTeamService().permitApply(id(), target);

        //红点检查
        signCalRedPoint(MConst.Team, RedPointConst.NEW_TEAM_APPLY);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "作为队长同意 " + target + " 加入组队");
        }
    }

    /**
     * 拒绝入队申请(队长)
     *
     * @param target
     */
    public void refuseApply(long target) {
        if (SpecialAccountManager.isSpecialAccount(target)) {
            return;
        }
        ServiceHelper.baseTeamService().refuseApply(id(), target);

        //红点检查
        signCalRedPoint(MConst.Team, RedPointConst.NEW_TEAM_APPLY);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "作为队长拒绝 " + target + " 加入组队");
        }
    }

    /**
     * 清空入队申请列表
     */
    public void reqClearApplyList() {
        ServiceHelper.baseTeamService().reqClearApplyList(id());
        //红点检查
        signCalRedPoint(MConst.Team, RedPointConst.NEW_TEAM_APPLY);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "清空入队申请列表");
        }
    }

    /**
     * 加入队伍(从聊天窗口直接加入)
     *
     * @param teamId
     */
    public void joinTeamByChat(int teamId, int teamTarget) {
        ForeShowModule open = module(MConst.ForeShow);
        if (teamTarget == 101) {
            //守护屈原
            if (!open.isOpen(ForeShowConst.DAILY_TEAM_DEFEND)) {
                warn("foretips_dailyteam_protect");
                return;
            }
        }
        if (teamTarget == 102) {
            //组队挑战
            if (!open.isOpen(ForeShowConst.DAILY_TEAM_CHALLENGE)) {
                warn("foretips_dailyteam_challenge");
                return;
            }
        }
        SceneModule scene = module(MConst.Scene);
        if (scene.getScene().getSceneType() != SceneManager.SCENETYPE_CITY) {
            warn("team_fighting");
            return;
        }
        if (ServiceHelper.baseTeamService().hasTeam(id())) {
            warn("playerteam_selectotherteam");
            return;
        }
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(teamId);
        if (team == null) {
            warn("playerteam_teaminfo_dissolve");
            return;
        }
        if (team.getTarget() != teamTarget) {
            warn("playerteam_tiptext_switchtarget");
            return;
        }
        TeamHandler teamHandler = BaseTeamManager.getHandler(team.getTeamType());
        if (!teamHandler.selfJoinInTeam(moduleMap(), id(), team.getTarget(), team)) {
            return;
        }
        ServiceHelper.baseTeamService().joinTeam(teamId, selfToTeamMember(team.getTeamType()));
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "从聊天窗口直接加入队伍");
        }

        ServerLogModule serverLogModule = module(MConst.ServerLog);
        serverLogModule.logTeamStyle(team.getTarget(), 2);
    }

    /**
     * 离队
     */
    public void leaveTeam() {
        ServiceHelper.baseTeamService().leaveTeam(id());
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "离开队伍");
        }
    }

    /**
     * 踢出队伍
     *
     * @param target
     */
    public void kickOutTeam(long target) {
        ServiceHelper.baseTeamService().kickOutTeam(id(), target);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "将 " + target + " 踢出队伍");
        }
    }

    /**
     * 转让队长
     *
     * @param target
     */
    public void changeCaptain(long target) {
        ServiceHelper.baseTeamService().changeCaptain(id(), target);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "将 " + target + " 设置为队长");
        }
    }

    /**
     * 设置开放申请标志
     *
     * @param flag
     */
    public void setOpenApplyFlag(byte flag) {
        ServiceHelper.baseTeamService().setAutoApplyFlag(id(), flag);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "设置开放申请标志为:" + flag);
        }
    }

    /**
     * 请求匹配队伍
     *
     * @param teamType
     * @param teamTarget
     */
    public void reqMatchTeam(byte teamType, int teamTarget) {
        if (ServiceHelper.baseTeamService().hasTeam(id())) {
            return;
        }
        TeamHandler teamHandler = BaseTeamManager.getHandler(teamType);
        if (!teamHandler.selfJoinInTeam(moduleMap(), id(), teamTarget, null)) {
            ClientBaseTeamMatch ctm = new ClientBaseTeamMatch(ClientBaseTeamMatch.CANCEL_MATCH_TEAM);
            send(ctm);
            return;
        }
        if (matchTeamType != -1 && matchTeamTarget != -1) {
            ServiceHelper.baseTeamService().cancelMatchTeam(id(), matchTeamType, matchTeamTarget);
        }
        this.matchTeamType = teamType;
        this.matchTeamTarget = teamTarget;
        ServiceHelper.baseTeamService().startMatchTeam(selfToTeamMember(teamType), matchTeamType, matchTeamTarget);
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "请求匹配队伍");
        }
    }

    /**
     * 取消匹配队伍
     */
    public void reqCancelMatchTeam(boolean showCancelTips) {
        ServiceHelper.baseTeamService().cancelMatchTeam(id(), matchTeamType, matchTeamTarget);
        matchTeamType = -1;
        matchTeamTarget = -1;
        ClientBaseTeamMatch ctm = new ClientBaseTeamMatch(ClientBaseTeamMatch.CANCEL_MATCH_TEAM);
        send(ctm);
        if (showCancelTips) {
            warn("team_cancelMatchTeamSuc");
        }
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "取消匹配队伍");
        }
    }

    /**
     * 请求匹配队员
     */
    public void reqMatchMember() {
        // 没有队伍
        if (!ServiceHelper.baseTeamService().hasTeam(id())) {
            return;
        }
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
        this.matchTeamType = team.getTeamType();
        this.matchTeamTarget = team.getTarget();
        ServiceHelper.baseTeamService().matchMemeber(id());
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "请求匹配队伍");
        }
    }

    /**
     * 取消匹配队员
     */
    public void reqCancelMatchMember(String notice) {
        ServiceHelper.baseTeamService().cancelMatchTeam(id(), matchTeamType, matchTeamTarget);
        matchTeamType = -1;
        matchTeamTarget = -1;
        send(new ClientBaseTeamMatch(ClientBaseTeamMatch.CANCEL_MATCH_MEMBER));
        if (notice != null) {
            warn(notice);
        }
        //warn("team_cancelMatchMemberSuc");
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "取消匹配队员");
        }
    }

    /**
     * 加入队伍 事件处理
     */
    public void joinTeamHandler(byte teamType) {
        ServiceHelper.baseTeamService().cancelMatchTeam(id(), matchTeamType, matchTeamTarget);
        matchTeamType = -1;
        matchTeamTarget = -1;
        TeamHandler teamHandler = BaseTeamManager.getHandler(teamType);
        teamHandler.afterJoinTeam(moduleMap(), id());
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "加入队伍" + " 队伍类型:" + teamType);
        }
    }

    /**
     * 根据自己创建一个teamMember
     * @param teamType TODO
     *
     * @return
     */
    public BaseTeamMember selfToTeamMember(byte teamType) {
        RoleModule rm = module(MConst.Role);
        TeamHandler teamHandler = BaseTeamManager.getHandler(teamType);
        BaseTeamMember tm = null;
        if(teamHandler!=null){        	
        	tm = teamHandler.createBaseTeamMember(moduleMap());
        }
        if(tm==null){
        	tm = new BaseTeamMember();
        }
        tm.setRoleId(id());
        tm.setJob((byte) rm.getRoleRow().getJobId());
        FamilyModule familyModule = module(MConst.Family);
        tm.setFamilyName(familyModule.getAuth().getFamilyName());
        FighterEntity fighterEntity = FighterCreator.createSelf(moduleMap());
        // 需要把类型设置为1=player
        fighterEntity.setFighterType(FighterEntity.TYPE_PLAYER);
        tm.addEntity(fighterEntity);
        // 加入伙伴,如果不允许屏蔽此处即可
        BuddyModule bm = module(MConst.Buddy);
        if (bm.getFightBuddyId() != 0) {
            tm.addEntity(FighterCreator.create(FighterEntity.TYPE_BUDDY, FighterEntity.CAMP_SELF,
                    bm.getRoleBuddy(bm.getFightBuddyId())));
        }
        return tm;
    }


    /**
     * 更新组队中的成员属性
     */
    public void updateTeamMember() {
    	BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
    	if(team==null) return;
        ServiceHelper.baseTeamService().updateTeamMember(selfToTeamMember(team.getTeamType()));
        if (isSpecialAccount()) {
            fireSpecialAccountLogEvent(id() + "更新组队中的成员属性");
        }
    }

    private void addReceiveInvite(BaseTeamInvitor invitor) {
        if (!receiveInviteMap.containsKey(invitor.getTeamType())) {
            receiveInviteMap.put(invitor.getTeamType(), new HashMap<Long, BaseTeamInvitor>());
        }
        receiveInviteMap.get(invitor.getTeamType()).put(invitor.getInvitorId(), invitor);
    }

    public void backToCity() {
        SceneModule sceneModule = module(MConst.Scene);
        sceneModule.backToCity(Boolean.FALSE);
    }

    /**
     * 红点
     */
    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.NEW_TEAM_INVITE))) {
            Set<Long> inviteSet = new HashSet<Long>();
            for (Map<Long, BaseTeamInvitor> perTypeInviteMap : receiveInviteMap.values()) {
                for (BaseTeamInvitor invitor : perTypeInviteMap.values()) {
                    inviteSet.add(invitor.getInvitorId());
                }
            }
            checkRedPoint(redPointMap, inviteSet, RedPointConst.NEW_TEAM_INVITE);
        }
        if (redPointIds.contains(Integer.valueOf(RedPointConst.NEW_TEAM_APPLY))) {
            BaseTeam team = ServiceHelper.baseTeamService().getTeam(id());
            if (team != null && team.getCaptainId() == id()) {
                Map<Long, BaseTeamMember> teamAppliers = team.getApplyMembers();
                Set<Long> applySet = new HashSet<Long>();
                for (BaseTeamMember applier : teamAppliers.values()) {
                    applySet.add(applier.getRoleId());
                }
                checkRedPoint(redPointMap, applySet, RedPointConst.NEW_TEAM_APPLY);
            }
        }
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

    private void fireSpecialAccountLogEvent(String content) {
        eventDispatcher().fire(new SpecialAccountEvent(id(), content, true));
    }

    public boolean isSpecialAccount() {
        return SpecialAccountManager.isSpecialAccount(id());
    }
}
