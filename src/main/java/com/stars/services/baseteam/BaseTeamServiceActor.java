package com.stars.services.baseteam;

import com.stars.core.event.Event;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.player.PlayerUtil;
import com.stars.ExcutorKey;
import com.stars.core.schedule.SchedulerManager;
import com.stars.modules.baseteam.BaseTeamManager;
import com.stars.modules.baseteam.event.BaseTeamEvent;
import com.stars.modules.baseteam.handler.TeamHandler;
import com.stars.modules.baseteam.packet.ClientBaseTeamApply;
import com.stars.modules.baseteam.packet.ClientBaseTeamInfo;
import com.stars.modules.baseteam.packet.ClientBaseTeamInvite;
import com.stars.modules.baseteam.userdata.BaseTeamInvitor;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.teamdungeon.TeamDungeonManager;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.role.RoleNotification;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by liuyuheng on 2016/11/8.
 */
public class BaseTeamServiceActor extends ServiceActor implements BaseTeamService {
    private int idCreator;

    private Map<Integer, BaseTeam> teamMap;// <teamId, AbstractTeam>
    private Map<Byte, List<Integer>> type2Team;// 队伍类型-队伍Id映射
    private Map<Long, Integer> role2Team;// 玩家Id-队伍Id映射
    // 等待匹配队伍的玩家集合,<teamType+target, <roleId, teamMember>>
    private Map<String, Map<Long, BaseTeamMember>> matchTeamMap;

    private Map<Long, BaseTeamMatch> role2Match = new HashMap<>();

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd("teamService", this);
        teamMap = new HashMap<>();
        type2Team = new HashMap<>();
        role2Team = new HashMap<>();
        matchTeamMap = new HashMap<>();
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.BASETEAM, new BaseTeamTask(), 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出:{},matchTeamMap.size:{}", this.getClass().getSimpleName(), matchTeamMap.size());
    }

