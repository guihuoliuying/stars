package com.stars.services.family.main.userdata;

public class FamilyLogData {
	
	private FamilyPo familyPo;
	
	private long master;
	
	private String assistantStr;//副族长
	
	private String elderStr;//元老
	
	private String memberStr;//普通成员
	
	private int activeNum;
	
	private int ranking;

	public FamilyLogData(FamilyPo familyPo, long master, String assistantStr, String elderStr, String memberStr,
			int activeNum, int ranking) {
		super();
		this.familyPo = familyPo;
		this.master = master;
		this.assistantStr = assistantStr;
		this.elderStr = elderStr;
		this.memberStr = memberStr;
		this.activeNum = activeNum;
		this.ranking = ranking;
	}

	public FamilyPo getFamilyPo() {
		return familyPo;
	}

	public void setFamilyPo(FamilyPo familyPo) {
		this.familyPo = familyPo;
	}

	public long getMaster() {
		return master;
	}

	public void setMaster(long master) {
		this.master = master;
	}

	public String getAssistantStr() {
		return assistantStr;
	}

	public void setAssistantStr(String assistantStr) {
		this.assistantStr = assistantStr;
	}

	public String getElderStr() {
		return elderStr;
	}

	public void setElderStr(String elderStr) {
		this.elderStr = elderStr;
	}

	public String getMemberStr() {
		return memberStr;
	}

	public void setMemberStr(String memberStr) {
		this.memberStr = memberStr;
	}

	public int getActiveNum() {
		return activeNum;
	}

	public void setActiveNum(int activeNum) {
		this.activeNum = activeNum;
	}

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}
	
}
