package com.stars.multiserver.teamPVPGame.stepIns;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.teamPVPGame.TPGFightScence;
import com.stars.multiserver.teamPVPGame.TPGHost;
import com.stars.multiserver.teamPVPGame.TPGTeam;
import com.stars.multiserver.teamPVPGame.TPGTeamMember;
import com.stars.multiserver.teamPVPGame.helper.TPGTask;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.services.chat.cache.MyLinkedList;
import com.stars.services.chat.cache.MyLinkedListNode;
import com.stars.services.summary.Summary;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * @author dengzhou
 *各阶段的父类
 */
public abstract class AbstractTPGStep {
	public long beginTime;
	public long endTime;
	public TPGHost tHost;
	
	/**
	 * 角色之于队伍
	 */
	protected Map<Long, TPGTeamMember>memberMap;
	
	protected Map<Integer, TPGTeam>teamMap;
	
	protected List<DbRow> insertDBList;
	
	protected MyLinkedList<DbRow> executeDBList;
	
	/**
	 * 单场战斗的时间
	 */
	public long lastTimeOfFight = 60000;
	
	public int fightScenceId;
	
	public int fightIdCounter = 0;

	// 通知相关
	public long noticeBeginTime;// 提示开始时间(距离开始时间点多少秒)
	public long noticeDisTime;// 滚屏提示间隔
	public long noticeCount;// 滚屏提示次数
	public String announceNotice;// 滚屏通知内容
	public int chatNoticeInterval;// 聊天通知间隔
	public int chatNoticeCount;// 聊天通知次数
	public String chatNotice;// 聊天通知内容

	public int awardTypeId;// 奖励类型Id,根据本服/跨服会有不同
	/**
	 * 任务列表
	 */
	public MyLinkedList<TPGTask>scheduleTasks;
	public MyLinkedList<TPGTask> announceTasks;
	public MyLinkedList<TPGTask> chatNoticeTasks;
	
	public AbstractTPGStep(){
		initSelf();
	}
	public void init(TPGHost tHost,Collection<TPGTeam> teams) throws Exception{
		this.tHost = tHost;
		initSelf();
		initTeamAndMember(teams);
		tHost.updateTeamInfo(teamMap);
		initConfig();
		init0(tHost, teams);
	}
	
	public void initFromDb (TPGHost tHost) throws Exception{
		this.tHost = tHost;
		initSelf();
		// 必须保证顺序,先加载队伍,再加载队员数据
		loadTeamsFromDB();
		loadMembersFromDB();
		tHost.updateTeamInfo(teamMap);
		initFromDB0(tHost);
		initConfig();
	}
	
	public abstract void init0(TPGHost tHost,Collection<TPGTeam> teams);
	
	public abstract void initFromDB0(TPGHost tHost);
	
	public abstract void initConfig() throws Exception;
	
	public abstract void doLuaFram(String fightScence,LuaFrameData luaFrameData);
	
	public abstract void doOffLine(long roleId);
	
	public abstract void doFightScenceEnd(TPGFightScence scence,TPGTeam winner,TPGTeam loser, Object... params);
	
	public abstract boolean offline(long memberId);

	public abstract void enterFight(long initiator);
	
	private void initSelf(){
		memberMap = new HashMap<Long, TPGTeamMember>();
		teamMap = new HashMap<Integer, TPGTeam>();
		insertDBList = new ArrayList<DbRow>();
		executeDBList = new MyLinkedList<DbRow>();
		scheduleTasks = new MyLinkedList<TPGTask>();
		announceTasks = new MyLinkedList<>();
		chatNoticeTasks = new MyLinkedList<>();
	}
	
	private void initTeamAndMember(Collection<TPGTeam> teams){
		if (StringUtil.isEmpty(teams))
			return;
		for (TPGTeam tpgTeam : teams) {
			for (TPGTeamMember tpgTeamMember : tpgTeam.getMembers().values()) {
				tpgTeamMember.setStep(tHost.getStep());
				tpgTeamMember.setUpdateStatus();
				this.insertDBList.add(tpgTeamMember);
				this.memberMap.put(tpgTeamMember.getRoleId(), tpgTeamMember);
			}
			this.teamMap.put(tpgTeam.getTeamId(), tpgTeam);
			tpgTeam.setStep(tHost.getStep());
			tpgTeam.setUpdateStatus();
			this.insertDBList.add(tpgTeam);
		}
	}
	
