package com.stars.multiserver.teamPVPGame.stepIns;

import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.offlinepvp.OfflinePvpManager;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterPK;
import com.stars.modules.teampvpgame.TeamPVPGameManager;
import com.stars.modules.teampvpgame.packet.ClientTPGData;
import com.stars.modules.teampvpgame.prodata.DoublePVPConfigVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.multiserver.teamPVPGame.*;
import com.stars.multiserver.teamPVPGame.helper.TPGTask;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.services.chat.ChatManager;
import com.stars.services.chat.cache.MyLinkedList;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dengzhou
 *积分赛阶段
 */
public class ScoreTPGStep extends AbstractTPGStep {
	
	private TreeSet<TPGTeam>baozouGroup;//暴走队
	
	private TreeSet<TPGTeam>shoucanGroup;//手残队
	
	private TreeSet<TPGTeam>putongGroup;//普通队
	
	private TreeSet<TPGTeam>qiangliGroup;//强力队
	
	private Map<String, TPGFightScence>fightScences;
	
	private Set<Long>offlineMember;
	
	/**
	 * 强力队、手残队等的一些判断条件参数
	 * 队伍状态数据, 格式为:强力队排名x+手残队连输场数y+暴走队连输场数z. 服务器排名前x名会被归为强力队, 连输y场会被归为手残队, 连输z场会被归为手残队
	 */
	private int[] statusPram;
	
	/**
	 * 算积分的
	 */
	private Integraph integraph;
		
	public ScoreTPGStep(){}

	@Override
	public void init0(TPGHost tpgHost, Collection<TPGTeam> teams) {
		tpgHost.updateScoreRank(getScoreRank());
	}
	
	@Override
	public void initFromDB0(TPGHost tpgHost) {
		//从DB加载数据启动，需要处理的事情
		tpgHost.updateScoreRank(getScoreRank());
	}

	@Override
	public void onReceived(Object message) {
		
	}
	
	@Override
	public void doFightScenceEnd(TPGFightScence scence,TPGTeam winner, TPGTeam loser, Object... params) {
		int robotDeadCount = (int) params[0];// 机器人死亡数量
		//清理战斗&玩家的连接切回来吧
		tHost.getFightBaseService().stopFight(tHost.getFightServerId(),
				FightConst.T_TEAM_PVP_GAME_FIGHT, tHost.getServerId(), scence.getId());
		// 机器人的情况下winner/loser可能为空
		this.fightScences.remove(scence.getId());
		// 算积分
		ClientStageFinish clientStageFinish = integraph.integral(winner, loser, robotDeadCount);
		if (winner != null) {
			winner.setLastWin(winner.getLastWin() + 1);
			winner.setLastLose(0);
			winner.setUpdateStatus();
			insertDBList.add(winner);
			for (TPGTeamMember tpgTeamMember : winner.getMembers().values()) {
				MultiServerHelper.modifyConnectorRoute(tpgTeamMember.getRoleId(), tHost.getServerId());
			}
			// 发给胜利方
			clientStageFinish.setStatus(SceneManager.STAGE_VICTORY);
			scence.sendPacketToEnterMember(clientStageFinish, winner.getTeamId());
		}
		if (loser != null) {
			loser.setLastWin(0);
			loser.setLastLose(loser.getLastLose() + 1);
			loser.setUpdateStatus();
			insertDBList.add(loser);
			for (TPGTeamMember tpgTeamMember : loser.getMembers().values()) {
				MultiServerHelper.modifyConnectorRoute(tpgTeamMember.getRoleId(), tHost.getServerId());
			}
			// 发给失败方
			clientStageFinish.setStatus(SceneManager.STAGE_FAIL);
			scence.sendPacketToEnterMember(clientStageFinish, loser.getTeamId());
		}
	}
	
