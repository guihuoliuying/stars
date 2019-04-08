package com.stars.multiserver.teamPVPGame;

import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterPK;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.teampvpgame.packet.ClientTPGFightDamageCollect;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.multiserver.teamPVPGame.stepIns.AbstractTPGStep;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.fightbase.FightBaseService;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import io.netty.buffer.Unpooled;

import java.util.*;
import java.util.Map.Entry;

public class TPGFightScence {

	private Map<Integer, TPGTeam>teamMap;

	private Map<Long, TPGTeamMember>memberMap;

	private Map<String, FighterEntity>robotMap;

	private String id;

	private AbstractTPGStep tpgStep;

	private String key;
	private int luaFramCount = 0;// lua帧返回计数
	private int robotDeadCount = 0;// 机器人死亡计数
	private List<Byte> campList;
	private List<String> positionList;
	private List<Integer> rotationList;
	private Map<Integer, Set<Long>> enterMembers = new HashMap<>();// 进入场景的角色Id
	private boolean isStart = false;// 战斗是否开始

	/* 写死使用两个敌对阵营 */
	private byte camp1 = 11;
	private byte camp2 = 19;

	public TPGFightScence() {
		initCamp();
	}

	public TPGFightScence(String id,AbstractTPGStep tpgStep){
		this.id = id;
		this.tpgStep = tpgStep;
		teamMap = new HashMap<Integer, TPGTeam>();
		memberMap = new HashMap<Long, TPGTeamMember>();
		initCamp();
	}

	public TPGFightScence(String id,AbstractTPGStep tpgStep,String key){
		this.id = id;
		this.tpgStep = tpgStep;
		teamMap = new HashMap<Integer, TPGTeam>();
		memberMap = new HashMap<Long, TPGTeamMember>();
		this.key = key;
		initCamp();
	}

	private void initCamp() {
		campList = new LinkedList<>();
		campList.add(camp1);
		campList.add(camp2);
	}

	/**
	 * 一定要设置阵营!!!
	 *
	 * @param team
	 */
	public void addTeam(TPGTeam team){
		byte camp = campList.remove(0);
		String pos = positionList.remove(0);
		int rot = rotationList.remove(0);
		teamMap.put(team.getTeamId(), team);
		enterMembers.put(team.getTeamId(), new HashSet<Long>());
		for (TPGTeamMember tpgTeamMember : team.getMembers().values()) {
			memberMap.put(tpgTeamMember.getRoleId(), tpgTeamMember);
			tpgTeamMember.getfEntity().setCamp(camp);
			tpgTeamMember.getfEntity().setPosition(pos);
			tpgTeamMember.getfEntity().setRotation(rot);
		}
	}

