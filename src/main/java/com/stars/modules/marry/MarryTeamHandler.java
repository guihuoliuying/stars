package com.stars.modules.marry;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.baseteam.handler.TeamHandler;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.teamdungeon.TeamDungeonManager;
import com.stars.modules.teamdungeon.prodata.TeamDungeonVo;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMatch;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.services.marry.userdata.Marry;
import com.stars.util.I18n;
import com.stars.util.LogUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by zhouyaohui on 2016/12/10.
 */
public class MarryTeamHandler implements TeamHandler {

    @Override
    public void createTeam(Map<String, Module> moduleMap, BaseTeamMember creator, byte teamType, int target) {
        long roleId = moduleMap.get(MConst.Role).id();
        int dungeonCount = ServiceHelper.marryService().getRemainTeamDungeon(roleId);
        if (dungeonCount <= 0) {
            PlayerUtil.send(roleId, new ClientText("marry_btn_my_timenotenough"));
            return;
        }
        LogUtil.info("createTeam|creator:{}", creator);
        ServiceHelper.marryService().createTeam(roleId, creator, target);
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
        ServiceHelper.marryService().sendCanInviteList(initiator);
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
    public boolean selfJoinInTeam(Map<String, Module> moduleMap, long initiator, int teamTarget, BaseTeam team) {
        Marry marry = ServiceHelper.marryService().getMarrySync(initiator);
        if (marry == null || marry.getState() != Marry.MARRIED || marry.getBreakState() == MarryManager.BREAK_STATE_OVER) {
            // 不是结婚状态，不能进入队伍
            return false;
        }
        TeamDungeonVo dungeonVo = TeamDungeonManager.getTeamDungeonVo(teamTarget);
        if (dungeonVo.getEntrance() != TeamDungeonManager.ENTRANCE_MARRY) { // 不是情谊副本，不能加入
            PlayerUtil.send(initiator, new ClientText(I18n.get("marry.team.not.dungeon")));
            return false;
        }

        // 判断次数够不够
        int dungeonCount = ServiceHelper.marryService().getRemainTeamDungeon(initiator);
        if (dungeonCount <= 0) {
            PlayerUtil.send(initiator, new ClientText("marry_btn_my_timenotenough"));
            return false;
        }
        return true;
    }

    @Override
    public boolean otherJoinInTeam(long target, int teamTarget) {
        return false;
    }

    @Override
    public boolean canBeInvite(long invitorId, long inviteedId, int teamTarget) {
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
    public void handleOpenTeamUI(long roleId, Map<String, Module> moduleMap) {

    }

    @Override
    public void handleCloseTeamUI(long roleId) {

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
