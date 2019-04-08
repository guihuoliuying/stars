package com.stars.multiserver.daily5v5.data;

import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.util.DateUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Daily5v5MatchingVo {
	
	private long roleId;//玩家ID
	
	private String roleName;//玩家名
	
	private int level;
	
	private int job;//职业
	
	private int startMatchTime;//开始匹配时间
	
	private byte step = 1;//扩展阶段  初始默认为第一阶段
	
	private int lose;//连败次数
	
	private int win;//连胜次数
	
	private int fixIntegral;//修正段位分
	
	private int teamId;
	
	private int serverId;
	
	private String serverName;
	
	private FighterEntity entity;
	
	private int fightValue;
	
	private Map<Integer,Daily5v5BuffInfo> initiativeBuff;//主动
	
	private Map<Integer,Daily5v5BuffInfo> passivityBuff;//被动
	
	private Map<Integer, Integer> buffCD;//主动buff
	
	private Map<Integer, Set<Integer>> passivityBuffCd;

	public Daily5v5MatchingVo(long roleId, String roleName, int level, int job, int lose, int win, 
			int fixIntegral, int serverId, String serverName, FighterEntity entity, int fightValue, 
			Map<Integer,Daily5v5BuffInfo> initiativeBuff, Map<Integer,Daily5v5BuffInfo> passivityBuff) {
		super();
		this.roleId = roleId;
		this.roleName = roleName;
		this.level = level;
		this.job = job;
		this.startMatchTime = DateUtil.getCurrentTimeInt();
		this.lose = lose;
		this.win = win;
		this.fixIntegral = fixIntegral;
		this.serverId = serverId;
		this.serverName = serverName;
		this.entity = entity;
		entity.getFightScore();
		this.fightValue = fightValue;
		this.initiativeBuff = initiativeBuff;
		this.passivityBuff = passivityBuff;
		this.buffCD = new HashMap<Integer, Integer>();
		this.passivityBuffCd = new HashMap<>();
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public int getStartMatchTime() {
		return startMatchTime;
	}

	public void setStartMatchTime(int startMatchTime) {
		this.startMatchTime = startMatchTime;
	}

	public byte getStep() {
		return step;
	}

	public void setStep(byte step) {
		this.step = step;
	}

	public int getLose() {
		return lose;
	}

	public void setLose(int lose) {
		this.lose = lose;
	}

	public int getWin() {
		return win;
	}

	public void setWin(int win) {
		this.win = win;
	}

	public int getFixIntegral() {
		return fixIntegral;
	}

	public void setFixIntegral(int fixIntegral) {
		this.fixIntegral = fixIntegral;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public FighterEntity getEntity() {
		return entity;
	}

	public void setEntity(FighterEntity entity) {
		this.entity = entity;
	}
	
	public int getFightScore(){
		return this.entity.getFightScore();
	}

	public int getFightValue() {
		return fightValue;
	}

	public void setFightValue(int fightValue) {
		this.fightValue = fightValue;
	}

	public Map<Integer, Daily5v5BuffInfo> getInitiativeBuff() {
		return initiativeBuff;
	}

	public void setInitiativeBuff(Map<Integer, Daily5v5BuffInfo> initiativeBuff) {
		this.initiativeBuff = initiativeBuff;
	}

	public Map<Integer, Daily5v5BuffInfo> getPassivityBuff() {
		return passivityBuff;
	}

	public void setPassivityBuff(Map<Integer, Daily5v5BuffInfo> passivityBuff) {
		this.passivityBuff = passivityBuff;
	}

	public Map<Integer, Integer> getBuffCD() {
		return buffCD;
	}

	public void setBuffCD(Map<Integer, Integer> buffCD) {
		this.buffCD = buffCD;
	}

	public Map<Integer, Set<Integer>> getPassivityBuffCd() {
		return passivityBuffCd;
	}

	public void setPassivityBuffCd(Map<Integer, Set<Integer>> passivityBuffCd) {
		this.passivityBuffCd = passivityBuffCd;
	}

}
