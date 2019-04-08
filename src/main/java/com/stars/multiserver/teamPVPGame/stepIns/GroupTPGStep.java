package com.stars.multiserver.teamPVPGame.stepIns;

import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.teampvpgame.TeamPVPGameManager;
import com.stars.modules.teampvpgame.prodata.DoublePVPConfigVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.multiserver.teamPVPGame.*;
import com.stars.multiserver.teamPVPGame.helper.FaceNode;
import com.stars.multiserver.teamPVPGame.helper.TPGGroup;
import com.stars.multiserver.teamPVPGame.helper.TPGTask;
import com.stars.services.ServiceHelper;
import com.stars.services.chat.ChatManager;
import com.stars.services.chat.cache.MyLinkedList;
import com.stars.util.DateUtil;
import com.stars.util._HashMap;

import java.util.*;

public class GroupTPGStep extends AbstractTPGStep {

	private Map<Integer, TPGGroup>groupMap = new HashMap<>();
	
	private List<TPGTeam>result;
	
	private long readyTime;
	private long fieldDisTime;
	private long ringDisTime;
	
	@Override
	public void init0(TPGHost tpgHost, Collection<TPGTeam> teams) {
		initGroup(teams.size() > 16 ? 4 : 2, teams);
	}

	@Override
	public void initFromDB0(TPGHost tpgFlow) {
		groupMap = new HashMap<Integer, TPGGroup>();
		StringBuilder sBuilder = new StringBuilder("select * from tpggroup where tpgid = '");
		sBuilder.append(tHost.getTpgId()).append("' and step='").append(tpgFlow.getStep()).append("'");
		try {
//			tpgid,step,groupid,nodeid,valuea,valueb,parentid
			List<_HashMap> list = DBUtil.queryList(tHost.getDbAlias(), _HashMap.class, sBuilder.toString());
			Map<String, String>node2Father = new HashMap<String, String>();
			Map<String, FaceNode<TPGTeam>>nodeMap = new HashMap<String, FaceNode<TPGTeam>>();
			for (_HashMap map : list) {
				FaceNode<TPGTeam> faceNode = new FaceNode<TPGTeam>(map.getString("nodeid"), null);
				nodeMap.put(faceNode.getId(), faceNode);
				int groupId = map.getInt(map.getKey("groupid"));
				TPGGroup tGroup = groupMap.get(groupId);
				if (tGroup == null) {
					tGroup = new TPGGroup(groupId,this);
					groupMap.put(groupId, tGroup);
				}
				
				int teamId = map.getInt(map.getKey("valuea"));
				if (teamId > -1) {
					faceNode.addValue(teamMap.get(teamId));
					tGroup.putTPGTeam(teamMap.get(teamId));
				}
				teamId = map.getInt(map.getKey("valueb"));
				if (teamId > -1) {
					faceNode.addValue(teamMap.get(teamId));
					tGroup.putTPGTeam(teamMap.get(teamId));
				}
				String father = map.getString("parentid");
				if (father.length() > 0) {
					node2Father.put(faceNode.getId(),father);
				}else {
					if (tGroup.getRoot() != null) {
						throw new RuntimeException("小组赛加载数据出错：group："+groupId);
					}
					tGroup.setRoot(faceNode);
				}
			}
			Set<String>set = node2Father.keySet();
			for (String string : set) {
				FaceNode<TPGTeam>node = nodeMap.get(string);
				node.setFather(nodeMap.get(node2Father.get(string)));
				nodeMap.get(node2Father.get(string)).setSon(node);
			}
			Collection<TPGGroup>gCollection= groupMap.values();
			for (TPGGroup tpgGroup : gCollection) {
				tpgGroup.initFaceMap();
			}
			tHost.putFaceMap(groupMap);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public void initConfig() throws Exception {
		this.awardTypeId =
				tHost.getTpgType() == TPGUtil.TPG_LOACAL ? TPGUtil.AWARD_LOCAL_GROUP : TPGUtil.AWARD_REMOTE_GROUP;
		DoublePVPConfigVo configVo = TeamPVPGameManager.getConfigVo(tHost.getTpgType());
		if (configVo == null)
			return;
		//本服小组赛的开启时间段, 格式为: 周几+hh:mm:ss+hh:mm:ss
		//小组赛初始预备时间+单场间隔时间+轮次间隔时间.
		initConfig(configVo.getTeamBattleOpen(),
				DataManager.getCommConfig("doublepvp_teambattle_timeset"));
//		doublepvp_teambattle_stageid
		fightScenceId = Integer.parseInt(DataManager.getCommConfig("doublepvp_teambattle_stageid"));
		//单场战斗时间
		this.lastTimeOfFight = configVo.getTeamStageTime() * DateUtil.SECOND;
		// 本服/跨服使用提示不同
		String announceNotice = DataManager.getGametext("doublepvp_teambattle_servertips");
		String chatNotice = DataManager.getGametext("doublepvp_teambattle_servermessage");
		if (tHost.getTpgType() == TPGUtil.TPG_REMOTE) {
			announceNotice = DataManager.getGametext("doublepvp_teambattle_servertips_cross");
			chatNotice = DataManager.getGametext("doublepvp_teambattle_servermessage_cross");
		}
		initNoticeConfig(DataManager.getCommConfig("doublepvp_teambattletips_time"), announceNotice, chatNotice);
		initTask(3);
	}
	
	public void initGroup(int groupSize,Collection<TPGTeam> teams){
		LinkedList<TPGTeam>list = new LinkedList<TPGTeam>();
		if (teams != null) {
			for (TPGTeam tpgTeam : teams) {
				list.add(tpgTeam);
			}
		}
		TPGGroup[] tGroups = group(groupSize,list);
		groupMap = new HashMap<Integer, TPGGroup>();
		for (TPGGroup tpgGroup : tGroups) {
			groupMap.put(tpgGroup.getId(), tpgGroup);
			tpgGroup.makeFaceMap();
			tpgGroup.saveFaceMap();
		}
		tHost.putFaceMap(groupMap);
	}
	
	/**
	 * @param weekConfig 周几开始时间配置
	 * @param timeConfig 间隔时间配置
	 */
	public void initConfig(String weekConfig,String timeConfig){
		this.beginTime = TPGUtil.weekTime2AbsolutTime(weekConfig);
		//小组赛初始预备时间+单场间隔时间+轮次间隔时间.
		String[] ss = timeConfig.split("[+]");
		scheduleTasks = new MyLinkedList<TPGTask>();
		readyTime = Long.parseLong(ss[0]);
		fieldDisTime = Long.parseLong(ss[1]);
		ringDisTime = Long.parseLong(ss[2]);
	}
	
	/**
	 * @param ring 轮
	 */
	public void initTask(int ring){
		long noticeT = beginTime - this.noticeBeginTime;
		// 滚屏通知
		for (int i = 0;i < noticeCount;i++) {
			announceTasks.addLast(new NoticeGroupGameTask(noticeT+i*noticeDisTime));
		}
		// 聊天窗口通知
		for (int i = 0; i < chatNoticeCount; i++) {
			chatNoticeTasks.addLast(new ChatNoticeTask(noticeT + chatNoticeInterval * i));
		}
		long execTime = beginTime;
		// 轮次
		for (int i = 0; i < ring; i++) {
			// 每轮打三场
			for (int j = 0; j < 3; j++) {
				// 当前时间大于执行时间,跳过
				if (System.currentTimeMillis() > execTime) {
					execTime = execTime + readyTime + getLastTimeOfFight() + fieldDisTime;
					continue;
				}
				// 创建战斗
				scheduleTasks.addLast(new BeginFightGroupGameTask(execTime));
				execTime = execTime + readyTime;
				// 开始战斗
				scheduleTasks.addLast(new StartGroupFightTask(execTime));
				execTime = execTime + getLastTimeOfFight();
				// 一场战斗结束
				scheduleTasks.addLast(new EndFieldGroupGameTask(execTime));
				execTime = execTime + fieldDisTime;
			}
			// 一轮战斗结束
			scheduleTasks.addLast(new EndRingGroupGameTask(execTime));
			execTime = execTime + ringDisTime;
		}
		// 分组战斗结束
		scheduleTasks.addLast(new EndGroupGameTask(execTime));
	}

	@Override
	public void doLuaFram(String fightSceneId, LuaFrameData luaFrameData) {
		for (TPGGroup tpgGroup : groupMap.values()) {
			if (!tpgGroup.isFightSceneIn(fightSceneId))
				continue;
			tpgGroup.doLuaFram(fightSceneId, luaFrameData);
		}
	}

	@Override
	public void doOffLine(long roleId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFightScenceEnd(TPGFightScence scence, TPGTeam winner, TPGTeam loser, Object... params) {
		winner.fieldEnd(Boolean.TRUE);
		loser.fieldEnd(Boolean.FALSE);
		winner.setUpdateStatus();
		loser.setUpdateStatus();
		insertDBList.add(winner);
		insertDBList.add(loser);
		String key = scence.getKey();
		String[] ss = key.split("[|]");
		//清理战斗&玩家的连接切回来吧
		tHost.getFightBaseService().stopFight(tHost.getFightServerId(), 
				FightConst.T_TEAM_PVP_GAME_FIGHT, tHost.getServerId(), scence.getId());
		for (TPGTeamMember tpgTeamMember : winner.getMembers().values()) {
			MultiServerHelper.modifyConnectorRoute(tpgTeamMember.getRoleId(), tHost.getServerId());
		}
		for (TPGTeamMember tpgTeamMember : loser.getMembers().values()) {
			MultiServerHelper.modifyConnectorRoute(tpgTeamMember.getRoleId(), tHost.getServerId());
		}
		this.groupMap.get(Integer.parseInt(ss[0])).removeFightScene(scence.getId());
		// 发给胜利方
		scence.sendPacketToEnterMember(new ClientStageFinish(SceneManager.SCENETYPE_TPG, SceneManager.STAGE_VICTORY),
				winner.getTeamId());
		// 发给失败方
		scence.sendPacketToEnterMember(new ClientStageFinish(SceneManager.SCENETYPE_TPG, SceneManager.STAGE_FAIL),
				loser.getTeamId());
	}

	@Override
	public boolean offline(long memberId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void enterFight(long initiator) {
		TPGTeamMember teamMember = memberMap.get(initiator);
		if (teamMember == null)
			return;
		TPGTeam team = teamMap.get(teamMember.getTeamId());
		if (team == null)
			return;
		for (TPGGroup tpgGroup : groupMap.values()) {
			if (!tpgGroup.isFightSceneIn(team.getFightSceneId()))
				continue;
			tpgGroup.memberEnterFight(teamMember, team.getFightSceneId());
		}
	}

	@Override
	public void onReceived(Object message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void maintenance() {
		if (scheduleTasks.getFirst() == null) {
			//所有任务执行完了，那么该阶段结束了
			tHost.promotion(result);
			return;
		}else {
			doTask(scheduleTasks);
		}
		// 滚屏通知
		doTask(announceTasks);
		// 聊天窗口通知
		doTask(chatNoticeTasks);
	}
	
	public TPGGroup[] group(int groupSize,LinkedList<TPGTeam>list){
		TPGGroup[] tpgGroups;
//		if (list.size() > 16) {
//			groupSize = 4;
//		}else {
//			groupSize = 2;
//		}
		int id = 0;
		tpgGroups = new TPGGroup[groupSize];
		for (int i = 0;i < groupSize;i++) {
			tpgGroups[i] = new TPGGroup(id++,this);
			groupMap.put(tpgGroups[i].getId(),tpgGroups[i] );
		}
		if (groupSize == 4) {
			tpgGroups[0].putTPGTeam(list.remove(0));
			tpgGroups[2].putTPGTeam(list.remove(0));
			tpgGroups[3].putTPGTeam(list.remove(0));
			tpgGroups[1].putTPGTeam(list.remove(0));
			
		}else if (groupSize == 2) {
			if (list.size() > 0)
				tpgGroups[0].putTPGTeam(list.remove(0));
			if (list.size() > 0)
				tpgGroups[1].putTPGTeam(list.remove(0));
		}
		Random r = new Random();
		while (list.size() > 0) {
			for (TPGGroup tpgGroup : tpgGroups) {
				tpgGroup.putTPGTeam(list.remove(r.nextInt(list.size())));
				if (list.size() <= 0) {
					break;
				}
			}
		}
		return tpgGroups;
	}


	/**
	 * 创建战斗,可以进入,不能打
	 */
	public class BeginFightGroupGameTask extends TPGTask{
		
		public BeginFightGroupGameTask(long time){
			super(time);
		}
		@Override
		public void doTask() {
			Collection<TPGGroup>col = groupMap.values();
			for (TPGGroup tpgGroup : col) {
				tpgGroup.createFight();
			}
		}
	}

	/**
	 * 战斗开打
	 */
	class StartGroupFightTask extends TPGTask {

		public StartGroupFightTask(long time) {
			super(time);
		}

		@Override
		public void doTask() {
			for (TPGGroup tpgGroup : groupMap.values()) {
				tpgGroup.startFight();
			}
		}
	}
	
	/**
	 * @author dengzhou
	 *单场结束检查，每轮战三场
	 */
	class EndFieldGroupGameTask extends TPGTask{
		
		public EndFieldGroupGameTask(long time) {
			super(time);
		}
		
		@Override
		public void doTask() {
			Collection<TPGGroup>col = groupMap.values();
			for (TPGGroup tpgGroup : col) {
				tpgGroup.endField();
			}
		}
	}
	
	/**
	 * @author dengzhou
	 *每轮回合检查
	 */
	class EndRingGroupGameTask extends TPGTask{
		
		public EndRingGroupGameTask(long time) {
			super(time);
		}
		
		@Override
		public void doTask() {
			Collection<TPGGroup>col = groupMap.values();
			for (TPGGroup tpgGroup : col) {
				tpgGroup.endRing();
			}
			// 更新对阵图
			tHost.putFaceMap(groupMap);
		}
	}

	/**
	 * 小组比赛结束
	 */
	class EndGroupGameTask extends TPGTask{
		public EndGroupGameTask(long time) {
			super(time);
		}
		
		@Override
		public void doTask() {
			result = new ArrayList<TPGTeam>();
			Collection<TPGGroup>col = groupMap.values();
			for (TPGGroup tpgGroup : col) {
				result.add(tpgGroup.endGroup());
				// 发奖
				tpgGroup.grantReward();
			}
			// 保险起见更新一次对阵图
			tHost.putFaceMap(groupMap);
		}
	}

	/**
	 * 赛前通知
	 * 本服滚屏通知
	 */
	class NoticeGroupGameTask extends TPGTask {

		public NoticeGroupGameTask(long time) {
			super(time);
		}

		@Override
		public void doTask() {
			long dis = beginTime - System.currentTimeMillis();
			if (dis > 0) {
				dis = dis / 1000;
				ServiceHelper.chatService().announce(announceNotice, String.valueOf(dis));
			}
		}
	}

	/**
	 * 本服聊天窗口通知
	 */
	class ChatNoticeTask extends TPGTask {

		public ChatNoticeTask(long time) {
			super(time);
		}

		@Override
		public void doTask() {
			ServiceHelper.chatService().chat(TPGUtil.chatNoticeSenderName, ChatManager.CHANNEL_WORLD, 0L, 0L,
					chatNotice, Boolean.TRUE);
		}
	}
}
