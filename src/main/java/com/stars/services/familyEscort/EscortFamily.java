package com.stars.services.familyEscort;

/**
 * 
 * 运镖的家族信息
 * @author xieyuejun
 *
 */
public class EscortFamily {
	
	private String familyName;
	private long familyId;
	private int fightRank;
	
	public EscortFamily(String familyName, long familyId, int fightRank) {
		this.familyId = familyId;
		this.familyName = familyName;
		this.fightRank = fightRank;
	}
	
	public String getFamilyName() {
		return familyName;
	}
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	public long getFamilyId() {
		return familyId;
	}
	public void setFamilyId(long familyId) {
		this.familyId = familyId;
	}
	public int getFightRank() {
		return fightRank;
	}
	public void setFightRank(int fightRank) {
		this.fightRank = fightRank;
	}

}
