package com.stars.services.elitedungeon;

import com.stars.core.dao.DbRowDao;
import com.stars.core.event.Event;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.modules.arroundPlayer.ArroundPlayer;
import com.stars.modules.baseteam.packet.ClientBaseTeamInvite;
import com.stars.modules.baseteam.userdata.TeamInvitee;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.summary.DungeonSummaryComponent;
import com.stars.modules.elitedungeon.EliteDungeonManager;
import com.stars.modules.elitedungeon.event.BackToCityFromEliteDungeonEvent;
import com.stars.modules.elitedungeon.event.EliteDungeonEnterFightEvent;
import com.stars.modules.elitedungeon.packet.ClientEliteDungeonPacket;
import com.stars.modules.elitedungeon.prodata.EliteDungeonVo;
import com.stars.modules.elitedungeon.summary.EliteDungeonSummaryComponent;
import com.stars.modules.elitedungeon.userdata.ElitePlayerImagePo;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.imp.fight.EliteDungeonScene;
import com.stars.modules.scene.packet.ClientRoleRevive;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.teamdungeon.TeamDungeonManager;
import com.stars.network.server.packet.Packet;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;
import com.stars.util.LogUtil;
import com.stars.util.RandomUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by gaopeidian on 2017/3/8.
 */
public class EliteDungeonServiceActor extends ServiceActor implements EliteDungeonService {
    // 参与的玩家数据池(有次数且打开组队副本界面)
    private Map<Integer, Set<Long>> memberIdPool;
    // 战斗场景,<teamId, FightScene>
    private Map<Integer, EliteDungeonScene> fightSceneMap;
    
    private DbRowDao dao;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd("eliteDungeonService", this);
        memberIdPool = new HashMap<>();
        fightSceneMap = new HashMap<>();
        dao = new DbRowDao(SConst.EliteDungeonService);

        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.EliteDungeon, new SchedulerTask(), 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void printState() {
        for (Map.Entry<Integer, Set<Long>> entry : memberIdPool.entrySet()) {
            LogUtil.info("容器大小输出:{},memberIdPool.get({}).size:{}" ,this.getClass().getSimpleName(),  entry.getKey() , entry.getValue().size());
        }
        LogUtil.info("容器大小输出:{},fightSceneMap.size:{}" , this.getClass().getSimpleName(), fightSceneMap.size());

    }

    @Override
    public boolean addMemberId(long roleId, int eliteDungeonId) {
        if (!memberIdPool.containsKey(eliteDungeonId)) {
            memberIdPool.put(eliteDungeonId, new HashSet<Long>());
        }
        memberIdPool.get(eliteDungeonId).add(roleId);
        return true;
    }

    @Override
    //public boolean removeMemberId(long roleId, int eliteDungeonId) {
    public void removeMemberId(long roleId, int eliteDungeonId) {
//        if (!memberIdPool.containsKey(eliteDungeonId)) {
//            //return false;
//        	return;
//        }
//        // 有队伍,先退队
//        BaseTeam team = ServiceHelper.baseTeamService().getTeam(roleId);
//        if (team != null) {
//            EliteDungeonScene eliteDungeonScene = fightSceneMap.get(team.getTeamId());
//            // 队伍正在战斗
//            if (eliteDungeonScene != null && eliteDungeonScene.stageStatus == SceneManager.STAGE_PROCEEDING) {
//            	eliteDungeonScene.exit(roleId);
////                int spendTime = (int) ((System.currentTimeMillis() - eliteDungeonScene.startTimestamp) / 1000);
////                ServiceHelper.roleService().notice(roleId, new TeamDungeonExitEvent(team.getTarget(), spendTime,
////                        teamDungeonScene.stageId));//只是打log？
//                if (eliteDungeonScene.hasNoPlayer()) {
//                	eliteDungeonScene.stageStatus = SceneManager.STAGE_FAIL;
//                    ServiceHelper.eliteDungeonService().removeFightScene(eliteDungeonScene.teamId);
//                }
//            }
//            if(team.getTeamType() == BaseTeamManager.TEAM_TYPE_ELITEDUNGEON) {
//                ServiceHelper.baseTeamService().leaveTeam(roleId);
//            }
//        }
//        //return memberIdPool.get(eliteDungeonId).remove(roleId);
//        memberIdPool.get(eliteDungeonId).remove(roleId);


        if (!memberIdPool.containsKey(eliteDungeonId)) {
            return;
        }

        for (EliteDungeonScene eliteDungeonScene : fightSceneMap.values()) {
            // 在某个战斗场景中且该场景正在战斗
            if (eliteDungeonScene.isIn(roleId) && eliteDungeonScene.stageStatus == SceneManager.STAGE_PROCEEDING) {
                eliteDungeonScene.exit(roleId);
                if (eliteDungeonScene.hasNoPlayer()) {
                    eliteDungeonScene.stageStatus = SceneManager.STAGE_FAIL;
                    ServiceHelper.eliteDungeonService().removeFightScene(eliteDungeonScene.teamId);
                }
            }
        }

        memberIdPool.get(eliteDungeonId).remove(roleId);
    }

