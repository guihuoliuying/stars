package com.stars.modules.skyrank;

import com.stars.modules.skyrank.prodata.*;

import java.util.*;

public class SkyRankManager {

	private static SkyRankManager manager = new SkyRankManager();

	public static SkyRankManager getManager() {
		return manager;
	}

	// 积分获取途径集合
	private Map<Short, SkyRankScoreVo> skyRankScoreMap = new HashMap<Short, SkyRankScoreVo>();

	// 赛季集合
	private List<SkyRankSeasonVo> skyRankSeasonList = new LinkedList<>();
	// 赛季集合
	private Map<Integer,SkyRankSeasonVo> skyRankSeasonMap = new HashMap<>();

	//段位配置集合
	private Map<Integer, SkyRankGradVo> skyRankGradMap = new HashMap<Integer, SkyRankGradVo>();
	
	//段位列表，有序从低到高
	private List<SkyRankGradVo> skyRankGradList = new ArrayList<>();
	
	private Map<Integer, SkyRankDailyAwardVo> rankDailyAwardMap = new HashMap<Integer, SkyRankDailyAwardVo>();

	//段位升级奖励
	private Map<Integer, SkyRankUpAwardVo> rankUpAwardMap = new HashMap<Integer, SkyRankUpAwardVo>();

	//赛季排名奖励
	private List<SkyRankSeasonRankAwardVo> rankAwardList = new LinkedList<>();

	//赛季段位奖励
	private Map<Integer, SkyRankSeasonGradAwardVo> seasonGradAwardMap = new HashMap<>();
	
	//奖励总集合
	private List<SkyRankAwardVo> awardList = new LinkedList<SkyRankAwardVo>();
	
	//段位等级信息（记录日志用）
	private Map<Integer, int[]> skyRankGradStageMap = new HashMap<>();
	
	//当前的赛季Id
	private int nowSeasonId;
	
	public SkyRankSeasonRankAwardVo getSkyRankSeasonRankAwardVo(int rank){
		for(SkyRankSeasonRankAwardVo rav :rankAwardList){
			if(rank >= rav.getLower() && rank <= rav.getUpper()){
				return rav;
			}
		}
		return null;
	}
	
	public SkyRankUpAwardVo getSkyRankUpAwardVo(int gradId){
		return rankUpAwardMap.get(gradId);
	}
	
	public SkyRankSeasonGradAwardVo getSkyRankSeasonGradAwardVo(int gradId){
		return seasonGradAwardMap.get(gradId);
	}
	
	public SkyRankGradVo getSkyRankGradVoByScore(int score){
		SkyRankGradVo lowRgv = null;
		for(SkyRankGradVo rgv:skyRankGradList){
			if(lowRgv == null || rgv.getReqscore() < lowRgv.getReqscore()){
				lowRgv = rgv;
			}
			if(score >= rgv.getReqscore()){
				return rgv;
			}
		}
		return lowRgv;
	}
	
	/**
	 * 获取段位奖励最低的分数
	 * @return
	 */
	public int getMinGradAwardScore(){
		int minScore =-1;
		for(SkyRankSeasonGradAwardVo gav:seasonGradAwardMap.values()){
			SkyRankGradVo gradVo = skyRankGradMap.get(gav.getGradId());
			if(gradVo == null)continue;
			if(gradVo.getReqscore() < minScore || minScore ==-1){
				minScore = gradVo.getReqscore();
			}
		}
		return minScore;
	}
	
	public SkyRankScoreVo getSkyRankScoreVo(short type){
		return skyRankScoreMap.get(type);
	}

	public Map<Short, SkyRankScoreVo> getSkyRankScoreMap() {
		return skyRankScoreMap;
	}

	public void setSkyRankScoreMap(Map<Short, SkyRankScoreVo> skyRankScoreMap) {
		this.skyRankScoreMap = skyRankScoreMap;
	}

	public List<SkyRankSeasonVo> getSkyRankSeasonList() {
		return skyRankSeasonList;
	}

	public void setSkyRankSeasonList(List<SkyRankSeasonVo> skyRankSeasonList) {
		this.skyRankSeasonList = skyRankSeasonList;
	}

	public Map<Integer, SkyRankGradVo> getSkyRankGradMap() {
		return skyRankGradMap;
	}
	
	public SkyRankGradVo getSkyRankGradById(int gradId) {
		return skyRankGradMap.get(gradId);
	}

	public void setSkyRankGradMap(Map<Integer, SkyRankGradVo> skyRankGradMap) {
		this.skyRankGradMap = skyRankGradMap;
	}

	public Map<Integer, SkyRankUpAwardVo> getRankUpAwardMap() {
		return rankUpAwardMap;
	}

	public void setRankUpAwardMap(Map<Integer, SkyRankUpAwardVo> rankUpAwardMap) {
		this.rankUpAwardMap = rankUpAwardMap;
	}

	public List<SkyRankSeasonRankAwardVo> getRankAwardList() {
		return rankAwardList;
	}

	public void setRankAwardList(List<SkyRankSeasonRankAwardVo> rankAwardList) {
		this.rankAwardList = rankAwardList;
	}

	public Map<Integer, SkyRankSeasonGradAwardVo> getSeasonGradAwardMap() {
		return seasonGradAwardMap;
	}

	public void setSeasonGradAwardMap(Map<Integer, SkyRankSeasonGradAwardVo> seasonGradAwardMap) {
		this.seasonGradAwardMap = seasonGradAwardMap;
	}

	public List<SkyRankGradVo> getSkyRankGradList() {
		return skyRankGradList;
	}

	public void setSkyRankGradList(List<SkyRankGradVo> skyRankGradList) {
		this.skyRankGradList = skyRankGradList;
	}

	public List<SkyRankAwardVo> getAwardList() {
		return awardList;
	}

	public void setAwardList(List<SkyRankAwardVo> awardList) {
		this.awardList = awardList;
	}

	public int getNowSeasonId() {
		return nowSeasonId;
	}

	public void setNowSeasonId(int nowSeasonId) {
		this.nowSeasonId = nowSeasonId;
	}

	public Map<Integer,SkyRankSeasonVo> getSkyRankSeasonMap() {
		return skyRankSeasonMap;
	}

	public void setSkyRankSeasonMap(Map<Integer,SkyRankSeasonVo> skyRankSeasonMap) {
		this.skyRankSeasonMap = skyRankSeasonMap;
	}
	
	public SkyRankSeasonVo getSkyRankSeasonVo(int id){
		return skyRankSeasonMap.get(id);
	}

	public Map<Integer, int[]> getSkyRankGradStageMap() {
		return skyRankGradStageMap;
	}

	public void setSkyRankGradStageMap(Map<Integer, int[]> skyRankGradStageMap) {
		this.skyRankGradStageMap = skyRankGradStageMap;
	}

	public Map<Integer, SkyRankDailyAwardVo> getRankDailyAwardMap() {
		return rankDailyAwardMap;
	}

	public void setRankDailyAwardMap(Map<Integer, SkyRankDailyAwardVo> rankDailyAwardMap) {
		this.rankDailyAwardMap = rankDailyAwardMap;
	}

}
