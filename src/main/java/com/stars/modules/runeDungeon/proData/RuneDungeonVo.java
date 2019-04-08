package com.stars.modules.runeDungeon.proData;

import com.stars.util.StringUtil;

import java.util.List;
import java.util.Map;

public class RuneDungeonVo {
	
	private int tokendungeonId;//副本id
	
	private String stageId;//关卡id     N个
	
	private List<Integer> stageIdList;
	
	private String dungeonname;//副本名称
	
	private String recommend;//单人推荐战力        N个
	
	private List<Integer> recommendList;
	
	private int recommendlevel;//单人推荐等级
	
	private String singlekilldrop;//单人通关奖励      N个 
	
	private List<Integer> singleKillDropList;
	
	private int singlecompletedrop;//单人击杀所以boss奖励
	
	private String multikilldrop;//合作击杀奖励         对应怒气等级
	
	private List<Integer> multiKilldropList;
	
	private String multicompletedrop;//合作全通奖励
	
	private List<Integer> multicompletedropList;
	
	private int helpdrop;//好友助战奖励
	
	private String showmodel;//界面展示模型            N个
	
	private List<Integer> showModelList;
	
	private String showname;//展示boss名称           N个
	
	private List<String> showNameList;
	
	private String showicon;//BOSS的头像
	
	private List<String> showIconList;
	
	private int reqpower;//单次挑战消耗体力
	
	private Map<Integer, RuneDungeonStageInfo> stageInfoMap;

	public int getTokendungeonId() {
		return tokendungeonId;
	}

	public void setTokendungeonId(int tokendungeonId) {
		this.tokendungeonId = tokendungeonId;
	}

	public String getStageId() {
		return stageId;
	}

	public void setStageId(String stageId) throws Exception {
		this.stageId = stageId;
		this.stageIdList = StringUtil.toArrayList(stageId, Integer.class, '+');
	}

	public String getDungeonname() {
		return dungeonname;
	}

	public void setDungeonname(String dungeonname) {
		this.dungeonname = dungeonname;
	}

	public String getRecommend() {
		return recommend;
	}

	public void setRecommend(String recommend) throws Exception {
		this.recommend = recommend;
		this.recommendList = StringUtil.toArrayList(recommend, Integer.class, '+');
	}

	public int getRecommendlevel() {
		return recommendlevel;
	}

	public void setRecommendlevel(int recommendlevel) {
		this.recommendlevel = recommendlevel;
	}

	public String getSinglekilldrop() {
		return singlekilldrop;
	}

	public void setSinglekilldrop(String singlekilldrop) throws Exception {
		this.singlekilldrop = singlekilldrop;
		this.singleKillDropList = StringUtil.toArrayList(singlekilldrop, Integer.class, '+');
	}

	public int getSinglecompletedrop() {
		return singlecompletedrop;
	}

	public void setSinglecompletedrop(int singlecompletedrop) {
		this.singlecompletedrop = singlecompletedrop;
	}

	public String getMultikilldrop() {
		return multikilldrop;
	}

	public void setMultikilldrop(String multikilldrop) throws Exception {
		this.multikilldrop = multikilldrop;
		this.multiKilldropList = StringUtil.toArrayList(multikilldrop, Integer.class, '+');
	}

	public String getMulticompletedrop() {
		return multicompletedrop;
	}

	public void setMulticompletedrop(String multicompletedrop) throws Exception {
		this.multicompletedrop = multicompletedrop;
		this.multicompletedropList = StringUtil.toArrayList(multicompletedrop, Integer.class, '+');
	}

	public int getHelpdrop() {
		return helpdrop;
	}

	public void setHelpdrop(int helpdrop) {
		this.helpdrop = helpdrop;
	}

	public String getShowmodel() {
		return showmodel;
	}

	public void setShowmodel(String showmodel) throws Exception {
		this.showmodel = showmodel;
		this.showModelList = StringUtil.toArrayList(showmodel, Integer.class, '+');
	}

	public String getShowname() {
		return showname;
	}

	public void setShowname(String showname) throws Exception {
		this.showname = showname;
		this.showNameList = StringUtil.toArrayList(showname, String.class, '+');
	}

	public int getReqpower() {
		return reqpower;
	}

	public void setReqpower(int reqpower) {
		this.reqpower = reqpower;
	}

	public List<Integer> getStageIdList() {
		return stageIdList;
	}

	public void setStageIdList(List<Integer> stageIdList) {
		this.stageIdList = stageIdList;
	}

	public List<Integer> getRecommendList() {
		return recommendList;
	}

	public void setRecommendList(List<Integer> recommendList) {
		this.recommendList = recommendList;
	}

	public List<Integer> getSingleKillDropList() {
		return singleKillDropList;
	}

	public void setSingleKillDropList(List<Integer> singleKillDropList) {
		this.singleKillDropList = singleKillDropList;
	}

	public List<Integer> getShowModelList() {
		return showModelList;
	}

	public void setShowModelList(List<Integer> showModelList) {
		this.showModelList = showModelList;
	}

	public List<String> getShowNameList() {
		return showNameList;
	}

	public void setShowNameList(List<String> showNameList) {
		this.showNameList = showNameList;
	}

	public String getShowicon() {
		return showicon;
	}

	public void setShowicon(String showicon) throws Exception {
		this.showicon = showicon;
		this.showIconList = StringUtil.toArrayList(showicon, String.class, '+');
	}

	public List<String> getShowIconList() {
		return showIconList;
	}

	public void setShowIconList(List<String> showIconList) {
		this.showIconList = showIconList;
	}

	public List<Integer> getMultiKilldropList() {
		return multiKilldropList;
	}

	public void setMultiKilldropList(List<Integer> multiKilldropList) {
		this.multiKilldropList = multiKilldropList;
	}

	public List<Integer> getMulticompletedropList() {
		return multicompletedropList;
	}

	public void setMulticompletedropList(List<Integer> multicompletedropList) {
		this.multicompletedropList = multicompletedropList;
	}

	public Map<Integer, RuneDungeonStageInfo> getStageInfoMap() {
		return stageInfoMap;
	}

	public void setStageInfoMap(Map<Integer, RuneDungeonStageInfo> stageInfoMap) {
		this.stageInfoMap = stageInfoMap;
	}

}
