package com.stars.modules.baseteam.handler;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.arroundPlayer.ArroundPlayer;
import com.stars.modules.baseteam.packet.ClientBaseTeamInvite;
import com.stars.modules.baseteam.userdata.TeamInvitee;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.escort.EscortManager;
import com.stars.modules.escort.EscortModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMatch;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/6.
 */
public class CargoRobTeamHandler implements TeamHandler  {
    @Override
    public void createTeam(Map<String, Module> moduleMap, BaseTeamMember creator, byte teamType, int target) {
        if(creator==null) return;
        String checkResult = checkRobCondition(moduleMap);
        if(checkResult!=null){
            if(!checkResult.equals("")){
                PlayerUtil.send(creator.getRoleId(), new ClientText(checkResult));
            }
            return;
        }
        ServiceHelper.baseTeamService().createTeam(creator, teamType, (byte)1, (byte) EscortManager.getMemberCount(0), target);
    }

    /**
     *  组队劫镖条件检测
     */
    private String checkRobCondition(Map<String, Module> moduleMap){
        RoleModule rm = (RoleModule) moduleMap.get(MConst.Role);
        if(rm == null || rm.getRoleRow() == null) return "";
        int levelLimit = EscortManager.getTeamModeLevel();
        if(rm.getLevel() < levelLimit) {
            return "team_levelLimit";
        }
        EscortModule escortModule = (EscortModule) moduleMap.get(MConst.Escort);
        if(escortModule == null || escortModule.getRoleEscort() == null) return "";
        if(escortModule.getRoleEscort().getRemainRobTime() <= 0){
            return "team_dailyTimeNotEnough";
        }
        return null;
    }

    /**
     * 组队目标是否为运镖类型
     */
    private boolean isEscortCargoType(int target){
        return false;
    }

    /**
     * 组队目标是否为劫镖类型
     */
    private boolean isRobCargoType(int target){
        return false;
    }

