package com.stars.modules.runeDungeon.proData;

public class RuneDungeonStageInfo {
	
	private int stageId;//关卡id
	
	private int recommend;//推荐战力
	
	private int killAward;//单人通关奖励
	
	private int showmodel;
	
	private String showname;
	
	private String showicon;

	public RuneDungeonStageInfo(int stageId, int recommend, int killAward, int showmodel, String showname, String showicon) {
		super();
		this.stageId = stageId;
		this.recommend = recommend;
		this.killAward = killAward;
		this.showmodel = showmodel;
		this.showname = showname;
		this.showicon = showicon;
	}

	public int getStageId() {
		return stageId;
	}

	public void setStageId(int stageId) {
		this.stageId = stageId;
	}

	public int getRecommend() {
		return recommend;
	}

	public void setRecommend(int recommend) {
		this.recommend = recommend;
	}

	public int getKillAward() {
		return killAward;
	}

	public void setKillAward(int killAward) {
		this.killAward = killAward;
	}

	public int getShowmodel() {
		return showmodel;
	}

	public void setShowmodel(int showmodel) {
		this.showmodel = showmodel;
	}

	public String getShowname() {
		return showname;
	}

	public void setShowname(String showname) {
		this.showname = showname;
	}

	public String getShowicon() {
		return showicon;
	}

	public void setShowicon(String showicon) {
		this.showicon = showicon;
	}

}
