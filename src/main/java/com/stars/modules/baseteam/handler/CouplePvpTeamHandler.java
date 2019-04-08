package com.stars.modules.baseteam.handler;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.arroundPlayer.ArroundPlayer;
import com.stars.modules.baseteam.packet.ClientBaseTeamInvite;
import com.stars.modules.baseteam.userdata.TeamInvitee;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.teamdungeon.TeamDungeonManager;
import com.stars.modules.teampvpgame.TeamPVPGameManager;
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
 * Created by liuyuheng on 2016/12/15.
 */
public class CouplePvpTeamHandler implements TeamHandler {
    @Override
    public void createTeam(Map<String, Module> moduleMap, BaseTeamMember creator, byte teamType, int target) {
        creator.removeBuddyEntity();
        ServiceHelper.baseTeamService().createTeamSync(creator, teamType, TeamPVPGameManager.minMemberCount,
                TeamPVPGameManager.maxMemberCount, target);
    }

    @Override
    public void sendCanInviteList(Map<String, Module> moduleMap, long initiator) {
        // 能否报名
        if (!ServiceHelper.tpgLocalService().canSignUp(initiator)) {
            return;
        }
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(initiator);
        if (team == null) {
            PlayerUtil.send(initiator, new ClientText("team_noTeam"));
            return;
        }
        if (team.isFull()) {
            PlayerUtil.send(initiator, new ClientText("team_full"));
            return;
        }
        RoleModule rm = (RoleModule) moduleMap.get(MConst.Role);
        int[] levelLimit = new int[]{Math.max(0, rm.getLevel() - TeamPVPGameManager.levelExcursion),
                rm.getLevel() + TeamPVPGameManager.levelExcursion};
        sendArroundPlayerList(initiator, rm.getJoinSceneStr(), levelLimit);
        sendFriendFamilyList(initiator, levelLimit);
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
    public boolean selfJoinInTeam(Map<String, Module> moduleMap, long initiator, int teamId, BaseTeam team) {
        // 能否报名
        if (!ServiceHelper.tpgLocalService().canSignUp(initiator)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean otherJoinInTeam(long target, int teamTarget) {
        // 能否报名
        if (!ServiceHelper.tpgLocalService().canSignUp(target)) {
            return false;
        }
        return true;
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

    }

    @Override
    public void handleCloseTeamUI(long roleId) {

    }

    private void sendArroundPlayerList(long initiator, String scene, int[] levelLimit) {
        ClientBaseTeamInvite cti = new ClientBaseTeamInvite(ClientBaseTeamInvite.CAN_INVITE_LIST);
        cti.setInviteeType((byte) 0);
        Map<Long, ArroundPlayer> players = ServiceHelper.arroundPlayerService().getArroundPlayersBySceneId(scene);
        List<ArroundPlayer> ls = new ArrayList<>(players.values());
        if (ls != null) {
            TeamInvitee tInvitee;
            for (ArroundPlayer arroundPlayer : ls) {
                if (arroundPlayer.getRoleId() == initiator ||
                        arroundPlayer.getLevel() < levelLimit[0] || arroundPlayer.getLevel() > levelLimit[1]) {
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
                RoleSummaryComponent rsc = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                        arroundPlayer.getRoleId(), "role");
                tInvitee.setFightScore(rsc.getFightScore());
                cti.addInvitee(tInvitee);
            }
        }
        PlayerUtil.send(initiator, cti);
    }

    private void sendFriendFamilyList(long initiator, int[] levelLimit) {
        // friend
        List<Long> friendIdList = ServiceHelper.friendService().getFriendList(initiator); // 拿好友列表
        List<Summary> onlineRoleList = ServiceHelper.summaryService().getAllOnlineSummary(friendIdList); // 获取

        ClientBaseTeamInvite packet = new ClientBaseTeamInvite(ClientBaseTeamInvite.CAN_INVITE_LIST);
        packet.setInviteeType(TeamDungeonManager.INVOTEE_TYPE_FRIEND);
        for (Summary summary : onlineRoleList) {
            RoleSummaryComponent comp = (RoleSummaryComponent) summary.getComponent(SummaryConst.C_ROLE);
            if (comp.getRoleLevel() < levelLimit[0] || comp.getRoleLevel() > levelLimit[1]
                    || ServiceHelper.baseTeamService().hasTeam(summary.getRoleId())
                    || summary.getRoleId() == initiator) {
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
        PlayerUtil.send(initiator, packet);
        // family
        long familyId = ServiceHelper.familyRoleService().getFamilyId(initiator); // 拿家族id
        List<Long> familyMemberIdList = ServiceHelper.familyMainService().getMemberIdList(familyId, initiator); // 拿家族成员列表
        onlineRoleList = ServiceHelper.summaryService().getAllOnlineSummary(familyMemberIdList); // 获取
        packet = new ClientBaseTeamInvite(ClientBaseTeamInvite.CAN_INVITE_LIST);
        packet.setInviteeType(TeamDungeonManager.INVOTEE_TYPE_FAMILY);
        for (Summary summary : onlineRoleList) {
            RoleSummaryComponent comp = (RoleSummaryComponent) summary.getComponent(SummaryConst.C_ROLE);
            if (comp.getRoleLevel() < levelLimit[0] || comp.getRoleLevel() > levelLimit[1]
                    || ServiceHelper.baseTeamService().hasTeam(summary.getRoleId())
                    || summary.getRoleId() == initiator) {
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
        PlayerUtil.send(initiator, packet);
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
