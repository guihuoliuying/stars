package com.stars.modules.baseteam.handler;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.baseteam.packet.ClientBaseTeamInfo;
import com.stars.modules.daily.DailyModule;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.summary.ForeShowSummaryComponent;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.teamdungeon.TeamDungeonManager;
import com.stars.modules.teamdungeon.prodata.TeamDungeonVo;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMatch;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.services.offlinepvp.cache.OPEnemyCache;

import java.util.*;

/**
 * Created by liuyuheng on 2016/11/9.
 */
public class DailyDungeonTeamHandler implements TeamHandler {

    @Override
    public void createTeam(Map<String, Module> moduleMap, BaseTeamMember creator, byte teamType, int target) {
        int teamDungeonId = target;
        TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo(teamDungeonId);
        if (teamDungeonVo == null) {
            PlayerUtil.send(creator.getRoleId(), new ClientText("队伍副本数据错误"));
            return;
        }
        RoleModule rm = (RoleModule) moduleMap.get(MConst.Role);
        if (rm.getLevel() < teamDungeonVo.getLevellimit()) {
            PlayerUtil.send(creator.getRoleId(), new ClientText("您的等级不满足队伍副本要求"));
            return;
        }
        DailyModule dm = (DailyModule) moduleMap.get(MConst.Daily);
        if (dm.getDailyRemain(teamDungeonVo.getDailyid()) <= 0) {
            PlayerUtil.send(creator.getRoleId(), new ClientText("playerteam_enterchance_empty", teamDungeonVo.getName()));
            return;
        }
        ServiceHelper.baseTeamService().createTeam(creator, teamType, TeamDungeonManager.minTeamCount,
                TeamDungeonManager.maxTeamCount, target);
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
        ServiceHelper.teamDungeonService().sendCanInviteList(initiator, team.getTarget(), rm.getJoinSceneStr());
    }

    @Override
    public boolean canChangeTeamTarget(Map<String, Module> moduleMap, long initiator, int target) {
        TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo(target);
        if (teamDungeonVo == null) {
            PlayerUtil.send(initiator, new ClientText("team_noDungeon"));
            return false;
        }
        RoleModule rm = (RoleModule) moduleMap.get(MConst.Role);
        if (rm.getLevel() < teamDungeonVo.getLevellimit()) {
            PlayerUtil.send(initiator, new ClientText("team_levelLimit"));
            return false;
        }
        DailyModule dm = (DailyModule) moduleMap.get(MConst.Daily);
        if (dm.getDailyRemain(teamDungeonVo.getDailyid()) <= 0) {
            PlayerUtil.send(initiator, new ClientText("playerteam_enterchance_empty", teamDungeonVo.getName()));
            return false;
        }
        return true;
    }
    
    @Override
    public boolean canAllChangeTeamTarget(Map<String, Module> moduleMap, long initiator, int target) {
        return true;
    }

    @Override
    public boolean selfJoinInTeam(Map<String, Module> moduleMap, long initiator, int teamTarget, BaseTeam team) {
        TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo(teamTarget);
        RoleModule rm = (RoleModule) moduleMap.get(MConst.Role);
        if (rm.getLevel() < teamDungeonVo.getLevellimit()) {
            PlayerUtil.send(initiator, new ClientText("team_levelLimit"));
            return false;
        }
        DailyModule dm = (DailyModule) moduleMap.get(MConst.Daily);
        if (dm.getDailyRemain(teamDungeonVo.getDailyid()) <= 0) {
            PlayerUtil.send(initiator, new ClientText("team_targetEnterNumNotEnough"));
            return false;
        }
        return true;
    }

    @Override
    public boolean otherJoinInTeam(long target, int teamTarget) {
        return ServiceHelper.teamDungeonService().isMemberIn(target, teamTarget);
    }
    