    public void removeMemberId(long roleId){
        for (EliteDungeonScene eliteDungeonScene : fightSceneMap.values()) {
            // 在某个战斗场景中且该场景正在战斗
            if (eliteDungeonScene.isIn(roleId)) {
                eliteDungeonScene.exit(roleId);
                if (eliteDungeonScene.hasNoPlayer()) {
                    eliteDungeonScene.stageStatus = SceneManager.STAGE_FAIL;
                    ServiceHelper.eliteDungeonService().removeFightScene(eliteDungeonScene.teamId);
                }
            }
        }
    }
    
    @Override
    public boolean isMemberIn(long roleId, int eliteDungeonId) {
        if (!memberIdPool.containsKey(eliteDungeonId))
            return false;
        return memberIdPool.get(eliteDungeonId).contains(roleId);
    }

    /**
     * 下发可邀请列表
     *
     * @param initiator
     * @param target
     * @param scene
     */
    @Override
    public void sendCanInviteList(long initiator, int target, String scene) {
        int eliteDungeonId = target;
        EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(eliteDungeonId);
        if (eliteDungeonVo == null) return;

        reqArroundPlayerCanInviteList(initiator, scene, eliteDungeonVo.getActiveDungeon());
        sendCandidateInviteeList(initiator, scene, eliteDungeonVo.getActiveDungeon());
    }

