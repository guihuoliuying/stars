package com.stars.multiserver.daily5v5.data;

import com.stars.modules.scene.fightdata.FighterEntity;

import java.util.Map;

public class MatchingInfo {
	
	private long roleId;
	
	private String roleName;
	
	private int roleLevel;
	
	private int job;
	
	private int integal;
	
	private int win;
	
	private int lose;
	
	private int serverId;
	
	private String serverName;
	
	private FighterEntity entity;
	
	private int trueFightValue;//实际战力     结算   排行列表排序用
	
	private Map<Integer, Daily5v5BuffInfo> initiativeBuff;//主动
	
	private Map<Integer, Daily5v5BuffInfo> passivityBuff;//被动

	public MatchingInfo(long roleId, String roleName, int roleLevel, int job, int integal, 
			int win, int lose, int serverId, String serverName, FighterEntity entity, int trueFightValue, Map<Integer, Daily5v5BuffInfo> initiativeBuff,
			Map<Integer, Daily5v5BuffInfo> passivityBuff) {
		super();
		this.roleId = roleId;
		this.roleName = roleName;
		this.roleLevel = roleLevel;
		this.job = job;
		this.integal = integal;
		this.win = win;
		this.lose = lose;
		this.serverId = serverId;
		this.serverName = serverName;
		this.entity = entity;
		this.trueFightValue = trueFightValue;
		this.initiativeBuff = initiativeBuff;
		this.passivityBuff = passivityBuff;
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

	public int getRoleLevel() {
		return roleLevel;
	}

	public void setRoleLevel(int roleLevel) {
		this.roleLevel = roleLevel;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public int getIntegal() {
		return integal;
	}

	public void setIntegal(int integal) {
		this.integal = integal;
	}

	public int getWin() {
		return win;
	}

	public void setWin(int win) {
		this.win = win;
	}

	public int getLose() {
		return lose;
	}

	public void setLose(int lose) {
		this.lose = lose;
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

	public int getTrueFightValue() {
		return trueFightValue;
	}

	public void setTrueFightValue(int trueFightValue) {
		this.trueFightValue = trueFightValue;
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

}