    @Override
    public boolean canBeInvite(long invitorId , long inviteedId, int teamTarget){
    	TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo(teamTarget);
        short dailyid = teamDungeonVo.getDailyid();
        ForeShowSummaryComponent fsSummary = (ForeShowSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(inviteedId, MConst.ForeShow);
        if (ForeShowConst.DAILY_TEAM_DEFEND.endsWith(dailyid + "")) {
            if (!fsSummary.isOpen(ForeShowConst.DAILY_TEAM_DEFEND)) {
                return false;
            }
        }
        if (ForeShowConst.DAILY_TEAM_CHALLENGE.endsWith(dailyid + "")) {
            if (!fsSummary.isOpen(ForeShowConst.DAILY_TEAM_CHALLENGE)) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public void changeTeamTarget(Map<String, Module> moduleMap, long initiator, int teamDungeonId) {
        TeamDungeonVo td = TeamDungeonManager.getTeamDungeonVo(teamDungeonId);
        if (td == null) {
            return;
        }
        RoleModule rm = (RoleModule) moduleMap.get(MConst.Role);
        if (rm.getLevel() < td.getLevellimit()) {
            ServiceHelper.baseTeamService().leaveTeam(initiator);
            return;
        }
        DailyModule dm = (DailyModule) moduleMap.get(MConst.Daily);
        if (dm.getDailyRemain(td.getDailyid()) <= 0) {
            ServiceHelper.baseTeamService().leaveTeam(initiator);
            return;
        }
        ClientBaseTeamInfo packet = new ClientBaseTeamInfo(ClientBaseTeamInfo.CHANGE_TEAM_TARGET);
        packet.setTeamTarget(teamDungeonId);
        PlayerUtil.send(initiator, packet);
        PlayerUtil.send(initiator, new ClientText("playerteam_tiptext_targetswitch", td.getName()));
    }

    @Override
    public List<BaseTeamMember> matchFakePlayer(BaseTeamMember captain, int count, List<Long> exception, BaseTeamMatch match, BaseTeam team) {
        List<BaseTeamMember> robotList = new ArrayList<>();
        List<OPEnemyCache> opEnemyListTemp = ServiceHelper.offlinePvpService().executeMatch(captain.getLevel(),
                captain.getJob(), count, exception);
        if (opEnemyListTemp == null || opEnemyListTemp.size() <= 0) {
            return robotList;
        }
        // 复制一份
        List<OPEnemyCache> opEnemyList = new LinkedList<>(opEnemyListTemp);
        for (OPEnemyCache opEnemy : opEnemyList) {
            // 复制一份
            Map<String, FighterEntity> entityMap = new HashMap<>();
//            entityMap.putAll(opEnemy.getEntityMap());
//            if (entityMap == null) {
//                continue;
//            }
            Set<Map.Entry<String, FighterEntity>> set = opEnemy.getEntityMap().entrySet();
            StringBuilder builder = new StringBuilder("");
            builder.append("isRobot=").append("1").append(";");
            FighterEntity entity;
            for (Map.Entry<String, FighterEntity> entry : set) {
                entity = entry.getValue().copy();
                //修改角色entity的阵营
                entity.setCamp(FighterEntity.CAMP_SELF);
                //添加机器人的标记
                entity.addExtraValue(builder.toString());
                if (entity.getFighterType() == FighterEntity.TYPE_PLAYER) {//若是玩家主角，则设置为robot
                	entity.setIsRobot(true);
				}
                entityMap.put(entry.getKey(),entity);
            }
            BaseTeamMember robotMember = new BaseTeamMember((byte) 1);// 构造假玩家数据
            robotMember.setRoleId(Long.parseLong(opEnemy.getUniqueId()));
            robotMember.setJob((byte) opEnemy.getJobId());
            robotMember.setEntityMap(entityMap);
            robotList.add(robotMember);
        }
        return robotList;
    }

    @Override
    public boolean matchTeamWithFakePlayer(BaseTeamMember creator, byte teamType, int target) {
        List<Long> exception = new LinkedList<>();
        exception.add(creator.getRoleId());
        List<BaseTeamMember> fakePlayers = matchFakePlayer(creator, 1, exception, null, null);
        if (fakePlayers == null || fakePlayers.isEmpty()) {
            return false;
        }
        ServiceHelper.baseTeamService().createTeamWithFakePlayer(creator, teamType, TeamDungeonManager.minTeamCount,
                TeamDungeonManager.maxTeamCount, target, fakePlayers);
        return true;
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