    @Override
    /**
     * 发送可邀请列表
     */
    public void sendCanInviteList(Map<String, Module> moduleMap, long roleId) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(roleId);
        if (team == null) {
            PlayerUtil.send(roleId, new ClientText("team_noTeam"));
            return;
        }
        if (team.isFull()) {
            PlayerUtil.send(roleId, new ClientText("team_full"));
            return;
        }
        RoleModule rm = (RoleModule) moduleMap.get(MConst.Role);
        sendCanInviteList(roleId, team.getTarget(), rm.getJoinSceneStr());
    }

    @Override
    public boolean canChangeTeamTarget(Map<String, Module> moduleMap, long initiator, int target) {
        return false;
    }
    
    @Override
    public boolean canAllChangeTeamTarget(Map<String, Module> moduleMap, long initiator, int target) {
        return true;
    }

    @Override
    public void changeTeamTarget(Map<String, Module> moduleMap, long initiator, int newTeamTarget) {

    }

    @Override
    public boolean selfJoinInTeam(Map<String, Module> moduleMap, long roleId, int teamTarget, BaseTeam team) {
        String checkResult = checkRobCondition(moduleMap);
        if(checkResult!=null){
            if(!checkResult.equals("")){
                PlayerUtil.send(roleId,new ClientText(checkResult));
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean otherJoinInTeam(long target, int teamTarget) {
        return ServiceHelper.escortService().hasJoinRobTeamPermit(target);
    }

    @Override
    public boolean canBeInvite(long invitorId , long inviteedId, int teamTarget){
        return true;
    }
    
    @Override
    public List<BaseTeamMember> matchFakePlayer(BaseTeamMember captain, int count, List<Long> exception, BaseTeamMatch match, BaseTeam team) {
        return null;
    }

    @Override
    public boolean matchTeamWithFakePlayer(BaseTeamMember creator, byte teamType, int target) {
        return false;
    }

    @Override
    public void afterJoinTeam(Map<String, Module> moduleMap, long roleId) {

    }

    @Override
    public void handleOpenTeamUI(long roleId,Map<String, Module> moduleMap) {
        String conditionCheck = checkRobCondition(moduleMap);
        if(conditionCheck!=null) return;//不满足条件

        ServiceHelper.escortService().addToRobPermitSet(roleId);
    }

    @Override
    public void handleCloseTeamUI(long roleId) {
        ServiceHelper.escortService().removeFromRobPermitSet(roleId);
    }

    /**
     * 下发可邀请列表
     */
    private void sendCanInviteList(long roleId, int target, String scene) {
        reqArroundPlayerCanInviteList(roleId, scene);
        sendCandidateInviteeList(roleId);
    }

    /**
     * 可邀请列表-周围玩家
     */
    private void reqArroundPlayerCanInviteList(long roleId, String scene) {
        int levelLimit = EscortManager.getTeamModeLevel();
        ClientBaseTeamInvite client = new ClientBaseTeamInvite(ClientBaseTeamInvite.CAN_INVITE_LIST);
        client.setInviteeType((byte) 0);
        Map<Long, ArroundPlayer> players = ServiceHelper.arroundPlayerService().getArroundPlayersBySceneId(scene);
        List<ArroundPlayer> arroundPlayers = new ArrayList<>(players.values());
        if (arroundPlayers != null) {
            TeamInvitee tInvitee;
            for (ArroundPlayer arroundPlayer : arroundPlayers) {
                if (arroundPlayer.getRoleId() == roleId || arroundPlayer.getLevel() < levelLimit) {
                    continue;
                }
                if (ServiceHelper.baseTeamService().hasTeam(arroundPlayer.getRoleId())) {
                    continue;
                }
                tInvitee = new TeamInvitee();
                tInvitee.setId(arroundPlayer.getRoleId());
                tInvitee.setName(arroundPlayer.getName());
                tInvitee.setLevel(arroundPlayer.getLevel());
                tInvitee.setJob((byte) arroundPlayer.getJob());
                RoleSummaryComponent rsc = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(arroundPlayer.getRoleId(), "role");
                tInvitee.setFightScore(rsc.getFightScore());
                client.addInvitee(tInvitee);
            }
        }
        PlayerUtil.send(roleId, client);
    }

    /**
     * 可邀请列表-好友&家族
     */
    private void sendCandidateInviteeList(long roleId) {
        int levelLimit = EscortManager.getTeamModeLevel();

        //在线可邀请好友列表
        List<Long> friendIdList = ServiceHelper.friendService().getFriendList(roleId); // 拿好友列表
        List<Summary> onlineRoleList = ServiceHelper.summaryService().getAllOnlineSummary(friendIdList); // 获取
        ClientBaseTeamInvite packet = new ClientBaseTeamInvite(ClientBaseTeamInvite.CAN_INVITE_LIST);
        packet.setInviteeType((byte) 1);
        for (Summary summary : onlineRoleList) {
            RoleSummaryComponent comp = (RoleSummaryComponent) summary.getComponent(SummaryConst.C_ROLE);
            if (comp.getRoleLevel() < levelLimit
                    || ServiceHelper.baseTeamService().hasTeam(summary.getRoleId())
                    || summary.getRoleId() == roleId) {
                continue;
            }
            TeamInvitee invitee = new TeamInvitee();
            invitee.setId(summary.getRoleId());
            invitee.setName(comp.getRoleName());
            invitee.setJob((byte) comp.getRoleJob());
            invitee.setLevel((short) comp.getRoleLevel());
            invitee.setFightScore(comp.getFightScore());
            packet.addInvitee(invitee);
        }
        PlayerUtil.send(roleId, packet);

        // 在线可邀请家族列表
        long familyId = ServiceHelper.familyRoleService().getFamilyId(roleId); // 拿家族id
        List<Long> familyMemberIdList = ServiceHelper.familyMainService().getMemberIdList(familyId, roleId); // 拿家族成员列表
        onlineRoleList = ServiceHelper.summaryService().getAllOnlineSummary(familyMemberIdList); // 获取
        packet = new ClientBaseTeamInvite(ClientBaseTeamInvite.CAN_INVITE_LIST);
        packet.setInviteeType((byte) 2);
        for (Summary summary : onlineRoleList) {
            RoleSummaryComponent comp = (RoleSummaryComponent) summary.getComponent(SummaryConst.C_ROLE);
            if (comp.getRoleLevel() < levelLimit
                    || ServiceHelper.baseTeamService().hasTeam(summary.getRoleId())
                    || summary.getRoleId() == roleId) {
                continue;
            }
            TeamInvitee invitee = new TeamInvitee();
            invitee.setId(summary.getRoleId());
            invitee.setName(comp.getRoleName());
            invitee.setJob((byte) comp.getRoleJob());
            invitee.setLevel((short) comp.getRoleLevel());
            invitee.setFightScore(comp.getFightScore());
            packet.addInvitee(invitee);
        }
        PlayerUtil.send(roleId, packet);
    }

	@Override
	public void leaveTeam(long roleId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newMatchHandle(BaseTeamMatch match, BaseTeam team) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCanMatch(BaseTeamMatch match) {
		return true;
	}

	@Override
	public BaseTeamMember createBaseTeamMember(Map<String, Module> moduleMap) {
		return null;
	}
}
