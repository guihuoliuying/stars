package com.stars.modules.baseteam.handler;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.baseteam.packet.ClientBaseTeamInvite;
import com.stars.modules.baseteam.userdata.TeamInvitee;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.family.summary.FamilySummaryComponent;
import com.stars.modules.familyactivities.bonfire.FamilyBonfireModule;
import com.stars.modules.familyactivities.invade.FamilyInvadeManager;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.imp.city.FamilyScene;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMatch;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.services.family.activities.invade.FamilyActInvadeFlow;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;

import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/11/17.
 */
public class FamilyInvadeTeamHandler implements TeamHandler {
    @Override
    public void createTeam(Map<String, Module> moduleMap, BaseTeamMember creator, byte teamType, int target) {
        // 活动未开启
        if (!FamilyActInvadeFlow.isStarted())
            return;
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        // 不在家族场景
        if (!(sceneModule.getScene() instanceof FamilyScene)) {
            return;
        }
        ServiceHelper.baseTeamService().createTeam(creator, teamType, FamilyInvadeManager.minTeamCount,
                FamilyInvadeManager.maxTeamCount, target);
    }

    @Override
    public void sendCanInviteList(Map<String, Module> moduleMap, long initiator) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(initiator);
        if (team == null) {
            PlayerUtil.send(initiator, new ClientText("team_noTeam"));
            return;
        }
        if (team.isFull()) {
            PlayerUtil.send(initiator, new ClientText("playerteam_applyteam_nummax"));
            return;
        }
        // 在线家族成员
        long familyId = ServiceHelper.familyRoleService().getFamilyId(initiator); // 拿家族id
        List<Long> familyMemberIdList = ServiceHelper.familyMainService().getMemberIdList(familyId, initiator); // 拿家族成员列表
        List<Summary> onlineRoleList = ServiceHelper.summaryService().getAllOnlineSummary(familyMemberIdList); // 获取
        ClientBaseTeamInvite packet = new ClientBaseTeamInvite(ClientBaseTeamInvite.CAN_INVITE_LIST);
        packet.setInviteeType((byte) 2);
        for (Summary summary : onlineRoleList) {
            RoleSummaryComponent comp = (RoleSummaryComponent) summary.getComponent(SummaryConst.C_ROLE);
            if (ServiceHelper.baseTeamService().hasTeam(summary.getRoleId()) || summary.getRoleId() == initiator) {
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
    public boolean canChangeTeamTarget(Map<String, Module> moduleMap, long initiator, int target) {
        return false;
    }

    @Override
    public boolean canAllChangeTeamTarget(Map<String, Module> moduleMap, long initiator, int target) {
        return true;
    }
    
    @Override
    public boolean selfJoinInTeam(Map<String, Module> moduleMap, long initiator, int teamId, BaseTeam team) {
        // 活动未开启
        if (!FamilyActInvadeFlow.isStarted())
            return false;
        return true;
    }

    @Override
    public boolean otherJoinInTeam(long target, int teamTarget) {// 活动未开启
        if (!FamilyActInvadeFlow.isStarted())
            return false;
        FamilySummaryComponent fsc = (FamilySummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                target, "family");
        if (fsc == null)
            return false;
        return ServiceHelper.familyActInvadeService().isMemberIn(fsc.getFamilyId(), target);
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
        // 不在家族场景组队成功后需要进入家族场景
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        if (sceneModule.getScene() instanceof FamilyScene) {
            return;
        }
        FamilyBonfireModule familyBonfireModule = (FamilyBonfireModule) moduleMap.get(MConst.FamilyActBonfire);
        familyBonfireModule.enterBonefireScene();
    }

    @Override
    public void handleOpenTeamUI(long roleId,Map<String, Module> moduleMap) {

    }

    @Override
    public void handleCloseTeamUI(long roleId) {

    }

    @Override
    public void changeTeamTarget(Map<String, Module> moduleMap, long initiator, int newTeamTarget) {

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