	/**
	 * 异步调用，需要自己实现数据同步
	 */
	public void saveToDb() {
		synchronized (insertDBList) {
			for (DbRow tpgTeam : insertDBList) {
				executeDBList.addLast(tpgTeam);
			}
			insertDBList.clear();
		}
		MyLinkedListNode<DbRow> node = executeDBList.getFirst();
		while (node != null) {
			DbRow team = (DbRow)node.getObject();
			boolean save = true;
			try {
				DBUtil.execSql(tHost.getDbAlias(), team.getChangeSql());
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
				save = false;
			}
			MyLinkedListNode<DbRow> remove = node;
			node = node.next;
			if (save) {
				executeDBList.remove(remove);
			}
		}
	}
	
	public abstract void onReceived(Object message);
	
	public abstract void maintenance();

	public void doTask(MyLinkedList<TPGTask> taskList) {
		long now = System.currentTimeMillis();
		MyLinkedListNode<TPGTask> node = taskList.getFirst();
		if (node != null) {
			TPGTask task = node.getObject();
			if (now >= task.getTime()) {
				taskList.remove(node);
				task.doTask();
			}
		}
	}
	
	protected void loadMembersFromDB() throws Exception{
		StringBuilder stringBuilder = new StringBuilder("select * from tpgmember where ");
		stringBuilder.append(" step='").append(tHost.getStep()).append("'");
		List<TPGTeamMember> result = DBUtil.queryList(tHost.getDbAlias(),TPGTeamMember.class, stringBuilder.toString());
		for (TPGTeamMember tpgTeamMember : result) {
			memberMap.put(tpgTeamMember.getRoleId(), tpgTeamMember);
			TPGTeam team = teamMap.get(tpgTeamMember.getTeamId());
			if (team == null)
				continue;
			team.addUpdateMember(tpgTeamMember);
		}
		// 战斗实体FightEntity
		List<Summary> summaryList = ServiceHelper.summaryService().getAllSummary(new LinkedList<>(memberMap.keySet()));
		for (Summary summary : summaryList) {
			if (summary == null || summary.isDummy()) {
				continue;
			}
			Map<String, FighterEntity> entityMap = FighterCreator.createBySummary((byte) 0, summary);
			memberMap.get(summary.getRoleId()).setfEntity(entityMap.get(String.valueOf(summary.getRoleId())));
		}
	}
	protected void loadTeamsFromDB() throws Exception{
		StringBuilder stringBuilder = new StringBuilder("select * from tpgteam where tpgid='");
		stringBuilder.append(tHost.getTpgId());
		stringBuilder.append("' and step='").append(tHost.getStep());
		stringBuilder.append("'");
		List<TPGTeam> result = DBUtil.queryList(tHost.getDbAlias(),TPGTeam.class, stringBuilder.toString());
		for (TPGTeam tpgTeam : result) {
			teamMap.put(tpgTeam.getTeamId(), tpgTeam);
		}
	}
	public long getLastTimeOfFight() {
		return lastTimeOfFight;
	}
	
	public void initNoticeConfig(String nocieConfig, String announceNotice, String chatNotice){
		//积分赛的比赛提示, 格式为: 提示开始时间点(距离开始时间点多少秒)+滚动提示间隔+滚动提示次数+聊天界面提示间隔+聊天界面提示次数
		String[] ss = nocieConfig.split("[+]");
		noticeBeginTime = Long.parseLong(ss[0])*DateUtil.SECOND;
		noticeDisTime = Long.parseLong(ss[1])*DateUtil.SECOND;
		noticeCount = Integer.parseInt(ss[2]);
		chatNoticeInterval = Integer.parseInt(ss[3]);
		chatNoticeCount = Integer.parseInt(ss[4]);
		this.announceNotice = announceNotice;
		this.chatNotice = chatNotice;
	}

	/**
	 * 是否在阶段开启时间内
	 *
	 * @return
	 */
	public boolean isBetweenTime() {
		return beginTime <= System.currentTimeMillis() && endTime == 0 ? Boolean.TRUE : System.currentTimeMillis() <= endTime;
	}

	/**
	 * 是否在队伍中
	 *
	 * @param inititor
	 * @return
	 */
	public boolean isInTeam(long inititor) {
		return memberMap.containsKey(inititor);
	}

	public TPGTeam getTeam(long inititor) {
		TPGTeamMember member = memberMap.get(inititor);
		if (member == null || member.getTeamId() == 0) {
			return null;
		}
		return teamMap.get(member.getTeamId());
	}

	public void updateTPGTeamMember(BaseTeamMember baseTeamMember) {
		if (!memberMap.containsKey(baseTeamMember.getRoleId())) {
			return;
		}
		TPGTeamMember tpgTeamMember = memberMap.get(baseTeamMember.getRoleId());
		tpgTeamMember.updateMember(baseTeamMember);
	}

	public void sendMatchResult(long initiator) {

	}
}