    /**
     * 进入副本战斗
     *
     * @param initiator
     */
    @Override
    public void enterFight(long initiator) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(initiator);
        int eliteDungeonId = team.getTarget();
        EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo(eliteDungeonId);
        //判断队伍人数是否足够进入副本战斗
        if (eliteDungeonVo == null || team.getMemberCount() < team.getMinMemberCount()) {
            PlayerUtil.send(initiator, new ClientText("playerteam_dungeon_minnumber", Integer.toString(team.getMinMemberCount())));
            return;
        }
        EliteDungeonScene eliteDungeonScene = (EliteDungeonScene) SceneManager.newScene(SceneManager.SCENETYPE_ELITEDUNGEON);
        // 业务根据平均等级找到注入stageId
        int stageId = eliteDungeonVo.getStageId();
        StageinfoVo stageinfoVo = SceneManager.getStageVo(stageId);
        if (stageinfoVo == null) {
            return;
        }
        eliteDungeonScene.stageId = stageId;
        //设置成员ids
        eliteDungeonScene.addTeamMemberFighter(team.getMembers().values());
        if (!eliteDungeonScene.canEnter(null, eliteDungeonId)) {
            return;
        }
        eliteDungeonScene.enter(null, eliteDungeonId);
        ServiceHelper.baseTeamService().cancelMatchTeam(initiator, team.getTeamType(), eliteDungeonId);
        eliteDungeonScene.teamId = team.getTeamId();
        team.setFight(Boolean.TRUE);
        fightSceneMap.put(team.getTeamId(), eliteDungeonScene);
        //sendEvent(team.getPlayerMembers().keySet(), new TeamDungeonEnterEvent(stageId, teamDungeonVo.getTeamdungeonid()));
        //sendEvent(team.getPlayerMembers().keySet(), new DailyFuntionEvent(teamDungeonVo.getDailyid(), 1));
        //sendEvent(team.getPlayerMembers().keySet(), new JoinActivityEvent(getAchievementId(teamDungeonVo.getType())));
        sendEvent(team.getPlayerMembers().keySet(), new EliteDungeonEnterFightEvent(stageId, eliteDungeonVo.getEliteId()));
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
            LogUtil.info("EliteDungeonServiceActor.startFightTime get no team,roleId=" + roleId);
            return;
        }
        EliteDungeonScene eliteDungeonScene = (EliteDungeonScene) fightSceneMap.get(team.getTeamId());
        if (eliteDungeonScene == null) {
            LogUtil.info("EliteDungeonServiceActor.deadInDungeon eliteDungeonScene is null");
            return;
        }

        Packet packet = eliteDungeonScene.startFightTime();
        if (packet != null) {
            PlayerUtil.send(roleId, packet);
        }
    }

    @Override
    public void backToCity(long initiator) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(initiator);
        // 只有队长可以发起回城
        if (team == null || team.getCaptainId() != initiator) {
            PlayerUtil.send(initiator, new ClientText("team_notCaptain"));
            return;
        }
        //发回城消息给所有队员
        sendEvent(team.getPlayerMembers().keySet(), new BackToCityFromEliteDungeonEvent(team.getTarget()));
        team.setFight(Boolean.FALSE);
        // 销毁scene
        fightSceneMap.remove(team.getTeamId());
    }

    public void deadInDungeon(long initiator) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(initiator);
        EliteDungeonScene eliteDungeonScene = (EliteDungeonScene) fightSceneMap.get(team.getTeamId());
        if (eliteDungeonScene == null) {
            LogUtil.info("EliteDungeonServiceActor.deadInDungeon eliteDungeonScene is null");
            return;
        }
        eliteDungeonScene.dead(initiator, team.getTarget());
        //退出队伍
        ServiceHelper.baseTeamService().leaveTeam(initiator);
        //发送消息给客户端
        ClientEliteDungeonPacket packet = new ClientEliteDungeonPacket(ClientEliteDungeonPacket.BACK_TO_CITY);
        PlayerUtil.send(initiator, packet);
    }

    @Override
    public void receiveFightPacket(PlayerPacket packet) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(packet.getRoleId());
        if (team == null) {
            return;
        }
        EliteDungeonScene eliteDungeonScene = (EliteDungeonScene) fightSceneMap.get(team.getTeamId());
        if (eliteDungeonScene == null) {
            return;
        }
        eliteDungeonScene.receivePacket(null, packet);
    }

    @Override
    public boolean checkResurgence(long roleId) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(roleId);
        if (team == null)
            return false;
        EliteDungeonScene eliteDungeonScene = (EliteDungeonScene) fightSceneMap.get(team.getTeamId());
        if (eliteDungeonScene != null) {
            boolean result = eliteDungeonScene.checkRevive(String.valueOf(roleId));
            if (result) {
                sendPacket(team.getPlayerMembers().keySet(), new ClientRoleRevive(roleId, result));
            }
            return result;
        }
        return false;
    }

    @Override
    public void removeFightScene(int teamId) {
        fightSceneMap.remove(teamId);
    }

    /**
     * 给指定roleId通知事件
     *
     * @param memberIds
     * @param event
     */
    private void sendEvent(Collection<Long> memberIds, Event event) {
        for (long roleId : memberIds) {
            ServiceHelper.roleService().notice(roleId, event);
        }
    }

    /**
     * 给指定roleId发包
     *
     * @param memberIds
     * @param packet
     */
    private void sendPacket(Collection<Long> memberIds, Packet packet) {
        for (long roleId : memberIds) {
            PlayerUtil.send(roleId, packet);
        }
    }

    /**
     * 根据玩法类型获得成就Id
     *
     * @param teamDungeonType
     * @return
     */