	@Override
	public void initConfig() throws Exception{
		this.awardTypeId = TPGUtil.AWARD_LOCAL_SCORE;
		DoublePVPConfigVo configVo = TeamPVPGameManager.getConfigVo(tHost.getTpgType());
		if (configVo == null)
			return;
		//积分赛的开启时间段, 格式为: 周几+hh:mm:ss+hh:mm:ss,
		String configStr = configVo.getScoreBattleOpen();
		
		this.beginTime = TPGUtil.weekTime2AbsolutTime(configStr);
		
		//积分赛场数
		configStr = DataManager.getCommConfig("doublepvp_scorebattle_battletimes");
		int count = Integer.parseInt(configStr);
		//初始等候时间+场间间隔时间
		configStr = DataManager.getCommConfig("doublepvp_scorebattle_timeset");
		String[] ss = configStr.split("[+]");
		long waitTime = Integer.parseInt(ss[0])*DateUtil.SECOND;
		long dis = Integer.parseInt(ss[1])*DateUtil.SECOND;
		//积分赛战斗的场景id
		configStr = DataManager.getCommConfig("doublepvp_scorebattle_stageid");
		fightScenceId = Integer.parseInt(configStr);
		
		statusPram = new int[3];
		configStr = DataManager.getCommConfig("doublepvp_scorebattle_teamstage");
		ss = configStr.split("[+]");
		statusPram[0] = Integer.parseInt(ss[0]);
		statusPram[1] = Integer.parseInt(ss[1]);
		statusPram[2] = Integer.parseInt(ss[2]);
		
		//单场战斗时间
		this.lastTimeOfFight = configVo.getScoreStageTime() * DateUtil.SECOND;
		
		configStr = DataManager.getCommConfig("doublepvp_scorebattle_score");
		integraph = new Integraph(configStr);
		
		initNoticeConfig(DataManager.getCommConfig("doublepvp_scorebattletips_time"),
				DataManager.getGametext("doublepvp_scorebattle_servertips"),
				DataManager.getGametext("doublepvp_scorebattle_servermessage"));
		
		scheduleTasks = new MyLinkedList<TPGTask>();
		// 滚屏通知
		long noticeTime = beginTime - noticeBeginTime;
		for (int i = 0;i < noticeCount;i++) {
			announceTasks.addLast(new NoticeScoreGameTask(noticeTime + i * noticeDisTime));
		}
		// 聊天窗口通知
		for (int i = 0; i < chatNoticeCount; i++) {
			chatNoticeTasks.addLast(new ChatNoticeTask(noticeTime + chatNoticeInterval * i));
		}
		long execTime = beginTime;
		for (int n = 0; n < count; n++) {
			// 当前时间大于执行时间,跳过
			if (System.currentTimeMillis() > execTime) {
				execTime = execTime + (n == 0 ? waitTime : dis) + getLastTimeOfFight();
				continue;
			}
			// 匹配时间点
			scheduleTasks.addLast(new MatchScoreGameTask(execTime));
			if (n == 0) {
				execTime = execTime + waitTime;
			} else {
				execTime = execTime + dis;
			}
			// 开始时间点
			scheduleTasks.addLast(new StartScoreFightTask(execTime));
			execTime = execTime + getLastTimeOfFight();
			// 比赛结束时间点
			scheduleTasks.addLast(new EndFieldScoreGameTask(execTime));
		}
	}

	@Override
	public void maintenance() {
		if (scheduleTasks.getFirst() == null) {
			//所有任务执行完了，那么该阶段结束了
			tHost.promotion(StatResult());
			return;
		}else {
			doTask(scheduleTasks);
		}
		// 滚屏通知
		doTask(announceTasks);
		// 聊天窗口通知
		doTask(chatNoticeTasks);
	}

	/**
	 * 积分赛排行榜
	 *
	 * @return
	 */
	private List<TPGTeam> getScoreRank() {
		List<TPGTeam> list = new LinkedList<>();
		list.addAll(teamMap.values());
		Collections.sort(list, new ScoreComparator());
		return list.subList(0, Math.min(TeamPVPGameManager.scoreRankLimit - 1, Math.max(0, list.size())));
	}

