package com.stars.multiserver.camp;

import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.arroundPlayer.ArroundPlayer;
import com.stars.modules.baseteam.packet.ClientBaseTeamInvite;
import com.stars.modules.baseteam.userdata.TeamInvitee;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.scene.imp.fight.CampCityFightScene;
import com.stars.modules.teamdungeon.TeamDungeonManager;
import com.stars.network.server.packet.Packet;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;

public class CampCityFightServiceActor extends ServiceActor implements CampCityFightService{
	
	private static Map<Integer, CampCityFightScene> fightSceneMap = new HashMap<>();

	@Override
	public void init() throws Throwable {
		ServiceSystem.getOrAdd(SConst.CampCityFightService, this);
	}

	@Override
	public void printState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addToSceneMap(int teamId, CampCityFightScene scene) {
		fightSceneMap.put(teamId, scene);
	}

	@Override
	public void removeFightScene(int teamId) {
		fightSceneMap.remove(teamId);
	}

	@Override
	public void removeMember(long roleId) {
		Iterator<CampCityFightScene> iterator = fightSceneMap.values().iterator();
		CampCityFightScene scene = null;
		for(;iterator.hasNext();){
			scene = iterator.next();
			scene.exit(roleId, false);
			if(scene.hasNoPlayer()){
				iterator.remove();
			}
		}
	}
	
	/**
     * 副本开始战斗
     *
     * @param roleId
     */
    @Override
    public void startFightTime(long roleId) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(roleId);
        if (team == null) {
            LogUtil.info("CampCityFightServiceActor.startFightTime get no team,roleId=" + roleId);
            return;
        }
        CampCityFightScene scene = (CampCityFightScene) fightSceneMap.get(team.getTeamId());
        if (scene == null) {
            LogUtil.info("CampCityFightServiceActor.deadInDungeon campCityFightScene is null");
            return;
        }

        Packet packet = scene.startFightTime();
        if (packet != null) {
            PlayerUtil.send(roleId, packet);
        }
    }

	@Override
	public void receiveFightPacket(PlayerPacket packet) {
		BaseTeam team = ServiceHelper.baseTeamService().getTeam(packet.getRoleId());
        if (team == null) {
            return;
        }
        CampCityFightScene scene = (CampCityFightScene) fightSceneMap.get(team.getTeamId());
        if (scene == null) {
            return;
        }
        scene.receivePacket(null, packet);
	}
	
	/**
     * 发送可邀请好友信息
     * @param initiator
     * @param campType
     * @param scene
     */
	@Override
	public void sendCanInviteList(long initiator, int campType, String scene) {
		reqArroundPlayerCanInviteList(initiator, scene, campType);
		sendCandidateInviteeList(initiator, scene, campType);
	}
	
	/**
     * 可邀请列表-周围玩家
     *
     * @param initiator
     * @param scene
     * @param
     */
    private void reqArroundPlayerCanInviteList(long initiator, String scene, int campType) {
        ClientBaseTeamInvite cti = new ClientBaseTeamInvite(ClientBaseTeamInvite.CAN_INVITE_LIST);
        cti.setInviteeType((byte) 0);
        Map<Long, ArroundPlayer> players = ServiceHelper.arroundPlayerService().getArroundPlayersBySceneId(scene);
        List<ArroundPlayer> ls = new ArrayList<>(players.values());
        if (ls != null) {
            TeamInvitee tInvitee;
            for (ArroundPlayer arroundPlayer : ls) {
                if (arroundPlayer.getRoleId() == initiator) {
                    continue;
                }
                if (ServiceHelper.baseTeamService().hasTeam(arroundPlayer.getRoleId())) {
                    continue;
                }
                RoleSummaryComponent rsc = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                		arroundPlayer.getRoleId(), "role");
                if(campType!=rsc.getCampType()){
                	continue;
                }
                tInvitee = new TeamInvitee();
                tInvitee.setId(arroundPlayer.getRoleId());
                tInvitee.setName(arroundPlayer.getName());
                tInvitee.setLevel(arroundPlayer.getLevel());
                tInvitee.setJob((byte) arroundPlayer.getJob());
                tInvitee.setFightScore(rsc.getFightScore());
                cti.addInvitee(tInvitee);
            }
        }
        PlayerUtil.send(initiator, cti);
    }
    
    /**
     * 可邀请列表-好友&家族
     *
     * @param initiator
     * @param scene
     * @param
     */
    private void sendCandidateInviteeList(long initiator, String scene, int campType) {
        //Map<Long, ArroundPlayer> players = ServiceHelper.arroundPlayerService().getArroundPlayersBySceneId(scene);
        // friend
        List<Long> friendIdList = ServiceHelper.friendService().getFriendList(initiator); // 拿好友列表
        List<Summary> onlineRoleList = ServiceHelper.summaryService().getAllOnlineSummary(friendIdList); // 获取

        ClientBaseTeamInvite packet = new ClientBaseTeamInvite(ClientBaseTeamInvite.CAN_INVITE_LIST);
        packet.setInviteeType(TeamDungeonManager.INVOTEE_TYPE_FRIEND);
        for (Summary summary : onlineRoleList) {
            RoleSummaryComponent comp = (RoleSummaryComponent) summary.getComponent(SummaryConst.C_ROLE);
            if (ServiceHelper.baseTeamService().hasTeam(summary.getRoleId())
                    || summary.getRoleId() == initiator) {
                continue;
            }
            if(campType!=comp.getCampType()){
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
            if (ServiceHelper.baseTeamService().hasTeam(summary.getRoleId())
                    || summary.getRoleId() == initiator) {
                continue;
            }
            if(campType!=comp.getCampType()){
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

}