//    private short getAchievementId(byte teamDungeonType) {
//        short achievementId = 0;
//        switch (teamDungeonType) {
//            case 1:// 守护类
//                achievementId = JoinActivityEvent.TEAMDUNGEON_DEFEND;
//                break;
//            case 2:// 挑战类
//                achievementId = JoinActivityEvent.TEAMDUNGEON_CHALLENGE;
//                break;
//        }
//        return achievementId;
//    }

    /**
     * 可邀请列表-周围玩家
     *
     * @param initiator
     * @param scene
     * @param
     */
    private void reqArroundPlayerCanInviteList(long initiator, String scene, int needActiveDungeonId) {
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

                DungeonSummaryComponent dsc = (DungeonSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                        arroundPlayer.getRoleId(), "dungeon");
                Map<Integer, Byte> dStatusMap = dsc.getDungeonStatusMap();
                if (dStatusMap == null
                        || !dStatusMap.containsKey(needActiveDungeonId)
                        || dStatusMap.get(needActiveDungeonId) != DungeonManager.STAGE_PASSED) {
                    continue;
                }

                //判断次数
                EliteDungeonSummaryComponent esc = (EliteDungeonSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                		arroundPlayer.getRoleId(), "elitedungeon");
                int playCount = esc.getPlayCount();
                int rewardTimes = esc.getRewardTimes();
                int helpTimes = esc.getHelpTimes();                            
                if (playCount >= rewardTimes + helpTimes) {//次数耗尽
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

    /**
     * 可邀请列表-好友&家族
     *
     * @param initiator
     * @param scene
     * @param
     */
    private void sendCandidateInviteeList(long initiator, String scene, int needActiveDungeonId) {
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

            DungeonSummaryComponent dsc = (DungeonSummaryComponent) summary.getComponent("dungeon");
            Map<Integer, Byte> dStatusMap = dsc.getDungeonStatusMap();
            if (dStatusMap == null
                    || !dStatusMap.containsKey(needActiveDungeonId)
                    || dStatusMap.get(needActiveDungeonId) != DungeonManager.STAGE_PASSED) {
                continue;
            }

            //判断次数
            EliteDungeonSummaryComponent esc = (EliteDungeonSummaryComponent) summary.getComponent("elitedungeon");
            int playCount = esc.getPlayCount();
            int rewardTimes = esc.getRewardTimes();
            int helpTimes = esc.getHelpTimes();                            
            if (playCount >= rewardTimes + helpTimes) {//次数耗尽
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

            DungeonSummaryComponent dsc = (DungeonSummaryComponent) summary.getComponent("dungeon");
            Map<Integer, Byte> dStatusMap = dsc.getDungeonStatusMap();
            if (dStatusMap == null
                    || !dStatusMap.containsKey(needActiveDungeonId)
                    || dStatusMap.get(needActiveDungeonId) != DungeonManager.STAGE_PASSED) {
                continue;
            }

            //判断次数
            EliteDungeonSummaryComponent esc = (EliteDungeonSummaryComponent) summary.getComponent("elitedungeon");
            int playCount = esc.getPlayCount();
            int rewardTimes = esc.getRewardTimes();
            int helpTimes = esc.getHelpTimes();                            
            if (playCount >= rewardTimes + helpTimes) {//次数耗尽
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
    public void addPlayerImageData(ElitePlayerImagePo po) {
    	List<ElitePlayerImagePo> playerList = EliteDungeonManager.stagePlayerMap.get(po.getStageid());
    	if(playerList==null){
    		playerList = new ArrayList<>();
    		EliteDungeonManager.stagePlayerMap.put(po.getStageid(), playerList);
    	}
    	List<ElitePlayerImagePo> rankList = new ArrayList<>(EliteDungeonManager.playerImageList);
    	if(playerList.size()>=EliteDungeonManager.KEEP_MAX_NUM){
    		int totalSize = rankList.size();
    		ElitePlayerImagePo playerImagePo = null;
    		List<ElitePlayerImagePo> removeList = new ArrayList<>();
    		for(int i=0;i<totalSize;i++){
    			playerImagePo = rankList.get(i);
    			if(playerImagePo.getLevel()==po.getLevel()){
    				removeList.add(playerImagePo);
    			}
    		}
    		if(!StringUtil.isEmpty(removeList)){    			
    			int rand = RandomUtil.rand(0, removeList.size()-1);
    			ElitePlayerImagePo elitePlayerImagePo = removeList.get(rand);
    			rankList.remove(elitePlayerImagePo);
    			dao.delete(elitePlayerImagePo);
    		}else{    			
    			Collections.sort(rankList);
    			if(rankList.size()>0){    				
    				ElitePlayerImagePo removePo = rankList.remove(0);
    				dao.delete(removePo);
    			}
    		}
    	}
    	rankList.add(po);
    	List<ElitePlayerImagePo> newList = new ArrayList<>(rankList);
    	EliteDungeonManager.stagePlayerMap.put(po.getStageid(), newList);
    	EliteDungeonManager.playerImageList = rankList;
		dao.insert(po);
    }
    
    @Override
    public void save() {
    	dao.flush();
    }

    class SchedulerTask implements Runnable {
        @Override
        public void run() {
            ServiceHelper.eliteDungeonService().executeTask();
        }
    }

    @Override
    public void executeTask() {
        if (fightSceneMap != null) {
            for (EliteDungeonScene eliteDungeonScene : fightSceneMap.values()) {
                eliteDungeonScene.onTime();
            }
        }
    }

}