	/**
	 * @return 返回积分赛进阶名单
	 */
	private List<TPGTeam> StatResult() {
		// 蛋疼的策划需求,这里所有队伍都要排名参与发奖
		List<TPGTeam> list = new ArrayList<TPGTeam>();
		Collection<TPGTeam> col = teamMap.values();
		for (TPGTeam tpgTeam : col) {
			list.add(tpgTeam);
		}
		Collections.sort(list, new ScoreComparator());
		// 发奖
		ServiceHelper.tpgLocalService().grantReward(list, awardTypeId, Boolean.TRUE);
		return list.subList(0, Math.min(31, Math.max(0, list.size())));
	}
	
	/**
	 * 按队伍状态分组
	 */
	public void group0(){
		baozouGroup = new TreeSet<TPGTeam>();
		putongGroup = new TreeSet<TPGTeam>();
		qiangliGroup = new TreeSet<TPGTeam>();
		shoucanGroup = new TreeSet<TPGTeam>();
		TreeSet<TPGTeam>g0 = new TreeSet<TPGTeam>();
		Collection<TPGTeam> col = teamMap.values();
		g0.addAll(col);
		int counter = 0;
		for (TPGTeam tpgTeam : col) {
			if (counter < statusPram[1]) {
				this.qiangliGroup.add(tpgTeam);
			}else {
				if (tpgTeam.getLastLose() >= statusPram[2]) {
					this.baozouGroup.add(tpgTeam);
				}else if (tpgTeam.getLastLose() >= statusPram[1] && tpgTeam.getLastLose() < statusPram[2]) {
					this.shoucanGroup.add(tpgTeam);
				}else {
					this.putongGroup.add(tpgTeam);
				}
			}
		}
	}
	
	/**
	 * 每次匹配前重置一下数据
	 */
	public void resetTeamMemberData(){
		Collection<TPGTeamMember>col = memberMap.values();
		for (TPGTeamMember tpgTeamMember : col) {
			tpgTeamMember.reset();
		}
	}
	
	public String getFightId(){
		fightIdCounter++;
		return this.tHost.getTpgId()+"tpg"+fightIdCounter;
	}
	
	@Override
	public void doLuaFram(String fightScence,LuaFrameData luaFrameData) {
		if (!fightScences.containsKey(fightScence))
			return;
		fightScences.get(fightScence).doLuaFram(luaFrameData);
	}
	
	@Override
	public void doOffLine(long roleId) {
		if (offlineMember == null) {
			offlineMember = new HashSet<Long>();
		}
		offlineMember.add(roleId);
	}
	
	@Override
	public boolean offline(long memberId) {
		return offlineMember.contains(memberId);
	}

	@Override
	public void enterFight(long initiator) {
		TPGTeamMember teamMember = memberMap.get(initiator);
		if (teamMember == null)
			return;
		TPGTeam team = teamMap.get(teamMember.getTeamId());
		if (team == null)
			return;
		TPGFightScence fightScence = fightScences.get(team.getFightSceneId());
		if (fightScence == null) {
			PacketManager.send(initiator, new ClientText("还没有匹配到队伍,请耐心等待"));
			LogUtil.info("队伍的fightsceneId不存在");
			return;
		}
		if (fightScence.isStart()) {
			PacketManager.send(initiator, new ClientText("战斗已经开始,不能进入"));
			return;
		}
		// 先切连接
		MultiServerHelper.modifyConnectorRoute(initiator, tHost.getFightServerId());
		// 发包给客户端
		ClientEnterPK clientEnterPK = new ClientEnterPK();
		clientEnterPK.setFightType(SceneManager.SCENETYPE_TPG);
		clientEnterPK.setStageId(fightScenceId);
		clientEnterPK.setLimitTime((int) (lastTimeOfFight / 1000));// 限制时间
		List<FighterEntity> entityList = new LinkedList<>();
		// 预加载所有战斗实体
		entityList.addAll(fightScence.getAllFighterEntity().values());
		clientEnterPK.setFighterEntityList(entityList);
		clientEnterPK.getFighterEntityList().clear();
		// 加入自己实体
		clientEnterPK.getFighterEntityList().add(teamMember.getfEntity());
		PacketManager.send(initiator, clientEnterPK);
		// 新加入玩家提交给战斗
		fightScence.memberEnterFight(tHost.getFightBaseService(), tHost.getFightServerId(), tHost.getServerId(), teamMember);
	}

