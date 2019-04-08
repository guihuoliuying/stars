package com.stars.modules.familyEscort;

import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.services.family.FamilyAuth;

/**
 * 角色传到公共业务的参数信息
 * @author xieyuejun
 *
 */
public class FamilyEscrotRoleParameter {
	private int serverId;
	private long enterFamilyId;
	private long myFamlilyId;
	private FighterEntity fe ;
	private FamilyAuth familyAuth;
	private int[] randomPos;
	
	private int[] oldPostion;
	
	public FamilyEscrotRoleParameter(int serverId,long enterFamilyId,long myFamlilyId,FighterEntity fe,int[] oldPostion,FamilyAuth familyAuth,int[] randomPos){
		this.enterFamilyId = enterFamilyId;
		this.myFamlilyId = myFamlilyId;
		this.fe = fe;
		this.oldPostion = oldPostion;
		this.serverId = serverId;
		this.familyAuth = familyAuth;
		this.randomPos = randomPos;
	}

	public long getEnterFamilyId() {
		return enterFamilyId;
	}

	public void setEnterFamilyId(long enterFamilyId) {
		this.enterFamilyId = enterFamilyId;
	}

	public long getMyFamlilyId() {
		return myFamlilyId;
	}

	public void setMyFamlilyId(long myFamlilyId) {
		this.myFamlilyId = myFamlilyId;
	}

	public FighterEntity getFe() {
		return fe;
	}

	public void setFe(FighterEntity fe) {
		this.fe = fe;
	}

	public int[] getOldPostion() {
		return oldPostion;
	}

	public void setOldPostion(int[] oldPostion) {
		this.oldPostion = oldPostion;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public FamilyAuth getFamilyAuth() {
		return familyAuth;
	}

	public void setFamilyAuth(FamilyAuth familyAuth) {
		this.familyAuth = familyAuth;
	}

	public int[] getRandomPos() {
		return randomPos;
	}

	public void setRandomPos(int[] randomPos) {
		this.randomPos = randomPos;
	}
	
	
	
	
}
