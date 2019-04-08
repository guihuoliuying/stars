package com.stars.modules.baseteam.handler;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.baseteam.packet.ClientBaseTeamInfo;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.summary.DungeonSummaryComponent;
import com.stars.modules.elitedungeon.EliteDungeonManager;
import com.stars.modules.elitedungeon.EliteDungeonModule;
import com.stars.modules.elitedungeon.packet.ClientEliteDungeonPacket;
import com.stars.modules.elitedungeon.prodata.EliteDungeonRobotVo;
import com.stars.modules.elitedungeon.prodata.EliteDungeonVo;
import com.stars.modules.elitedungeon.recordmap.RecordMapEliteDungeon;
import com.stars.modules.elitedungeon.summary.EliteDungeonSummaryComponent;
import com.stars.modules.elitedungeon.teamMember.EliteDungeonRobotTeamMember;
import com.stars.modules.elitedungeon.userdata.ElitePlayerImagePo;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMatch;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.util.DateUtil;
import com.stars.util.RandomUtil;

import java.util.*;

/**
 * Created by gaopeidian on 2017/3/8.
 */
public class EliteDungeonTeamHandler implements TeamHandler {
	
    @Override
    public void createTeam(Map<String, Module> moduleMap, BaseTeamMember creator, byte teamType, int target) {
        int eliteDungeonId = target;
        
        //检查是否有配置
        EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(eliteDungeonId);
        if (eliteDungeonVo == null) {
            PlayerUtil.send(creator.getRoleId(), new ClientText("获取不到精英副本产品数据"));
            return;
        }
        
        //是否激活了该精英副本
        EliteDungeonModule eliteDungeonModule = (EliteDungeonModule)moduleMap.get(MConst.EliteDungeon);
        if (!eliteDungeonModule.isEliteDungeonActive(eliteDungeonVo.getEliteId())) {
        	PlayerUtil.send(creator.getRoleId(), new ClientText("未激活该精英副本"));
            return;
		}
        
        //是否开放精英副本
        ForeShowModule foreShowModule = (ForeShowModule)moduleMap.get(MConst.ForeShow);
        if (!foreShowModule.isOpen(ForeShowConst.ELITE_DUNGEON)) {
        	PlayerUtil.send(creator.getRoleId(), new ClientText("elitedungeon_createteam_notopen"));
            return;
		}
        
        //判断次数
        RecordMapEliteDungeon record = eliteDungeonModule.getRecord();
        int playCount = record.getPlayCount();
        int rewardTimes = record.getRewardTimes();
        int helpTimes = record.getHelpTimes();
                    
        if (playCount < rewardTimes) {//判断是否可收益进入    
        	//判断体力是否足够
            int costVigour = eliteDungeonVo.getVigorCost();
            RoleModule roleModule = (RoleModule)moduleMap.get(MConst.Role);
            int myVigour = roleModule.getRoleRow().getVigor();
            if (myVigour < costVigour) {
            	PlayerUtil.send(creator.getRoleId(), new ClientText("elitedungeon_createteam_vigorshort"));
                return;
    		}
		}else if (playCount >= rewardTimes && playCount < rewardTimes + helpTimes) {//判断是否可助战进入
			//助战中，不可创建队伍
			PlayerUtil.send(creator.getRoleId(), new ClientText("elitedungeon_createteam_helping"));
            return;
		}else if (playCount >= rewardTimes + helpTimes) {//次数耗尽
			PlayerUtil.send(creator.getRoleId(), new ClientText("elitedungeon_createteam_timesshort"));
            return;
		}
       
        ServiceHelper.baseTeamService().createTeam(creator, teamType, EliteDungeonManager.minTeamCount,
        		EliteDungeonManager.maxTeamCount, target);
        ServiceHelper.baseTeamService().matchMemeber(creator.getRoleId());
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
        ServiceHelper.eliteDungeonService().sendCanInviteList(initiator, team.getTarget(), rm.getJoinSceneStr());
    }