	/**
	 * 一定要设置阵营!!!
	 * 设置机器人标记isRobot=1
     *
	 * @param robot 机器人
	 */
	public void addRobot(FighterEntity robot){
		byte camp = campList.get(0);
		String pos = positionList.get(0);
		int rot = rotationList.get(0);
		if (robotMap == null) {
			robotMap = new HashMap<>();
		}
		robotMap.put(robot.getUniqueId(), robot);
		robot.setCamp(camp);
		robot.setPosition(pos);
		robot.setRotation(rot);
		StringBuilder builder = new StringBuilder("");
		builder.append("isRobot=").append("1").append(";");
		robot.addExtraValue(builder.toString());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 创建战斗(没有战斗实体)
	 *
	 * @param fightBaseService
	 * @param fightServer
	 * @param localServer
	 * @param scence
	 * @param limitTime
	 */
	public void createFight(FightBaseService fightBaseService,int fightServer,int localServer,int scence,int limitTime){
		ClientEnterPK enterPack = new ClientEnterPK();
        enterPack.setFightType(SceneManager.SCENETYPE_TPG);
        enterPack.setStageId(scence);
        // 限制时间
        enterPack.setLimitTime(limitTime);
        List<FighterEntity>l = new ArrayList<FighterEntity>();
        Collection<TPGTeamMember>col = memberMap.values();
        for (TPGTeamMember tpgTeamMember : col) {
			l.add(tpgTeamMember.getfEntity());
		}
        enterPack.setFighterEntityList(l);
        enterPack.getFighterEntityList().clear();
		// 机器人数据不为空的话,需要先把机器人加入战斗
		if (!StringUtil.isEmpty(robotMap)) {
			List<FighterEntity> robotList = new LinkedList<>();
			robotList.addAll(robotMap.values());
			enterPack.setFighterEntityList(robotList);
		}

        NewByteBuffer buffer = new NewByteBuffer(Unpooled.buffer());
        enterPack.writeToBuffer(buffer);
        byte[] bytes = new byte[buffer.getBuff().readableBytes()];
        buffer.getBuff().readBytes(bytes);
        buffer.getBuff().release();
		fightBaseService.createFight(fightServer, FightConst.T_TEAM_PVP_GAME_FIGHT, localServer, id, bytes, null);
	}

	/**
	 * 玩家进入战斗
	 *
	 * @param fightBaseService
	 * @param fightServer
	 * @param localServer
	 * @param teamMember
	 */
	public void memberEnterFight(FightBaseService fightBaseService, int fightServer, int localServer,
								 TPGTeamMember teamMember) {
		enterMembers.get(teamMember.getTeamId()).add(teamMember.getRoleId());
		List<FighterEntity> addList = new LinkedList<>();
		addList.add(teamMember.getfEntity());
		fightBaseService.addFighter(fightServer, FightConst.T_TEAM_PVP_GAME_FIGHT, localServer, id, addList);
	}

	/**
	 * 准备结束,开始战斗
	 *
	 * @param fightBaseService
	 * @param fightServer
	 * @param localServer
	 */
	public void startFight(FightBaseService fightBaseService, int fightServer, int localServer) {
		// 开始战斗时没进入的队员置为死亡,hured=maxhp
		for (TPGTeamMember tpgTeamMember : memberMap.values()) {
			if (!enterMembers.get(tpgTeamMember.getTeamId()).contains(tpgTeamMember.getTeamId())) {
				tpgTeamMember.setDead(Boolean.TRUE);
				tpgTeamMember.setHurted(tpgTeamMember.getfEntity().getAttribute().getMaxhp());
			}
		}
		fightBaseService.readyFight(fightServer, FightConst.T_TEAM_PVP_GAME_FIGHT, localServer, id);
		isStart = Boolean.TRUE;
	}

	/**
	 * @param hurtMap 每帧伤害列表 key:目标角色ID value:记录表(key:攻击者ID value:伤害值)
	 */
	private void doLuaFramHurt(HashMap<String, HashMap<String, Integer>> hurtMap){
		Set<Entry<String, HashMap<String,Integer>>>set0 = hurtMap.entrySet();
		for (Entry<String, HashMap<String, Integer>> entry : set0) {
			TPGTeamMember hurted = null;
			// 非机器人
			if (!robotMap.containsKey(entry.getKey())) {
				hurted = memberMap.get(Long.parseLong(entry.getKey()));
			}
			Set<Entry<String, Integer>> set1 = entry.getValue().entrySet();
			for (Entry<String, Integer> entry2 : set1) {
				TPGTeamMember hurter = null;
				// 非机器人
				if (!robotMap.containsKey(entry2.getKey())) {
					hurter = memberMap.get(Long.parseLong(entry2.getKey()));
				}
				if (hurter != null) {
					hurter.addHurt(entry2.getValue());
				}
				if (hurted != null) {
					hurted.addHurted(entry2.getValue());
				}
			}
		}
		luaFramCount++;
		// 30帧=1s同步一次队员伤害统计
		if (luaFramCount >= 30) {
			luaFramCount = 0;
			ClientTPGFightDamageCollect packet = new ClientTPGFightDamageCollect();
			for (TPGTeam tpgTeam : teamMap.values()) {
				packet.setCollectDamage(tpgTeam.getMemberDamageMap());
				sendPacketToMember(packet, enterMembers.get(tpgTeam.getTeamId()));
			}
		}
	}

	/**
	 * @param deadMap 每帧死亡角色ID列表 key:死亡角色ID value:杀手角色ID
	 */
	private void doLuaFramDead(HashMap<String, String>deadMap){
		TPGTeam loser = null;
		for (String deadUId : deadMap.keySet()) {
			// 机器人死亡
			if (robotMap.containsKey(deadUId)) {
				robotDeadCount++;
				continue;
			}
			TPGTeamMember member = memberMap.get(Long.parseLong(deadUId));
			if (member == null) {
				continue;
			}
			member.setDead(true);
			TPGTeam team = teamMap.get(member.getTeamId());
			if (team.allDead()) {
				loser = team;
				break;
			}
		}
		if (loser != null) {
			TPGTeam winner = null;
			Collection<TPGTeam>col = teamMap.values();
			for (TPGTeam tpgTeam : col) {
				if (tpgTeam.getTeamId() != loser.getTeamId()) {
					winner = tpgTeam;
					break;
				}
			}
			this.tpgStep.doFightScenceEnd(this, winner, loser, robotDeadCount);
		}


	}

	/**
	 * 战斗限制时间到了
	 * 根据剩余血量百分比来决定胜负
	 */
	public void doLuaFramTimeOut(){
		Collection<TPGTeam>col = teamMap.values();
		TPGTeam winner = null;
		TPGTeam loser = null;
		for (TPGTeam tpgTeam : col) {
			if (winner == null) {
				winner = tpgTeam;
				continue;
			}
			if (!winner.isWin(tpgTeam)) {
				loser = winner;
				winner = tpgTeam;
			}else {
				loser = tpgTeam;
			}
			break;
		}
		this.tpgStep.doFightScenceEnd(this,winner, loser, robotDeadCount);
	}

	public void doLuaFram(LuaFrameData luaFrameData){
		if (luaFrameData.getDamage() != null) {
			doLuaFramHurt(luaFrameData.getDamage());
		}
		if (luaFrameData.getDead()!= null) {
			doLuaFramDead(luaFrameData.getDead());
		}
		if (luaFrameData.getFighttimeout()) {
			doLuaFramTimeOut();
		}
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setStageId(int stageId) {
		positionList = new LinkedList<>();
		rotationList = new LinkedList<>();
		StageinfoVo stageVo = SceneManager.getStageVo(stageId);
		if (stageVo == null) {
			LogUtil.error("找不到stageinfo表id={}的数据", stageId);
		}
		for (String pos : stageVo.getMultiPosList()) {
			positionList.add(pos);
		}
		for (int rot : stageVo.getMultiRotList()) {
			rotationList.add(rot);
		}
	}

	public Map<String, FighterEntity> getAllFighterEntity() {
		Map<String, FighterEntity> map = new HashMap<>();
		for (TPGTeamMember teamMember : memberMap.values()) {
			map.put(teamMember.getfEntity().getUniqueId(), teamMember.getfEntity());
		}
		if (!robotMap.isEmpty()) {
			map.putAll(robotMap);
		}
		return map;
	}

	public int getEnemyTeamId(int myTeamId) {
		int enemyTeamId = -1;
		for (int teamId : teamMap.keySet()) {
			if (teamId != myTeamId)
				return teamId;
		}
		return enemyTeamId;
	}

	public List<FighterEntity> getEnemyFighterEntity(int myTeamId) {
		List<FighterEntity> list = new LinkedList<>();
		if (teamMap.size() == 1) {
			if (teamMap.containsKey(myTeamId))
				list.addAll(robotMap.values());
		} else {
			int enemyTeamId = 0;
			for (int teamId : teamMap.keySet()) {
				if (myTeamId != teamId) {
					enemyTeamId = teamId;
					break;
				}
			}
			for (TPGTeamMember tpgTeamMember : memberMap.values()) {
				if (tpgTeamMember.getTeamId() == enemyTeamId) {
					list.add(tpgTeamMember.getfEntity());
				}
			}
		}
		return list;
	}

	public void sendPacketToEnterMember(PlayerPacket packet, int teamId) {
		if (enterMembers.containsKey(teamId) && !enterMembers.get(teamId).isEmpty()) {
			sendPacketToMember(packet, enterMembers.get(teamId));
		}
	}

	public boolean isStart() {
		return isStart;
	}

	private void sendPacketToMember(PlayerPacket packet, Collection<Long> roleIds) {
		for (long roleId : roleIds) {
			PacketManager.send(roleId, packet);
		}
	}
}
