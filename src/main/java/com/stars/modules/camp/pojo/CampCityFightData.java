package com.stars.modules.camp.pojo;

import com.stars.modules.scene.fightdata.FighterEntity;

public class CampCityFightData {
	
	private long roleId;
	
	private FighterEntity entity;
	
	private int integral;//积分
	
	private int job;
	
	private int serverId;

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public FighterEntity getEntity() {
		return entity;
	}

	public void setEntity(FighterEntity entity) {
		this.entity = entity;
	}

	public int getIntegral() {
		return integral;
	}

	public void setIntegral(int integral) {
		this.integral = integral;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

}