    @Override
    public boolean canChangeTeamTarget(Map<String, Module> moduleMap, long initiator, int target) {
    	int eliteId = target;
    	
    	//检查是否有配置
    	EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(eliteId);
        if (eliteDungeonVo == null) {
        	PlayerUtil.send(initiator, new ClientText("获取不到精英副本产品数据"));
            return false;
        }
    	
        //是否激活了该精英副本
        EliteDungeonModule eliteDungeonModule = (EliteDungeonModule)moduleMap.get(MConst.EliteDungeon);
        if (!eliteDungeonModule.isEliteDungeonActive(eliteDungeonVo.getEliteId())) {
        	PlayerUtil.send(initiator, new ClientText("未激活该精英副本"));
            return false;
		}
        
        //判断次数
        RecordMapEliteDungeon record = eliteDungeonModule.getRecord();
        int playCount = record.getPlayCount();
        int rewardTimes = record.getRewardTimes();
        int helpTimes = record.getHelpTimes();
                    
        if (playCount < rewardTimes) {//判断是否可收益进入    
        	//判断体力是否足够
            int costVigour = eliteDungeonVo.getVigorCost();
            RoleModule roleModule = (RoleModule)moduleMap.get(MConst.Role);
            int myVigour = roleModule.getRoleRow().getVigor();
            if (myVigour < costVigour) {
            	PlayerUtil.send(initiator, new ClientText("体力不足"));
                return false;
    		}
		}else if (playCount >= rewardTimes && playCount < rewardTimes + helpTimes) {//判断是否可助战进入
			//无需消耗，可进入
		}else if (playCount >= rewardTimes + helpTimes) {//次数耗尽
			PlayerUtil.send(initiator, new ClientText("elitedungeon_createteam_timesshort"));
            return false;
		}
       
        return true;
    }

    @Override
    public boolean canAllChangeTeamTarget(Map<String, Module> moduleMap, long initiator, int target) {
    	BaseTeam team = ServiceHelper.baseTeamService().getTeam(initiator);
        if (team == null) {
            PlayerUtil.send(initiator, new ClientText("team_noTeam"));
            return false;
        }
        
        int eliteId = target;
    	
    	//检查是否有配置
    	EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(eliteId);
        if (eliteDungeonVo == null) {
        	PlayerUtil.send(initiator, new ClientText("获取不到精英副本产品数据"));
            return false;
        }
    	
        Map<Long, String> memberNameMap = new HashMap<Long, String>();
    	Map<Long, Byte> tipsTypeMap = new HashMap<Long, Byte>();	
    	
    	for (BaseTeamMember teamMember : team.getMembers().values()) {
    		if (teamMember.getType() == (byte)1) {//机器人无需判断
				continue;
			}
    		
			long roleId = teamMember.getRoleId();
			DungeonSummaryComponent dsc = (DungeonSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
	                roleId, "dungeon");
			RoleSummaryComponent rsc = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
	                roleId, MConst.Role);
	        EliteDungeonSummaryComponent esc = (EliteDungeonSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
	                roleId, "elitedungeon");
			//判断是否激活了该精英副本
	        Map<Integer, Byte> dStatusMap = dsc.getDungeonStatusMap();
	        if (dStatusMap == null 
	        		|| !dStatusMap.containsKey(eliteDungeonVo.getActiveDungeon())
	        		|| dStatusMap.get(eliteDungeonVo.getActiveDungeon()) != DungeonManager.STAGE_PASSED) {
	        	memberNameMap.put(roleId, rsc.getRoleName());
	        	tipsTypeMap.put(roleId, ClientEliteDungeonPacket.TIPS_NOT_ACTIVE);
				continue;
			}
	        
	        //判断体力和次数  
	        int myVigour = rsc.getVigour();
	        int playCount = esc.getPlayCount();
	        int rewardTimes = esc.getRewardTimes();
	        int helpTimes = esc.getHelpTimes();
	                    
	        if (playCount < rewardTimes) {//判断是否可收益进入    
	        	//判断体力是否足够
	            int costVigour = eliteDungeonVo.getVigorCost();
	            if (myVigour < costVigour) {
	            	memberNameMap.put(roleId, rsc.getRoleName());
		        	tipsTypeMap.put(roleId, ClientEliteDungeonPacket.TIPS_NOT_ENOUGH_VIGOUR);
	            	continue;
	    		}
			}else if (playCount >= rewardTimes && playCount < rewardTimes + helpTimes) {//判断是否可助战进入
				//无需消耗，可进入
			}else if (playCount >= rewardTimes + helpTimes) {//次数耗尽
				memberNameMap.put(roleId, rsc.getRoleName());
	        	tipsTypeMap.put(roleId, ClientEliteDungeonPacket.TIPS_NO_TIMES);
	        	continue;
			}	        
		}
    	