	@Override
	public void sendMatchResult(long initiator) {
		TPGTeamMember tpgTeamMember = memberMap.get(initiator);
		if (tpgTeamMember == null)
			return;
		TPGTeam tpgTeam = teamMap.get(tpgTeamMember.getTeamId());
		if (tpgTeam == null)
			return;
		if (tpgTeam.getFightSceneId().isEmpty() || !fightScences.containsKey(tpgTeam.getFightSceneId())) {
			PacketManager.send(initiator, new ClientText("还没有匹配到队伍,请耐心等待"));
			return;
		}
		TPGFightScence tpgFightScence = fightScences.get(tpgTeam.getFightSceneId());
		// send to client,
		ClientTPGData clientTPGData = new ClientTPGData(ClientTPGData.SCORE_MATCH_RESULT);
		clientTPGData.setScoreEnemy(tpgFightScence.getEnemyTeamId(tpgTeam.getTeamId()),
				tpgFightScence.getEnemyFighterEntity(tpgTeam.getTeamId()));
		PacketManager.send(initiator, clientTPGData);
	}

	/**
	 * @赛前通知
	 * 本服滚屏通知
	 */
	class NoticeScoreGameTask extends TPGTask{
		
		public NoticeScoreGameTask(long time){
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
	
	/**
	 * @匹配
	 *
	 */
	class MatchScoreGameTask extends TPGTask{
		
		public MatchScoreGameTask(long time){
			super(time);
		}
		@Override
		public void doTask() {
			if(fightScences == null)
				fightScences = new ConcurrentHashMap<String, TPGFightScence>();
			group0();//分组先
			resetTeamMemberData();
			match(qiangliGroup, true, putongGroup,shoucanGroup);
			match(baozouGroup, false, putongGroup,shoucanGroup);
			match(shoucanGroup, false, putongGroup,shoucanGroup);
			match(putongGroup, true, putongGroup);
//			//匹配强力队
//			TPGTeam team = qiangliGroup.pollFirst();
//			while (team != null) {
//				TPGTeam match = putongGroup.pollFirst();
//				if (match == null) {
//					match = shoucanGroup.pollFirst();
//				}
//				TPGFightScence fightScence = new TPGFightScence(getFightId(),ScoreTPGStep.this);
//				fightScences.put(fightScence.getId(), fightScence);
//				fightScence.addTeam(team);
//				if (match == null) {
//					//匹配机器人，这里可能匹配到两个相同的机器人，名字就一样了，需要处理
//					fightScence.addRobot(OfflinePvpManager.getRobotByLevel(team.getMaxMemberLevel()));
//					fightScence.addRobot(OfflinePvpManager.getRobotByLevel(team.getMaxMemberLevel()));
//				}else {
//					fightScence.addTeam(match);
//				}
//				fightScence.beginFight(tFlow.getFightBaseService(), 
//						tFlow.getFightServerId(), tFlow.getServerId(), fightScenceId, 0);
//				team = qiangliGroup.pollFirst();
//			}
//			//匹配暴走队
//			team = baozouGroup.pollFirst();
//			while (team != null) {
//				TPGTeam match = putongGroup.pollLast();
//				if (match == null) {
//					match = shoucanGroup.pollFirst();
//				}
//				TPGFightScence fightScence = new TPGFightScence(getFightId(),ScoreTPGStep.this);
//				fightScences.put(fightScence.getId(), fightScence);
//				fightScence.addTeam(team);
//				if (match == null) {
//					//匹配机器人
//					fightScence.addRobot(OfflinePvpManager.getRobotByLevel(team.getMinMemberLevel()));
//					fightScence.addRobot(OfflinePvpManager.getRobotByLevel(team.getMinMemberLevel()));
//				}else {
//					fightScence.addTeam(match);
//				}
//				fightScence.beginFight(tFlow.getFightBaseService(), 
//						tFlow.getFightServerId(), tFlow.getServerId(), fightScenceId, 0);
//				team = baozouGroup.pollFirst();
//			}
//			//匹配手残队
//			team = shoucanGroup.pollFirst();
//			while (team != null) {
//				TPGTeam match = putongGroup.pollLast();
//				if (match == null) {
//					match = shoucanGroup.pollFirst();
//				}
//				TPGFightScence fightScence = new TPGFightScence(getFightId(),ScoreTPGStep.this);
//				fightScences.put(fightScence.getId(), fightScence);
//				fightScence.addTeam(team);
//				if (match == null) {
//					//匹配机器人
//					fightScence.addRobot(OfflinePvpManager.getRobotByLevel(team.getMinMemberLevel()));
//					fightScence.addRobot(OfflinePvpManager.getRobotByLevel(team.getMinMemberLevel()));
//				}else {
//					fightScence.addTeam(match);
//				}
//				fightScence.beginFight(tFlow.getFightBaseService(), 
//						tFlow.getFightServerId(), tFlow.getServerId(), fightScenceId, 0);
//				team = shoucanGroup.pollFirst();
//			}
//			//匹配普通队
//			team = putongGroup.pollFirst();
//			while (team != null) {
//				TPGTeam match = putongGroup.pollFirst();
//				TPGFightScence fightScence = new TPGFightScence(getFightId(),ScoreTPGStep.this);
//				fightScences.put(fightScence.getId(), fightScence);
//				fightScence.addTeam(team);
//				if (match == null) {
//					//匹配机器人，
//					fightScence.addRobot(OfflinePvpManager.getRobotByLevel(team.getMaxMemberLevel()));
//					fightScence.addRobot(OfflinePvpManager.getRobotByLevel(team.getMaxMemberLevel()));
//				}else {
//					fightScence.addTeam(match);
//				}
//				fightScence.beginFight(tFlow.getFightBaseService(), 
//						tFlow.getFightServerId(), tFlow.getServerId(), fightScenceId, 0);
//				team = putongGroup.pollFirst();
//			}
			
		}
		
		/**
		 * @param match 匹配队列
		 * @param isStrong 是否是强队
		 * @param matched 被匹配队列
		 */
		private void match(TreeSet<TPGTeam>match,boolean isStrong,TreeSet<TPGTeam>... matched){
			TPGTeam team = match.pollFirst();
			while (team != null) {
				TPGTeam matchTeam = null;
				int counter = 0;
				for (TreeSet<TPGTeam> treeSet : matched) {
					if (isStrong) {
						matchTeam = treeSet.pollFirst();
					}else {
						if (counter == 0) {
							matchTeam = treeSet.pollLast();
						}else {
							matchTeam = treeSet.pollFirst();
						}	
					}
					if (matchTeam != null) {
						break;
					}
				}
				TPGFightScence fightScence = new TPGFightScence(getFightId(),ScoreTPGStep.this);
				fightScence.setStageId(fightScenceId);
				fightScences.put(fightScence.getId(), fightScence);
				fightScence.addTeam(team);
				team.setFightSceneId(fightScence.getId());
				// 匹配对手
				int enemyTeamId = 0;
				List<FighterEntity> enemyList = new LinkedList<>();
				if (matchTeam == null) {
					//匹配机器人，这里可能匹配到两个相同的机器人，名字就一样了，需要处理
					List<FighterEntity> entityList = OfflinePvpManager.getRobotByLevel(
							isStrong ? team.getMaxMemberLevel() : team.getMinMemberLevel(), 2);
					for (FighterEntity entity : entityList) {
						fightScence.addRobot(entity);
					}
					enemyTeamId = -1;
					enemyList.addAll(entityList);// 机器人队伍Id=-1
				} else {
					fightScence.addTeam(matchTeam);
					enemyTeamId = matchTeam.getTeamId();
					for (TPGTeamMember teamMember : matchTeam.getMembers().values()) {
						enemyList.add(teamMember.getfEntity());
					}
				}
				fightScence.createFight(tHost.getFightBaseService(),
						tHost.getFightServerId(), tHost.getServerId(), fightScenceId, (int) getLastTimeOfFight());
				// send to client,
				ClientTPGData clientTPGData = new ClientTPGData(ClientTPGData.SCORE_MATCH_RESULT);
				clientTPGData.setScoreEnemy(enemyTeamId, enemyList);
				team.sendPacketToMember(clientTPGData);
				team = match.pollFirst();
			}
		}
	}
	
	
	
	/**
	 * @每轮战斗结束检测处理
	 *
	 */
	class EndFieldScoreGameTask extends TPGTask{
		public EndFieldScoreGameTask(long time){
			super(time);
		}
		@Override
		public void doTask() {
			if (fightScences.size() > 0) {
				Collection<TPGFightScence>col = fightScences.values();
				for (TPGFightScence tpgFightScence : col) {
					tpgFightScence.doLuaFramTimeOut();
				}
			}
			// 更新积分赛排行榜
			tHost.updateScoreRank(getScoreRank());
		}
	}

	class StartScoreFightTask extends TPGTask {

		public StartScoreFightTask(long time) {
			super(time);
		}

		@Override
		public void doTask() {
			for (TPGFightScence tpgFightScence : fightScences.values()) {
				tpgFightScence.startFight(tHost.getFightBaseService(), tHost.getFightServerId(), tHost.getServerId());
			}
		}
	}
	
	/**
	 * @author dengzhou
	 *积分器，算积分的
	 */
	class Integraph{
		
		private int winIntegral = 0;//胜利积分
		
		private int loseIntegral = 0;//失败积分
		
		/**
		 * A[0]：击杀一个人积分   A[1]：击杀两个人积分
		 */
		private int[] killIntegral;
		
		/**
		 * 连胜积分 a[0]:连胜场次  a[1]连胜积分
		 */
		private List<int[]>lastWinIntegral;
		
		public Integraph(String config) throws Exception{
			//战斗积分配置, 格式为:胜利积分+失败积分|连胜场次1+积分1,…..,连胜场次n+积分n|1+击杀积分1, 2+击杀积分2      
			//注：连胜场次n最小为2. 击杀积分固定配两个
			//注：这个格式解析起来真他妈的麻烦
			String[] sarr0 = config.split("[|]");
			String[] sarr1 = sarr0[0].split("[+]");
			winIntegral = Integer.parseInt(sarr1[0]);
			loseIntegral = Integer.parseInt(sarr1[1]);
			
			//击杀积分
			killIntegral = new int[2];
			sarr1 = sarr0[2].split("[,]");
			String[] sarr2 = sarr1[0].split("[+]");
			int a = 0;
			a = Integer.parseInt(sarr2[0]);
			if (a != 1) {
				throw new IllegalArgumentException("计算积分的参数错误：击杀数量不等于1");
			}
			killIntegral[0] = Integer.parseInt(sarr2[1]);
			sarr2 = sarr1[1].split("[+]");
			a = Integer.parseInt(sarr2[0]);
			if (a != 2) {
				throw new IllegalArgumentException("计算积分的参数错误：击杀数量不等于2");
			}
			killIntegral[1] = Integer.parseInt(sarr2[1]);
			
			//连胜积分
			lastWinIntegral = new ArrayList<int[]>();
			sarr1 = sarr0[1].split("[,]");
			for (String string : sarr1) {
				sarr2 = string.split("[+]");
				int[] b = new int[2];
				b[0] = Integer.parseInt(sarr2[0]);
				b[1] = Integer.parseInt(sarr2[1]);
				lastWinIntegral.add(b);
			}
		}
		
		public ClientStageFinish integral(TPGTeam win,TPGTeam lose, int robotDeadCount){
			List<Integer> winnerScoreInfo = new LinkedList<>();
			List<Integer> loserScoreInfo = new LinkedList<>();
			// 胜利/失败积分
			if (win != null) {
				winnerScoreInfo.add(win.getTeamId());// 队伍Id
				win.addScore(winIntegral);//胜利积分
			} else {// 胜利者是机器人
				winnerScoreInfo.add(-1);// 机器人队伍Id=-1
			}
			winnerScoreInfo.add(winIntegral);
			if (lose != null) {
				loserScoreInfo.add(lose.getTeamId());
				lose.addScore(loseIntegral);//失败积分
			} else {// 失败者是机器人
				loserScoreInfo.add(-1);
			}
			loserScoreInfo.add(loseIntegral);

			//击杀积分
			int kill = lose == null ? robotDeadCount : lose.getDeadCount();
			if (kill > 0) {
				if (win != null) {
					win.addScore(killIntegral[kill-1]);
				}
			}
			winnerScoreInfo.add(kill > 0 ? killIntegral[kill - 1] : 0);
			kill = win == null ? robotDeadCount : win.getDeadCount();
			if (kill > 0) {
				if (lose != null) {
					lose.addScore(killIntegral[kill-1]);
				}
			}
			loserScoreInfo.add(kill > 0 ? killIntegral[kill - 1] : 0);

			//连胜积分
			if (win != null) {
				int[] b = null;
				for (int[] a : lastWinIntegral) {
					if (b == null) {
						b = a;
					}
					if (win.getLastWin() > a[0]) {
						b = a;
					}else if (win.getLastWin() == a[0]) {
						b = a;
						break;
					}else {
						break;
					}
				}
				if (b != null) {
					win.addScore(b[1]);
					winnerScoreInfo.add(b[1]);
					loserScoreInfo.add(0);
				}else {
					LogUtil.error("计算积分错误，没有找到连胜策划数据");
				}
			} else {
				winnerScoreInfo.add(0);
				loserScoreInfo.add(0);
			}
			winnerScoreInfo.add(win != null ? win.getScore() : (winnerScoreInfo.get(1) + winnerScoreInfo.get(2)));// 总积分
			loserScoreInfo.add(lose != null ? lose.getScore() : (loserScoreInfo.get(1) + loserScoreInfo.get(2)));
			ClientStageFinish clientStageFinish = new ClientStageFinish(SceneManager.SCENETYPE_TPG, (byte) 0);
			clientStageFinish.setTPGScoreInfo(winnerScoreInfo.toArray(new Integer[5]), loserScoreInfo.toArray(new Integer[5]));
			return clientStageFinish;
		}
	}

	class ScoreComparator implements Comparator<TPGTeam> {

		@Override
		public int compare(TPGTeam o1, TPGTeam o2) {
			// 积分赛积分
			if (o1.getScore() != o2.getScore()) {
				return o1.getScore() - o2.getScore();
				// 队伍总战力
			} else if (o1.getFight() != o2.getFight()) {
				return o1.getFight() - o2.getFight();
				// 队伍Id(更小的(先报名)的获胜)
			} else if (o1.getTeamId() != o2.getTeamId()) {
				return o2.getTeamId() - o1.getTeamId();
			}
			return 0;
		}
	}
}
