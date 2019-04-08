package com.stars.multiserver.camp.pojo;

import com.stars.modules.scene.fightdata.FighterEntity;

public class CampPlayerImageData {
	
	private FighterEntity entity;
	
	private int commonOfficerId;
	
	private int rareOfficerId;
	
	private int designateOfficerId;
	
	private int job;
	
	private int serverId;
	
	private int cityId;

	public FighterEntity getEntity() {
		return entity;
	}

	public void setEntity(FighterEntity entity) {
		this.entity = entity;
	}

	public int getCommonOfficerId() {
		return commonOfficerId;
	}

	public void setCommonOfficerId(int commonOfficerId) {
		this.commonOfficerId = commonOfficerId;
	}

	public int getRareOfficerId() {
		return rareOfficerId;
	}

	public void setRareOfficerId(int rareOfficerId) {
		this.rareOfficerId = rareOfficerId;
	}

	public int getDesignateOfficerId() {
		return designateOfficerId;
	}

	public void setDesignateOfficerId(int designateOfficerId) {
		this.designateOfficerId = designateOfficerId;
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

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

}