        if (memberNameMap.size() > 0) {
			ClientEliteDungeonPacket clientEliteDungeonPacket = new ClientEliteDungeonPacket(ClientEliteDungeonPacket.CHANGE_TARGET_TIPS);
			clientEliteDungeonPacket.setMemberNameMap(memberNameMap);
			clientEliteDungeonPacket.setTipsTypeMap(tipsTypeMap);
			clientEliteDungeonPacket.setTargetEliteId(eliteId);
			PlayerUtil.send(initiator, clientEliteDungeonPacket);
            return false;
		}else{
			return true;
		}
    }
    
    @Override
    public boolean selfJoinInTeam(Map<String, Module> moduleMap, long initiator, int teamTarget, BaseTeam team) {
        int eliteId = teamTarget;
    	
    	//检查是否有配置
    	EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(eliteId);
        if (eliteDungeonVo == null) {
        	PlayerUtil.send(initiator, new ClientText("获取不到精英副本产品数据"));
            return false;
        }
    	
        //是否激活了该精英副本
        EliteDungeonModule eliteDungeonModule = (EliteDungeonModule)moduleMap.get(MConst.EliteDungeon);
        if (!eliteDungeonModule.isEliteDungeonActive(eliteDungeonVo.getEliteId())) {
        	PlayerUtil.send(initiator, new ClientText("未激活该精英副本"));
            return false;
		}
        
        //判断次数
        RecordMapEliteDungeon record = eliteDungeonModule.getRecord();
        int playCount = record.getPlayCount();
        int rewardTimes = record.getRewardTimes();
        int helpTimes = record.getHelpTimes();
                    
        if (playCount < rewardTimes) {//判断是否可收益进入    
        	//判断体力是否足够
            int costVigour = eliteDungeonVo.getVigorCost();
            RoleModule roleModule = (RoleModule)moduleMap.get(MConst.Role);
            int myVigour = roleModule.getRoleRow().getVigor();
            if (myVigour < costVigour) {
            	PlayerUtil.send(initiator, new ClientText("体力不足"));
                return false;
    		}
		}else if (playCount >= rewardTimes && playCount < rewardTimes + helpTimes) {//判断是否可助战进入
			//无需消耗，可进入
		}else if (playCount >= rewardTimes + helpTimes) {//次数耗尽
			//PlayerUtil.send(initiator, new ClientText("elitedungeon_createteam_timesshort"));
            return false;
		}
       
        return true;
    }

    @Override
    public boolean otherJoinInTeam(long target, int teamTarget) {
        return ServiceHelper.eliteDungeonService().isMemberIn(target, teamTarget);
    }

    @Override
    public boolean canBeInvite(long invitorId , long inviteedId, int teamTarget){
    	int eliteId = teamTarget;
    	//检查是否有配置
    	EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(eliteId);
        if (eliteDungeonVo == null) {
            return false;
        }
    	
    	EliteDungeonSummaryComponent esc = (EliteDungeonSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
    			inviteedId, "elitedungeon");
    	
        //判断次数
        int playCount = esc.getPlayCount();
        int rewardTimes = esc.getRewardTimes();
        int helpTimes = esc.getHelpTimes();
                    
        if (playCount >= rewardTimes + helpTimes) {//次数耗尽
			return false;
	    }
        
        return true;
    }
    
    @Override
    public void changeTeamTarget(Map<String, Module> moduleMap, long initiator, int newTeamTarget) {
        int eliteId = newTeamTarget;
    	
    	//检查是否有配置
    	EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(eliteId);
        if (eliteDungeonVo == null) {
            return;
        }
    	
        //是否激活了该精英副本
        EliteDungeonModule eliteDungeonModule = (EliteDungeonModule)moduleMap.get(MConst.EliteDungeon);
        if (!eliteDungeonModule.isEliteDungeonActive(eliteDungeonVo.getEliteId())) {
        	ServiceHelper.baseTeamService().leaveTeam(initiator);
            return;
		}
        
        //判断次数
        RecordMapEliteDungeon record = eliteDungeonModule.getRecord();
        int playCount = record.getPlayCount();
        int rewardTimes = record.getRewardTimes();
        int helpTimes = record.getHelpTimes();
                    
        if (playCount < rewardTimes) {//判断是否可收益进入    
        	//判断体力是否足够
            int costVigour = eliteDungeonVo.getVigorCost();
            RoleModule roleModule = (RoleModule)moduleMap.get(MConst.Role);
            int myVigour = roleModule.getRoleRow().getVigor();
            if (myVigour < costVigour) {
            	ServiceHelper.baseTeamService().leaveTeam(initiator);
                return;
    		}
		}else if (playCount >= rewardTimes && playCount < rewardTimes + helpTimes) {//判断是否可助战进入
			//无需消耗，可进入
		}else if (playCount >= rewardTimes + helpTimes) {//次数耗尽
			ServiceHelper.baseTeamService().leaveTeam(initiator);
            return;
		}
    	
        ClientBaseTeamInfo packet = new ClientBaseTeamInfo(ClientBaseTeamInfo.CHANGE_TEAM_TARGET);
        packet.setTeamTarget(newTeamTarget);
        PlayerUtil.send(initiator, packet);
        PlayerUtil.send(initiator, new ClientText("playerteam_tiptext_targetswitch", eliteDungeonVo.getName()));
    }