//    @Override
//    public void schedule() {
//        int now = DateUtil.getSecondTime();
//        int deadLine = (int) (TeamDungeonManager.matchTeamTime / 1000);
//        Set<Long> matchList = new HashSet<>(role2Match.keySet());
//        for (long roleId : matchList) {
//            BaseTeamMatch match = role2Match.get(roleId);
//            if (match == null) {
//                continue;
//            }
//            TeamHandler teamHandler = BaseTeamManager.getHandler(match.getTeamType());
//            if (now - match.getStamp() > deadLine * 2) {
//                // 移除没人管的匹配信息
//                role2Match.remove(roleId);
//                continue;
//            }
//            if (now - match.getStamp() >= deadLine - 2) {
//                // 强行匹配
//                if (match.getMatchType() == BaseTeamMatch.MATCH_MEMBER) {
//                    BaseTeam team = getTeam(match.getRoleId());
//                    List<BaseTeamMember> fakeMembers = teamHandler.matchFakePlayer(team.getCaptain(),
//                            team.getMaxMemberCount() - team.getMemberCount(), new LinkedList<>(team.getMembers().keySet()));
//                    if (fakeMembers == null || fakeMembers.isEmpty()) {
//                        PlayerUtil.send(team.getCaptainId(), new ClientText("playerteam_tiptext_matchfinish"));
//                        BaseTeamEvent teamEvent = new BaseTeamEvent(BaseTeamEvent.CANCEL_MATCH_MEMBER);
//                        teamEvent.setNotice("team_cancelMatchMemberSuc");;
//                        ServiceHelper.roleService().notice(roleId, teamEvent);
//                    } else {
//                        matchFakeMember(team.getCaptainId(), fakeMembers);
//                    }
//                } else {
//                    teamHandler = BaseTeamManager.getHandler(match.getTeamType());
//                    byte teamType = match.getTeamType();
//                    int teamTarget = match.getTarget();
//                    if (!teamHandler.matchTeamWithFakePlayer(makeFakePlayer(roleId, teamType, teamTarget), teamType, teamTarget)) {
//                        PlayerUtil.send(roleId, new ClientText("playerteam_tiptext_matchfinish"));
//                    }
//                    cancelMatchTeam(roleId, teamType, teamTarget);
//                }
//                role2Match.remove(roleId);
//                continue;
//            }
//            if (match.getMatchType() == BaseTeamMatch.MATCH_MEMBER) {
//                BaseTeam team = getTeam(match.getRoleId());
//                matchMemeber(team.getCaptainId());
//            } else {
//                matchTeam(match.getRoleId(), match.getTeamType(), match.getTarget());
//            }
//        }
//    }

    @Override
    public void schedule() {
        int now = DateUtil.getSecondTime();
        int deadLine = (int) (TeamDungeonManager.matchTeamTime / 1000);
        Set<Long> matchList = new HashSet<>(role2Match.keySet());
        for (long roleId : matchList) {
            BaseTeamMatch match = role2Match.get(roleId);
            if (match == null) {
                continue;
            }
            TeamHandler teamHandler = BaseTeamManager.getHandler(match.getTeamType());
            if (!teamHandler.isCanMatch(match)) {
                cancelMatchTeam(roleId, match.getTeamType(), match.getTarget());
                continue;
            }
            if (match.getTeamType() == BaseTeamManager.TEAM_TYPE_ELITEDUNGEON) {
                if (match.getMatchType() == BaseTeamMatch.MATCH_MEMBER) {
                    BaseTeam team = getTeam(match.getRoleId());
//				if(team.isFull()){
//					cancelMatchTeam(roleId, match.getTeamType(), match.getTarget());
//					continue;
//				}
                    if (team == null) {
                        continue;
                    }
                    List<BaseTeamMember> fakeMembers = teamHandler.matchFakePlayer(team.getCaptain(),
                            team.getMaxMemberCount() - team.getMemberCount(), new LinkedList<>(team.getMembers().keySet()), match, team);
                    if (fakeMembers == null || fakeMembers.isEmpty()) {
//					PlayerUtil.send(team.getCaptainId(), new ClientText("playerteam_tiptext_matchfinish"));
//					BaseTeamEvent teamEvent = new BaseTeamEvent(BaseTeamEvent.CANCEL_MATCH_MEMBER);
//					teamEvent.setNotice("team_cancelMatchMemberSuc");
//					ServiceHelper.roleService().notice(roleId, teamEvent);
                    } else {
                        matchFakeMember(team.getCaptainId(), fakeMembers);
                    }
                }
            } else {
                if (now - match.getStamp() > deadLine * 2) {
                    // 移除没人管的匹配信息
                    role2Match.remove(roleId);
                    continue;
                }
                if (now - match.getStamp() >= deadLine - 2) {
                    // 强行匹配
                    if (match.getMatchType() == BaseTeamMatch.MATCH_MEMBER) {
                        BaseTeam team = getTeam(match.getRoleId());
                        List<BaseTeamMember> fakeMembers = teamHandler.matchFakePlayer(team.getCaptain(),
                                team.getMaxMemberCount() - team.getMemberCount(), new LinkedList<>(team.getMembers().keySet()), match, team);
                        if (fakeMembers == null || fakeMembers.isEmpty()) {
                            PlayerUtil.send(team.getCaptainId(), new ClientText("playerteam_tiptext_matchfinish"));
                            BaseTeamEvent teamEvent = new BaseTeamEvent(BaseTeamEvent.CANCEL_MATCH_MEMBER);
                            teamEvent.setNotice("team_cancelMatchMemberSuc");
                            ;
                            ServiceHelper.roleService().notice(roleId, teamEvent);
                        } else {
                            matchFakeMember(team.getCaptainId(), fakeMembers);
                        }
                    } else {
                        teamHandler = BaseTeamManager.getHandler(match.getTeamType());
                        byte teamType = match.getTeamType();
                        int teamTarget = match.getTarget();
                        if (!teamHandler.matchTeamWithFakePlayer(makeFakePlayer(roleId, teamType, teamTarget), teamType, teamTarget)) {
                            PlayerUtil.send(roleId, new ClientText("playerteam_tiptext_matchfinish"));
                        }
                        cancelMatchTeam(roleId, teamType, teamTarget);
                    }
                    role2Match.remove(roleId);
                    continue;
                }
                if (match.getMatchType() == BaseTeamMatch.MATCH_MEMBER) {
                    BaseTeam team = getTeam(match.getRoleId());
                    matchMemeber(team.getCaptainId());
                } else {
                    matchTeam(match.getRoleId(), match.getTeamType(), match.getTarget());
                }
            }
        }
    }

    private String getKey(byte teamType, int teamTarget) {
        StringBuilder builder = new StringBuilder();
        builder.append(teamType).append("+").append(teamTarget);
        return builder.toString();
    }

    private BaseTeamMember makeFakePlayer(long roleId, byte teamType, int teamTarget) {
        Map<Long, BaseTeamMember> map = matchTeamMap.get(getKey(teamType, teamTarget));
        BaseTeamMember player = map.get(roleId);
        BaseTeamMember member = new BaseTeamMember();
        member.setRoleId(player.getRoleId());
        member.setJob(player.getJob());
        member.setFamilyName(player.getFamilyName());
        for (FighterEntity entry : player.getEntityMap().values()) {
            member.addEntity(entry.copy());
        }
        return member;
    }

    @Override
    public byte getTeamType(int teamId) {
        return teamMap.containsKey(teamId) ? teamMap.get(teamId).getTeamType() : -1;
    }

    @Override
    public BaseTeam getTeam(long roleId) {
//        LogUtil.info("获取role2Team:{}", role2Team);
        if (!role2Team.containsKey(roleId)) {
            return null;
        }
        return teamMap.get(role2Team.get(roleId));
    }

    @Override
    public boolean isCaptain(long roleId) {
        if (!role2Team.containsKey(roleId)) {
            return false;
        }
        BaseTeam team = teamMap.get(role2Team.get(roleId));
        if (team == null) return false;
        return team.getCaptainId() == roleId;
    }

    @Override
    public int getTeamFighting(long roleId) {
        if (!role2Team.containsKey(roleId)) {
            return -1;
        }
        BaseTeam team = teamMap.get(role2Team.get(roleId));
        if (team == null) return -1;

        return team.getPlayerTotalFighting();
    }

    /**
     * 获得队伍id，无队伍时返回-1
     */
    public int getTeamId(long roleId) {
        if (!role2Team.containsKey(roleId)) {
            return -1;
        }
        BaseTeam team = teamMap.get(role2Team.get(roleId));
        if (team == null) return -1;
        return team.getTeamId();
    }

    @Override
    public BaseTeam getTeam(int teamId) {
        return teamMap.get(teamId);
    }

    @Override
    public boolean hasTeam(long roleId) {
        return role2Team.containsKey(roleId);
    }

    @Override
    public void updateTeamMember(BaseTeamMember teamMember) {
        if (!role2Team.containsKey(teamMember.getRoleId())) {
            return;
        }
        BaseTeam team = teamMap.get(role2Team.get(teamMember.getRoleId()));
        if (team == null)
            return;
        team.addUpdateMember(teamMember);
    }


    @Override
    public void createTeam(BaseTeamMember creator, byte teamType, byte minMemberCount, byte maxMemberCount, int target) {
        if (role2Team.containsKey(creator.getRoleId())) {
            PlayerUtil.send(creator.getRoleId(), new ClientText("您已加入队伍"));
            return;
        }
        if (role2Match.containsKey(creator.getRoleId())) {
            BaseTeamMatch bMatch = role2Match.get(creator.getRoleId());
            if (bMatch != null) {
                this.cancelMatchTeam(creator.getRoleId(), bMatch.getTeamType(), bMatch.getTarget());
            }
        }
        createTeamSync(creator, teamType, minMemberCount, maxMemberCount, target);
        PlayerUtil.send(creator.getRoleId(), new ClientText("playerteam_suctext_buildteam"));
//        ServiceHelper.roleService().notice(creator.getRoleId(), new BaseTeamEvent(BaseTeamEvent.CANCEL_MATCH_TEAM));
    }

    @Override
    public BaseTeam createTeamSync(BaseTeamMember creator, byte teamType, byte minMemberCount, byte maxMemberCount, int target) {
        if (role2Team.containsKey(creator.getRoleId())) {
            PlayerUtil.send(creator.getRoleId(), new ClientText("您已加入队伍"));
            return null;
        }
        if (role2Match.containsKey(creator.getRoleId())) {
            BaseTeamMatch bMatch = role2Match.get(creator.getRoleId());
            if (bMatch != null) {
                this.cancelMatchTeam(creator.getRoleId(), bMatch.getTeamType(), bMatch.getTarget());
            }
        }
        BaseTeam team = new BaseTeam(newTeamId(), teamType, minMemberCount, maxMemberCount, creator, target);
        teamMap.put(team.getTeamId(), team);
        role2Team.put(creator.getRoleId(), team.getTeamId());
        putType2Team(teamType, team.getTeamId());
        ClientBaseTeamInfo packet = new ClientBaseTeamInfo(ClientBaseTeamInfo.TEAMINFO);
        packet.setTeam(team);
        PlayerUtil.send(creator.getRoleId(), packet);
        LogUtil.info("队伍role2Team:{}", role2Team);
        handleAfterJoinTeam(teamType, creator.getRoleId());
        ServiceHelper.chatService().addTeamMemberId(team.getTeamId(), creator.getRoleId());
        return team;
    }

    @Override
    public void createTeamWithFakePlayer(BaseTeamMember creator, byte teamType, byte minMemberCount, byte maxMemberCount,
                                         int target, List<BaseTeamMember> fakeMembers) {
        if (role2Team.containsKey(creator.getRoleId())) {
            PlayerUtil.send(creator.getRoleId(), new ClientText("您已加入队伍"));
            return;
        }
        if (role2Match.containsKey(creator.getRoleId())) {
            BaseTeamMatch bMatch = role2Match.get(creator.getRoleId());
            this.cancelMatchTeam(creator.getRoleId(), bMatch.getTeamType(), bMatch.getTarget());
        }
        BaseTeam team = new BaseTeam(newTeamId(), teamType, minMemberCount, maxMemberCount, creator, target);
        teamMap.put(team.getTeamId(), team);
        role2Team.put(creator.getRoleId(), team.getTeamId());
        putType2Team(teamType, team.getTeamId());
        ClientBaseTeamInfo packet = new ClientBaseTeamInfo(ClientBaseTeamInfo.TEAMINFO);
        packet.setTeam(team);
        PlayerUtil.send(creator.getRoleId(), packet);
        for (BaseTeamMember teamMember : fakeMembers) {
            putNewMemberToTeam(team, teamMember, "playerteam_applyteam_addsuc", "playerteam_applyteam_otherentered");
        }
        ServiceHelper.chatService().addTeamMemberId(team.getTeamId(), creator.getRoleId());
    }

    @Override
    public void canApplyTeam(long initiator, byte teamType) {
        List<BaseTeam> canApplyTeam = new LinkedList<>();
        List<Integer> teamIdList = type2Team.get(teamType);
        if (teamIdList != null) {
            for (int teamId : teamIdList) {
                BaseTeam team = teamMap.get(teamId);
                if (team == null)
                    continue;
                if (team.isFight() || team.isFull())
                    continue;
                canApplyTeam.add(team);
            }
        }
        ClientBaseTeamApply packet = new ClientBaseTeamApply(ClientBaseTeamApply.CAN_APPLY_TEAM);
        packet.setTeamList(canApplyTeam);
        PlayerUtil.send(initiator, packet);
    }

    @Override
    public void startMatchTeam(BaseTeamMember teamMember, byte teamType, int target) {
        if (role2Team.containsKey(teamMember.getRoleId())) {
            return;
        }
        if (role2Match.containsKey(teamMember.getRoleId())) {
            BaseTeamMatch bMatch = role2Match.get(teamMember.getRoleId());
            if (bMatch != null) {
                cancelMatchTeam(teamMember.getRoleId(), bMatch.getTeamType(), bMatch.getTarget());
            }
        }
        newMatch(teamMember.getRoleId(), BaseTeamMatch.MATCH_TEAM, teamType, target);
        String key = getKey(teamType, target);
        Map<Long, BaseTeamMember> map = this.matchTeamMap.get(key);
        if (map == null) {
            map = new TreeMap<>();
            this.matchTeamMap.put(key, map);
        }
        map.put(teamMember.getRoleId(), teamMember);
        matchTeam(teamMember.getRoleId(), teamType, target);
    }

    public void newMatch(long roleId, byte matchType, byte teamType, int target) {
        if (role2Match.containsKey(roleId)) {
            return;
        }
        BaseTeamMatch match = new BaseTeamMatch();
        match.setRoleId(roleId);
        match.setMatchType(matchType);
        match.setStamp(DateUtil.getSecondTime());
        match.setTeamType(teamType);
        match.setTarget(target);
        role2Match.put(roleId, match);
        TeamHandler teamHandler = BaseTeamManager.getHandler(match.getTeamType());
        BaseTeam team = getTeam(roleId);
        teamHandler.newMatchHandle(match, team);
    }

    private void cancelMatch(long roleId) {
        role2Match.remove(roleId);
    }

    private void matchTeam(long initiator, byte teamType, int target) {
        String key = getKey(teamType, target);
        if (!matchTeamMap.containsKey(key)) {
            return;
        }
        BaseTeamMember teamMember = this.matchTeamMap.get(key).get(initiator);
        if (teamMember == null) {
            return;
        }
        List<Integer> teamIdList = type2Team.get(teamType);
        // 没有队伍
        if (teamIdList == null) {
            return;
        }
        for (int teamId : teamIdList) {
            BaseTeam team = teamMap.get(teamId);
            if (team == null)
                continue;
            // 目标过滤
            if (team.getTarget() != target)
                continue;
            if (!team.canMatch())
                continue;
            putNewMemberToTeam(team, teamMember, "playerteam_applyteam_leaderaggre", "playerteam_applyteam_otherentered");
            this.matchTeamMap.get(key).remove(initiator);
            cancelMatch(initiator); // 取消自动匹配
            break;
        }
    }

    @Override
    public void cancelMatchTeam(long initiator, byte teamType, int target) {
        String key = getKey(teamType, target);
        Map<Long, BaseTeamMember> map = this.matchTeamMap.get(key);
        if (map != null) {
            map.remove(initiator);
        }
        cancelMatch(initiator);
    }

    @Override
    public void matchMemeber(long initiator) {
        // 没有队伍
        if (!role2Team.containsKey(initiator)) {
            return;
        }
        BaseTeam team = teamMap.get(role2Team.get(initiator));
        if (team == null) {
            return;
        }
        // 不是队长
        if (team.getCaptainId() != initiator) {
            return;
        }
        // 战斗中
        if (team.isFight()) {
            return;
        }
        // 人数已满
        if (team.isFull()) {
            BaseTeamEvent event = new BaseTeamEvent(BaseTeamEvent.CANCEL_MATCH_MEMBER);
            event.setNotice("playerteam_tiptext_fullnum");
            ServiceHelper.roleService().notice(initiator, event);
            return;
        }

        // 先加入自动匹配中，匹配到了就删除自动匹配
        newMatch(initiator, BaseTeamMatch.MATCH_MEMBER, team.getTeamType(), team.getTarget());

        // 先查找真实玩家中，是否有符合条件的，有则让其加入队伍中
        String key = getKey(team.getTeamType(), team.getTarget());
        Map<Long, BaseTeamMember> playerMap = matchTeamMap.get(key);
        List<Long> joinedTeamMemberList = new ArrayList<>();
        if (playerMap != null && playerMap.size() >= 0) {
            for (BaseTeamMember teamMember : playerMap.values()) {
                if (role2Team.containsKey(teamMember.getRoleId())) {
                    continue;
                }
                putNewMemberToTeam(team, teamMember, "playerteam_applyteam_leaderaggre", "playerteam_applyteam_otherentered");
                joinedTeamMemberList.add(teamMember.getRoleId());
                BaseTeamEvent teamEvent = new BaseTeamEvent(BaseTeamEvent.CANCEL_MATCH_MEMBER);
                if (team.isFull()) {
                    teamEvent.setNotice("playerteam_tiptext_fullnum");
                }
                ServiceHelper.roleService().notice(initiator, teamEvent);
                cancelMatch(initiator);
                break;
            }
        }
        for (Long roleId : joinedTeamMemberList) {
            cancelMatchTeam(roleId, team.getTeamType(), team.getTarget());
        }
    }

    @Override
    public void matchFakeMember(long initiator, List<BaseTeamMember> fakeMembers) {
        // 没有队伍
        if (!role2Team.containsKey(initiator)) {
            return;
        }
        BaseTeam team = teamMap.get(role2Team.get(initiator));
        if (team == null) {
            return;
        }
        // 不是队长
        if (team.getCaptainId() != initiator) {
            return;
        }
        // 人数已满
        if (team.isFull()) {
            BaseTeamEvent event = new BaseTeamEvent(BaseTeamEvent.CANCEL_MATCH_MEMBER);
            event.setNotice("playerteam_tiptext_fullnum");
            ServiceHelper.roleService().notice(initiator, event);
            return;
        }
        for (BaseTeamMember teamMember : fakeMembers) {
            if (team.isMember(teamMember.getRoleId()))
                continue;
            putNewMemberToTeam(team, teamMember, "playerteam_applyteam_leaderaggre", "playerteam_applyteam_otherentered");
        }
        if (team.isFull()) {
            BaseTeamEvent teamEvent = new BaseTeamEvent(BaseTeamEvent.CANCEL_MATCH_MEMBER);
            teamEvent.setNotice("playerteam_tiptext_fullnum");
            cancelMatch(initiator); // 移除匹配成员的队列
            ServiceHelper.roleService().notice(initiator, teamEvent);
        }

    }

    @Override
    public void inviteJoinTeam(BaseTeamInvitor invitor, long inviteeId) {
        LogUtil.info("组队邀请|invitor:{},invitee:{}", invitor.getInvitorId(), inviteeId);
        // 没有队伍
        if (!role2Team.containsKey(invitor.getInvitorId())) {
            return;
        }
        BaseTeam team = teamMap.get(role2Team.get(invitor.getInvitorId()));
        if (team == null) {
            return;
        }
        // 不是队长
//        if (team.getCaptainId() != invitor.getInvitorId()) {
//            return;
//        }
        // 人数已满
        if (team.isFull()) {
            PlayerUtil.send(invitor.getInvitorId(), new ClientText("playerteam_inviteother_fullteam"));
            return;
        }
        if (role2Team.containsKey(inviteeId)) {
            PlayerUtil.send(invitor.getInvitorId(), new ClientText("对方已有队伍"));
            return;
        }
        /**
         * 判断是否能接受邀请
         */
        TeamHandler teamHandler = BaseTeamManager.getHandler(team.getTeamType());
        if (!teamHandler.canBeInvite(invitor.getInvitorId(), inviteeId, team.getTarget())) {
            Player player = PlayerSystem.get(invitor.getInvitorId());
            //player.send(new ClientText("对方副本未开启"));
            player.send(new ClientText("对方不能接受邀请"));
            return;
        }

        /**
         * 邀请列表添加被邀请人roleid
         */
        team.getInvitedMembers().add(inviteeId);

        invitor.setTeamId(team.getTeamId());
        invitor.setTeamType(team.getTeamType());
        invitor.setMemberCount(team.getMemberCount());
        invitor.setMaxMemberCount(team.getMaxMemberCount());
        invitor.setTarget(team.getTarget());
        BaseTeamEvent teamEvent = new BaseTeamEvent(BaseTeamEvent.RECEIVE_INVITE);
        teamEvent.setTeamInvitor(invitor);
        ServiceHelper.roleService().notice(inviteeId, new RoleNotification(teamEvent));
    }

    /**
     * 队伍目标改变给邀请列表中的所有人重新发新邀请信息
     *
     * @param invitor
     */
    @Override
    public void inviteJoinTeam(BaseTeamInvitor invitor) {
        long invitorId = invitor.getInvitorId();
        BaseTeam baseTeam = teamMap.get(role2Team.get(invitorId));
        for (Long roleId : baseTeam.getInvitedMembers()) {
            ServiceHelper.baseTeamService().inviteJoinTeam(invitor, roleId);
        }
    }

    @Override
    public void permitInvite(int teamId, BaseTeamMember teamMember) {
        LogUtil.info("接受邀请加入队伍|teamId:{},teamMember:{}", teamId, teamMember.toString());
        // 已有队伍
        if (role2Team.containsKey(teamMember.getRoleId())) {
            PlayerUtil.send(teamMember.getRoleId(), new ClientText("playerteam_selectotherteam"));
            return;
        }
        BaseTeam team = teamMap.get(teamId);
        // 队伍不存在
        if (team == null) {
            PlayerUtil.send(teamMember.getRoleId(), new ClientText("playerteam_teaminfo_dissolve"));
            return;
        }
        // 队伍人数已满
        if (team.isFull()) {
            PlayerUtil.send(teamMember.getRoleId(), new ClientText("playerteam_tiptext_memberfull"));
            return;
        }
        // 战斗中
        if (team.isFight()) {
            PlayerUtil.send(teamMember.getRoleId(), new ClientText("playerteam_teaminfo_fighting"));
            return;
        }
        putNewMemberToTeam(team, teamMember, "playerteam_applyteam_addsuc", "playerteam_applyteam_otherentered");
    }

    @Override
    public void reqApplyList(long initiator) {
        // 操作人没有队伍
        if (!role2Team.containsKey(initiator)) {
            return;
        }
        BaseTeam team = teamMap.get(role2Team.get(initiator));
        if (team == null) {
            return;
        }
        ClientBaseTeamApply packet = new ClientBaseTeamApply(ClientBaseTeamApply.APPLY_LIST);
        packet.setApplyMembers(team.getApplyMembers());
        PlayerUtil.send(initiator, packet);
        if (SpecialAccountManager.isSpecialAccount(initiator)) {
            ServiceHelper.roleService().notice(initiator, new SpecialAccountEvent(initiator, "请求组队申请列表", true));
        }
    }

    @Override
    public void applyJoinTeam(int teamId, BaseTeamMember applier) {
        // 已有队伍
        if (role2Team.containsKey(applier.getRoleId())) {
            PlayerUtil.send(applier.getRoleId(), new ClientText("您已有队伍"));
            return;
        }
        BaseTeam team = teamMap.get(teamId);
        if (team == null) {
            PlayerUtil.send(applier.getRoleId(), new ClientText("playerteam_tiptext_breakteam"));
            // send to client,
            ClientBaseTeamApply packet = new ClientBaseTeamApply(ClientBaseTeamApply.TEAM_NOT_EXIT);
            PlayerUtil.send(applier.getRoleId(), packet);
            return;
        }
        // 队伍战斗中
        if (team.isFight()) {
            PlayerUtil.send(applier.getRoleId(), new ClientText("playerteam_teaminfo_fighting"));
            return;
        }
        // 已在队伍中
        if (team.isMember(applier.getRoleId())) {
            PlayerUtil.send(applier.getRoleId(), new ClientText("您已在队伍中"));
            return;
        }
        // 已满员
        if (team.isFull()) {
            PlayerUtil.send(applier.getRoleId(), new ClientText("playerteam_tiptext_memberfull"));
            // send to client,
            ClientBaseTeamApply packet = new ClientBaseTeamApply(ClientBaseTeamApply.TEAM_MEMBER_MAX);
            PlayerUtil.send(applier.getRoleId(), packet);
            return;
        }
        if (team.isOpenApply()) {
            putNewMemberToTeam(team, applier, "playerteam_applyteam_leaderaggre", "playerteam_applyteam_otherentered");
        } else {
            team.addApplyMember(applier);
            // send to client,
//            ClientBaseTeamApply packet = new ClientBaseTeamApply(ClientBaseTeamApply.TEAM_NOT_EXIT);
//            packet.setApplyMember(applier);
//            sendPacketToTeamMembers(team, packet, -1);
            PlayerUtil.send(applier.getRoleId(), new ClientText("playerteam_tiptext_applysend"));

            //通知队长有人申请入队
            BaseTeamEvent teamEvent = new BaseTeamEvent(BaseTeamEvent.APPLY_JOIN_TEAM);
            teamEvent.setApplierId(applier.getRoleId());
            ServiceHelper.roleService().notice(team.getCaptainId(), new RoleNotification(teamEvent));
        }
    }

    @Override
    public void permitApply(long initiator, long target) {
        // 目标已有队伍
        if (role2Team.containsKey(target)) {
            PlayerUtil.send(initiator, new ClientText("playerteam_applyteam_haveteam"));
            return;
        }
        // 操作人没有队伍
        if (!role2Team.containsKey(initiator)) {
            return;
        }
        BaseTeam team = teamMap.get(role2Team.get(initiator));
        if (team == null) {
            return;
        }
        // 不是队长
        if (team.getCaptainId() != initiator) {
            PlayerUtil.send(initiator, new ClientText("playerteam_tipsext_waitleader"));
            return;
        }
        // 目标不在申请列表中
        if (!team.isApplyMember(target)) {
            PlayerUtil.send(initiator, new ClientText("对方不在申请列表中"));
            return;
        }
        // 已满员
        if (team.isFull()) {
            PlayerUtil.send(initiator, new ClientText("playerteam_applyteam_nummax"));
            return;
        }
        // 战斗中
        if (team.isFight()) {
            return;
        }
        // 判断目标当前状态
        TeamHandler teamHandler = BaseTeamManager.getHandler(team.getTeamType());
        if (!teamHandler.otherJoinInTeam(target, team.getTarget())) {
            PlayerUtil.send(initiator, new ClientText("对方不在活动中"));
            return;
        }
        team.getInvitedMembers().remove(target);
        BaseTeamMember teamMember = team.removeApplyMember(target);
        putNewMemberToTeam(team, teamMember, "playerteam_applyteam_leaderaggre", "playerteam_applyteam_otherentered");
        // send to client,
        ClientBaseTeamApply packet = new ClientBaseTeamApply(ClientBaseTeamApply.REMOVE_APPLY);
        packet.setRemoveApplierId(target);
        sendPacketToTeamMembers(team, packet, target);
    }

    @Override
    public void refuseApply(long initiator, long target) {
        // 操作人没有队伍
        if (!role2Team.containsKey(initiator)) {
            return;
        }
        BaseTeam team = teamMap.get(role2Team.get(initiator));
        if (team == null) {
            return;
        }
        // 不是队长
        if (team.getCaptainId() != initiator) {
            PlayerUtil.send(initiator, new ClientText("playerteam_tipsext_waitleader"));
            return;
        }
        // 目标不在申请列表中
        if (!team.isApplyMember(target)) {
            PlayerUtil.send(initiator, new ClientText("对方不在申请列表中"));
            return;
        }
        team.getInvitedMembers().remove(target);
        BaseTeamMember teamMember = team.removeApplyMember(target);
        PlayerUtil.send(target, new ClientText("playerteam_applyteam_refuse", team.getCaptain().getName()));
        PlayerUtil.send(initiator, new ClientText("playerteam_applyteam_refusesuc", teamMember.getName()));
        // send to client,
        ClientBaseTeamApply packet = new ClientBaseTeamApply(ClientBaseTeamApply.REMOVE_APPLY);
        packet.setRemoveApplierId(target);
        sendPacketToTeamMembers(team, packet, -1);
    }

    @Override
    public void reqClearApplyList(long initiator) {
        // 操作人没有队伍
        if (!role2Team.containsKey(initiator)) {
            return;
        }
        BaseTeam team = teamMap.get(role2Team.get(initiator));
        if (team == null) {
            return;
        }
        // 不是队长
        if (team.getCaptainId() != initiator) {
            PlayerUtil.send(initiator, new ClientText("playerteam_tiptext_leaderonly"));
            return;
        }
        for (long aim : team.getApplyMembers().keySet()) {
            PlayerUtil.send(aim, new ClientText("playerteam_applyteam_refuse", team.getCaptain().getName()));
        }
        int size = team.clearAllApplyMember();
        ClientBaseTeamApply packet = new ClientBaseTeamApply(ClientBaseTeamApply.REMOVE_ALL_APPLY);
        PlayerUtil.send(initiator, packet);
        PlayerUtil.send(initiator, new ClientText("playerteam_apply_clearall", String.valueOf(size)));
    }

    @Override
    public void joinTeam(int teamId, BaseTeamMember teamMember) {
        if (role2Team.containsKey(teamMember.getRoleId())) {
            PlayerUtil.send(teamMember.getRoleId(), new ClientText("playerteam_selectotherteam"));
            return;
        }
        BaseTeam team = teamMap.get(teamId);
        if (team == null) {
            PlayerUtil.send(teamMember.getRoleId(), new ClientText("该队伍不存在"));
            return;
        }
        if (team.isFight()) {
            PlayerUtil.send(teamMember.getRoleId(), new ClientText("playerteam_teaminfo_fighting"));
            return;
        }
        if (team.isFull()) {
            PlayerUtil.send(teamMember.getRoleId(), new ClientText("playerteam_teaminfo_maxnum"));
            return;
        }
        putNewMemberToTeam(team, teamMember, "playerteam_applyteam_addsuc", "playerteam_applyteam_otherentered");
    }

    @Override
    public void leaveTeam(long initiator) {
        // 没有队伍
        if (!role2Team.containsKey(initiator)) {
            return;
        }
        int teamId = role2Team.remove(initiator);
        BaseTeam team = teamMap.get(teamId);
        if (team == null) {
            return;
        }
        // 不在队伍中
        if (!team.isMember(initiator)) {
            return;
        }
        // 离队成员名字
        String leaveName = team.getMember(initiator).getName();
        boolean isHeader = false;
        if (initiator == team.getCaptainId()) {
            isHeader = true;
        }
        if (team.getTeamType() == BaseTeamManager.TEAM_TYPE_ESCORT && isHeader) {
            disbandTeam(team.getTeamId());
            return;
        }
        team.removeMember(initiator);

        TeamHandler teamHandler = BaseTeamManager.getHandler(team.getTeamType());
        if (teamHandler != null) {
            teamHandler.leaveTeam(initiator);
        }

        PlayerUtil.send(initiator, new ClientText("playerteam_tiptext_leaveteam"));
        PlayerUtil.send(initiator, new ClientBaseTeamInfo(ClientBaseTeamInfo.LOST_TEAM));
        sendPacketToTeamMembers(team, new ClientText("playerteam_tiptext_memberleave", leaveName), -1);
        ClientBaseTeamInfo clientBaseTeamInfo = new ClientBaseTeamInfo(ClientBaseTeamInfo.REMOVE_TEAMMEMBER);
        List<Long> removeIds = new LinkedList<>();
        removeIds.add(initiator);
        clientBaseTeamInfo.setRemoveRoleIds(removeIds);
        sendPacketToTeamMembers(team, clientBaseTeamInfo, -1);
        ServiceHelper.chatService().delTeamMemberId(team.getTeamId(), initiator);
        // 队伍内没有活人
        ServiceHelper.teamDungeonService().removeFightScene(initiator, team);
        if (team.getPlayerMemberCount() == 0) {
            teamMap.remove(team.getTeamId());
            removeType2Team(team.getTeamType(), team.getTeamId());
            return;
        }
        // 队长退队
        if (isHeader) {
            team.autoChangeHeader();
            if(team.getTeamType()!=BaseTeamManager.TEAM_TYPE_CAMPCITYFIGHT){            	
            	matchMemeber(team.getCaptainId());
            }
            String newCaptainName = team.getCaptain().getName();
            PlayerUtil.send(team.getCaptainId(), new ClientText("playerteam_tiptext_leadergetted"));
            sendPacketToTeamMembers(team, new ClientText("playerteam_tiptext_newleader", newCaptainName), team.getCaptainId());
            ClientBaseTeamInfo packet = new ClientBaseTeamInfo(ClientBaseTeamInfo.CHANGE_CAPTAIN);
            packet.setNewCaptain(team.getCaptainId());
            sendPacketToTeamMembers(team, packet, -1);
        }
        cancelMatchTeam(initiator, team.getTeamType(), team.getTarget());
    }

    @Override
    public void kickOutTeam(long initiator, long target) {
        // 没有队伍
        if (!role2Team.containsKey(initiator)) {
            return;
        }
        BaseTeam team = teamMap.get(role2Team.get(initiator));
        if (team == null) {
            return;
        }
        // 不是队长
        if (team.getCaptainId() != initiator) {
            PlayerUtil.send(initiator, new ClientText("playerteam_tipsext_waitleader"));
            return;
        }
        if (team.getCaptainId() == target) {
            /**
             * 协议抵抗，无法踢队长
             */
            return;
        }
        // 目标不在队伍
        if (!team.isMember(target)) {
            return;
        }
        BaseTeamMember removeMember = team.removeMember(target);
        this.role2Team.remove(target);
        if (removeMember.isPlayer()) {
            ServiceHelper.chatService().delTeamMemberId(team.getTeamId(), target);
            PlayerUtil.send(target, new ClientBaseTeamInfo(ClientBaseTeamInfo.LOST_TEAM));
            PlayerUtil.send(target, new ClientText("playerteam_tiptext_deleted_receive", team.getCaptain().getName()));
        }
        PlayerUtil.send(initiator, new ClientText("playerteam_tiptext_deleted_send", removeMember.getName()));
        // send to client,
        ClientBaseTeamInfo packet = new ClientBaseTeamInfo(ClientBaseTeamInfo.REMOVE_TEAMMEMBER);
        List<Long> removeIds = new LinkedList<>();
        removeIds.add(target);
        packet.setRemoveRoleIds(removeIds);
        sendPacketToTeamMembers(team, packet, -1);
    }

    @Override
    public void changeCaptain(long initiator, long target) {
        // 没有队伍
        if (!role2Team.containsKey(initiator)) {
            return;
        }
        BaseTeam team = teamMap.get(role2Team.get(initiator));
        if (team == null) {
            return;
        }
        // 不是队长
        if (team.getCaptainId() != initiator) {
            PlayerUtil.send(initiator, new ClientText("playerteam_tipsext_waitleader"));
            return;
        }
        // 目标不在队伍
        if (!team.isMember(target)) {
            return;
        }
        // 目标不是真实玩家
        if (!team.isPlayer(target)) {
            team.autoChangeHeader();
        } else {
            team.setCaptainId(target);
        }
        // send to client,
        target = team.getCaptainId();
        matchMemeber(target);
        ClientBaseTeamInfo packet = new ClientBaseTeamInfo(ClientBaseTeamInfo.CHANGE_CAPTAIN);
        packet.setNewCaptain(target);
        PlayerUtil.send(team.getCaptainId(), new ClientText("playerteam_tiptext_leadergetted"));
        sendPacketToTeamMembers(team, new ClientText("playerteam_tiptext_newleader", team.getCaptain().getName()),
                team.getCaptainId());
        sendPacketToTeamMembers(team, packet, -1);
    }

    @Override
    public void changeTeamTarget(long initiator, int target) {
        // 没有队伍
        if (!role2Team.containsKey(initiator)) {
            return;
        }
        BaseTeam team = teamMap.get(role2Team.get(initiator));
        if (team == null) {
            return;
        }
        if (initiator != team.getCaptainId()) {
            PlayerUtil.send(initiator, new ClientText("不是队长"));
            return;
        }
        team.setTarget(target);

        //通知队员更换目标了
        BaseTeamEvent teamEvent = new BaseTeamEvent(BaseTeamEvent.TEAM_TARGET_CHANGED);
        teamEvent.setTeamTarget(target);
        teamEvent.setTeamType(team.getTeamType());
        sendEventToMembers(team, teamEvent, -1);

        //通知受邀请者删除邀请信息
        for (Long roleId : team.getInvitedMembers()) {
            BaseTeamEvent deleteInviteEvent = new BaseTeamEvent(BaseTeamEvent.DELETE_INVITE);
            deleteInviteEvent.setInvitorId(initiator);
            ServiceHelper.roleService().notice(roleId, deleteInviteEvent);
        }
    }

    @Override
    public void setAutoApplyFlag(long initiator, byte flag) {
        // 没有队伍
        if (!role2Team.containsKey(initiator)) {
            return;
        }
        BaseTeam team = teamMap.get(role2Team.get(initiator));
        if (team == null) {
            return;
        }
        if (initiator != team.getCaptainId()) {
            PlayerUtil.send(initiator, new ClientText("不是队长"));
            return;
        }
        team.setOpenApply(flag == 1);
        ClientBaseTeamInfo packet = new ClientBaseTeamInfo(ClientBaseTeamInfo.SET_OPEN_APPLY);
        packet.setOpenApply(flag);
        sendPacketToTeamMembers(team, packet, -1);
    }

    @Override
    public void disbandTeamByTeamtype(byte teamType) {
        if (!type2Team.containsKey(teamType)) {
            return;
        }
        List<Integer> teamIdList = new LinkedList<>(type2Team.get(teamType));
        for (int teamId : teamIdList) {
            disbandTeam(teamId);
        }
    }

    /**
     * 解散队伍
     */
    @Override
    public void disbandTeam(int teamId) {
        if (!teamMap.containsKey(teamId)) {
            return;
        }
        BaseTeam team = teamMap.get(teamId);
        for (long roleId : team.getPlayerMembers().keySet()) {
            role2Team.remove(roleId);
        }
        teamMap.remove(teamId);
        removeType2Team(team.getTeamType(), teamId);
        // 通知客户端
        sendPacketToTeamMembers(team, new ClientBaseTeamInfo(ClientBaseTeamInfo.LOST_TEAM), -1);
        ServiceHelper.chatService().delTeam(teamId);
    }

    /**
     * 解散队伍
     */
    @Override
    public void disbandTeam(long leaderId) {
        if (!role2Team.containsKey(leaderId)) return;
        BaseTeam team = teamMap.get(role2Team.get(leaderId));
        if (team == null) {
            role2Team.remove(leaderId);
            return;
        }
        if (team.getCaptainId() != leaderId) return;//不是队长

        for (long roleId : team.getPlayerMembers().keySet()) {
            role2Team.remove(roleId);
        }
        teamMap.remove(team.getTeamId());
        removeType2Team(team.getTeamType(), team.getTeamId());
        // 通知客户端
        sendPacketToTeamMembers(team, new ClientBaseTeamInfo(ClientBaseTeamInfo.LOST_TEAM), -1);
    }

    private int newTeamId() {
        return ++idCreator;
    }

    private void putNewMemberToTeam(BaseTeam team, BaseTeamMember teamMember, String... notice) {
        team.addUpdateMember(teamMember);
        if (teamMember.getType() == 0) {
            role2Team.put(teamMember.getRoleId(), team.getTeamId());
            // 通知已加入队伍
            BaseTeamEvent teamEvent = new BaseTeamEvent(BaseTeamEvent.JOIN_TEAM);
            teamEvent.setTeamType(team.getTeamType());
            ServiceHelper.roleService().notice(teamMember.getRoleId(), teamEvent);
            // 清除所有邀请列表
            ClientBaseTeamInvite clientBaseTeamInvite = new ClientBaseTeamInvite(ClientBaseTeamInvite.REMOVE_ALL_INVITE);
            PlayerUtil.send(teamMember.getRoleId(), clientBaseTeamInvite);
            // 发给新加入的
            ClientBaseTeamInfo clientBaseTeamInfo = new ClientBaseTeamInfo(ClientBaseTeamInfo.TEAMINFO);
            clientBaseTeamInfo.setTeam(team);
            PlayerUtil.send(teamMember.getRoleId(), clientBaseTeamInfo);
            PlayerUtil.send(teamMember.getRoleId(), new ClientText(notice[0], team.getCaptain().getName()));
            // 通知队长取消匹配队员
            if (team.isFull()) {
                teamEvent = new BaseTeamEvent(BaseTeamEvent.CANCEL_MATCH_MEMBER);
                teamEvent.setNotice("playerteam_tiptext_fullnum");
                ServiceHelper.roleService().notice(team.getCaptainId(), teamEvent);
            }

            handleAfterJoinTeam(team.getTeamType(), teamMember.getRoleId());
            ServiceHelper.chatService().addTeamMemberId(team.getTeamId(), teamMember.getRoleId());
        }
        // 发给其他队员
        ClientBaseTeamInfo clientBaseTeamInfo = new ClientBaseTeamInfo(ClientBaseTeamInfo.ADD_UPDATE_TEAMMEMBER);
        clientBaseTeamInfo.addMember(teamMember);
        sendPacketToTeamMembers(team, clientBaseTeamInfo, teamMember.getRoleId());
        sendPacketToTeamMembers(team, new ClientText(notice[1], teamMember.getName()), teamMember.getRoleId());
    }

    private void handleAfterJoinTeam(byte teamType, long roleId) {
        if (teamType == BaseTeamManager.TEAM_TYPE_CARGO_ROB) {
            ServiceHelper.escortService().removeFromRobPermitSet(roleId);
        } else if (teamType == BaseTeamManager.TEAM_TYPE_ESCORT) {
            ServiceHelper.escortService().removeFromEscortPermitSet(roleId);
        } else if (teamType == BaseTeamManager.TEAM_TYPE_MARRY) {
            ServiceHelper.marryService().SyncMarryOther(roleId);
        }
    }


    /**
     * 给队伍的成员发送数据包
     * 只发真实玩家
     *
     * @param team      队伍
     * @param packet    数据包
     * @param exception 不需要发的队员
     */
    private void sendPacketToTeamMembers(BaseTeam team, Packet packet, long exception) {
        if (team == null) {
            return;
        }
        for (BaseTeamMember teamMember : team.getMembers().values()) {
            if (teamMember.getRoleId() == exception) {
                continue;
            }
            if (!teamMember.isPlayer()) {
                continue;
            }
            PlayerUtil.send(teamMember.getRoleId(), packet);
        }
    }

    /**
     * 给队伍成员通知事件
     * 只发真实玩家
     *
     * @param team
     * @param event
     * @param exception
     */
    private void sendEventToMembers(BaseTeam team, Event event, long exception) {
        if (team == null)
            return;
        for (BaseTeamMember teamMember : team.getMembers().values()) {
            if (teamMember.getRoleId() == exception) {
                continue;
            }
            if (!teamMember.isPlayer()) {
                continue;
            }
            ServiceHelper.roleService().notice(teamMember.getRoleId(), event);
        }
    }

    private void putType2Team(byte teamType, int teamId) {
        if (!type2Team.containsKey(teamType)) {
            type2Team.put(teamType, new LinkedList<Integer>());
        }
        type2Team.get(teamType).add(teamId);
    }

    private void removeType2Team(byte teamType, int teamId) {
        if (!type2Team.containsKey(teamType))
            return;
        type2Team.get(teamType).remove(Integer.valueOf(teamId));
    }
}
