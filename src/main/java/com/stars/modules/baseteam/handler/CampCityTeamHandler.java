package com.stars.modules.baseteam.handler;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.CampTeamMember;
import com.stars.modules.camp.activity.CampActivity;
import com.stars.modules.camp.activity.imp.QiChuZhiZhengActivity;
import com.stars.modules.camp.usrdata.RoleCampPo;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.role.RoleModule;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMatch;
import com.stars.services.baseteam.BaseTeamMember;

import java.util.List;
import java.util.Map;

public class CampCityTeamHandler implements TeamHandler{

	@Override
	public void createTeam(Map<String, Module> moduleMap, BaseTeamMember creator, byte teamType, int target) {
		CampModule campModule = (CampModule)moduleMap.get(MConst.Camp);
		RoleCampPo roleCampPo = campModule.getRoleCamp();
		if(roleCampPo==null) return;
		ServiceHelper.baseTeamService().createTeam(creator, teamType, CampManager.minMemberCount, CampManager.maxMemberCount, target);
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
        RoleModule rm = (RoleModule) moduleMap.get(MConst.Role);
        CampModule campModule = (CampModule)moduleMap.get(MConst.Camp);
        ServiceHelper.campCityFightService().sendCanInviteList(initiator, campModule.getCampType(), rm.getJoinSceneStr());
	}

	@Override
	public boolean canChangeTeamTarget(Map<String, Module> moduleMap, long initiator, int target) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean canAllChangeTeamTarget(Map<String, Module> moduleMap, long initiator, int target) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void changeTeamTarget(Map<String, Module> moduleMap, long initiator, int newTeamTarget) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean selfJoinInTeam(Map<String, Module> moduleMap, long initiator, int targetId, BaseTeam team) {
		CampModule campModule = (CampModule)moduleMap.get(MConst.Camp);
		RoleCampPo roleCamp = campModule.getRoleCamp();
		if(roleCamp==null){
			PlayerUtil.send(initiator, new ClientText("请先加入阵营"));
			return false;
		}
		int campType = campModule.getCampType();
		if(team!=null){			
			CampTeamMember captain = (CampTeamMember)team.getCaptain();
			if(campType!=captain.getRoleCampPo().getCampType()){
				PlayerUtil.send(initiator, new ClientText(DataManager.getGametext("camp_tips_activityonesay")));
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean otherJoinInTeam(long target, int teamTarget) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean canBeInvite(long invitorId, long inviteedId, int teamTarget) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCanMatch(BaseTeamMatch match) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void newMatchHandle(BaseTeamMatch match, BaseTeam team) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<BaseTeamMember> matchFakePlayer(BaseTeamMember captain, int count, List<Long> exception,
			BaseTeamMatch match, BaseTeam team) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchTeamWithFakePlayer(BaseTeamMember creator, byte teamType, int target) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void afterJoinTeam(Map<String, Module> moduleMap, long roleId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleOpenTeamUI(long roleId, Map<String, Module> moduleMap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleCloseTeamUI(long roleId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void leaveTeam(long roleId) {
		
	}

	@Override
	public BaseTeamMember createBaseTeamMember(Map<String, Module> moduleMap) {
		CampModule campModule = (CampModule)moduleMap.get(MConst.Camp);
		QiChuZhiZhengActivity activity= (QiChuZhiZhengActivity) campModule.getCampActivityById(CampActivity.ACTIVITY_ID_QI_CHU_ZHI_ZHENG);
		BaseTeamMember tm = activity.createCampTeamMember();
		return tm;
	}

}