//    @Override
//    public List<BaseTeamMember> matchFakePlayer(BaseTeamMember captain, int count, List<Long> exception) {
//        List<BaseTeamMember> robotList = new ArrayList<>();
//        List<OPEnemyCache> opEnemyListTemp = ServiceHelper.offlinePvpService().executeMatch(captain.getLevel(),
//                captain.getJob(), count, exception);
//        if (opEnemyListTemp == null || opEnemyListTemp.size() <= 0) {
//            return robotList;
//        }
//        // 复制一份
//        List<OPEnemyCache> opEnemyList = new LinkedList<>(opEnemyListTemp);
//        for (OPEnemyCache opEnemy : opEnemyList) {
//            // 复制一份
//            Map<String, FighterEntity> entityMap = new HashMap<>();
////            entityMap.putAll(opEnemy.getEntityMap());
////            if (entityMap == null) {
////                continue;
////            }
//            Set<Map.Entry<String, FighterEntity>> set = opEnemy.getEntityMap().entrySet();
//            StringBuilder builder = new StringBuilder("");
//            builder.append("isRobot=").append("1").append(";");
//            FighterEntity entity;
//            for (Map.Entry<String, FighterEntity> entry : set) {
//                entity = entry.getValue().copy();
//                //修改角色entity的阵营
//                entity.setCamp(FighterEntity.CAMP_SELF);
//                //添加机器人的标记
//                entity.addExtraValue(builder.toString());
//                if (entity.getFighterType() == FighterEntity.TYPE_PLAYER) {//若是玩家主角，则设置为robot
//                	entity.setIsRobot(true);
//				}
//                entityMap.put(entry.getKey(),entity);
//            }
//            BaseTeamMember robotMember = new BaseTeamMember((byte) 1);// 构造假玩家数据
//            robotMember.setRoleId(Long.parseLong(opEnemy.getUniqueId()));
//            robotMember.setJob((byte) opEnemy.getJobId());
//            robotMember.setEntityMap(entityMap);
//            robotList.add(robotMember);
//        }
//        return robotList;
//    }
    
    @Override
    public List<BaseTeamMember> matchFakePlayer(BaseTeamMember captain, int count, List<Long> exception, BaseTeamMatch match, BaseTeam team) {
    	if(team==null) return null;
    	if(team.getMemberCount()>=EliteDungeonManager.maxTeamCount) return null;
    	//时间检测
    	int currentTime = DateUtil.getCurrentTimeInt();
    	if(currentTime<match.getEliteMatchTimes()){
    		return null;
    	}
    	int target = team.getTarget();
    	Map<Long, BaseTeamMember> robotMembers = team.getRobotMembers();
    	EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(target);
    	BaseTeamMember robotMember = null;
    	if(eliteDungeonVo!=null){
    		int[] levelRange = eliteDungeonVo.getLevelRange();
    		robotMember = getRobotMemberByPlayer(target, robotMembers, levelRange);//当前副本id检测
        	if(robotMember==null){//往前副本id检测
        		for(int i=target;i>=EliteDungeonManager.Min_Robot_StageId;i--){
        			robotMember = getRobotMemberByPlayer(i, robotMembers, levelRange);
        			if(robotMember!=null){
        				break;
        			}
        		}
        	}
        	if(robotMember==null){//往后副本id检测
        		for(int i=target;i<=EliteDungeonManager.Max_Robot_StageId;i++){
        			robotMember = getRobotMemberByPlayer(i, robotMembers, levelRange);
        			if(robotMember!=null){
        				break;
        			}
        		}
        	}
    	}
    	if(robotMember==null){//原始机器人数据
    		robotMember = getRobotMemberByRobotData(target);
    	}
    	List<BaseTeamMember> robotList = new ArrayList<>();
    	if(robotMember!=null){    		
    		robotList.add(robotMember);
    		int matchTime = getMatchTime(team.getMemberCount()-team.getPlayerMemberCount());
    		match.setEliteMatchTimes(match.getEliteMatchTimes()+matchTime);
    	}
    	return robotList;
    }
    
    private BaseTeamMember getRobotMemberByPlayer(int target, Map<Long, BaseTeamMember> robotMembers, int[] levelRange){
    	List<ElitePlayerImagePo> playerList = EliteDungeonManager.stagePlayerMap.get(target);
    	if(playerList==null) return null;
//    	EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(target);
//        if (eliteDungeonVo == null) {
//            return null;
//        }
//        int[] levelRange = eliteDungeonVo.getLevelRange();
    	List<ElitePlayerImagePo> randomList = new ArrayList<>();
    	for(ElitePlayerImagePo elitePlayerImagePo : playerList){
    		if(elitePlayerImagePo.getLevel()>=levelRange[0]&&elitePlayerImagePo.getLevel()<=levelRange[1]){
    			boolean isCountinue = false;
    			for(BaseTeamMember member : robotMembers.values()){
    				if(member.getName().equals(elitePlayerImagePo.getName())){
    					isCountinue = true;
    					break;
    				}
    			}
    			if(isCountinue){
    				continue;
    			}
    			
    			randomList.add(elitePlayerImagePo);
    		}
    	}
    	int size = randomList.size();
    	if(size>0){
    		long uid = EliteDungeonManager.getUid();
    		int rand = RandomUtil.rand(0, size-1);
    		ElitePlayerImagePo po = randomList.get(rand);
    		EliteDungeonRobotTeamMember robotMember = new EliteDungeonRobotTeamMember((byte) 1);// 构造假玩家数据
    		robotMember.setRoleId(uid);
    		robotMember.setStrRoleId("r"+uid);
    		robotMember.setJob((byte)po.getJob());
    		robotMember.setEntityMap(FighterCreator.createPlayerImageRobot(FighterEntity.CAMP_SELF, po, uid));
    		return robotMember;
    	}
    	return null;
    }
    
    private BaseTeamMember getRobotMemberByRobotData(int target){
    	EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(target);
        if (eliteDungeonVo == null) {
            return null;
        }
        int[] levelRange = eliteDungeonVo.getLevelRange();
        List<EliteDungeonRobotVo> randomList = new ArrayList<>();
        for(EliteDungeonRobotVo robotVo : EliteDungeonManager.robotList){
        	if(robotVo.getRobotLevel()>=levelRange[0]&&robotVo.getRobotLevel()<=levelRange[1]){        		
        		randomList.add(robotVo);
        	}
        }
        int size = randomList.size();
    	if(size>0){
    		int rand = RandomUtil.rand(0, size-1);
    		EliteDungeonRobotVo robotVo = randomList.get(rand);
    		EliteDungeonRobotTeamMember robotMember = new EliteDungeonRobotTeamMember((byte) 1);// 构造假玩家数据
    		robotMember.setRoleId(robotVo.getRobotId());
    		robotMember.setStrRoleId("r"+robotVo.getRobotId());
    		robotMember.setJob((byte)robotVo.getJobId());
    		robotMember.setEntityMap(FighterCreator.createRobot(FighterEntity.CAMP_SELF, robotVo));
    		return robotMember;
    	}
        return null;
    }

    @Override
    public boolean matchTeamWithFakePlayer(BaseTeamMember creator, byte teamType, int target) {
        List<Long> exception = new LinkedList<>();
        exception.add(creator.getRoleId());
        List<BaseTeamMember> fakePlayers = matchFakePlayer(creator, 1, exception, null, null);
        if (fakePlayers == null || fakePlayers.isEmpty()) {
            return false;
        }
        ServiceHelper.baseTeamService().createTeamWithFakePlayer(creator, teamType, EliteDungeonManager.minTeamCount,
                EliteDungeonManager.maxTeamCount, target, fakePlayers);
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
		ServiceHelper.eliteDungeonService().removeMemberId(roleId);
	}

	@Override
	public void newMatchHandle(BaseTeamMatch match, BaseTeam team) {
		int stamp = match.getStamp();
		if(team==null) return;
		int count = team.getMemberCount()-team.getPlayerMemberCount();
		int matchTime = getMatchTime(count);
		match.setEliteMatchTimes(stamp+matchTime);
	}
	
	private int getMatchTime(int count){
		int[] timerRange = EliteDungeonManager.timerRandomList.get(count);
		if(timerRange==null){
			timerRange = EliteDungeonManager.timerRandomList.get(0);
		}
		int time = RandomUtil.rand(timerRange[0], timerRange[1]);
		return time;
	}

	@Override
	public boolean isCanMatch(BaseTeamMatch match) {
		int currentTime = DateUtil.getCurrentTimeInt();
		int stamp = match.getStamp();
		if((currentTime-stamp)>=EliteDungeonManager.MATCH_TIME){
			return false;
		}
		return true;
	}

	@Override
	public BaseTeamMember createBaseTeamMember(Map<String, Module> moduleMap) {
		return null;
	}
}
