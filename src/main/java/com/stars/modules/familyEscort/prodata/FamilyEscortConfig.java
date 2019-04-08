package com.stars.modules.familyEscort.prodata;

import com.stars.modules.data.DataManager;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * 家族运镖配置
 * 
 * @author xieyuejun
 *
 */
public class FamilyEscortConfig {
	
	public static FamilyEscortConfig config = new FamilyEscortConfig();
	
	private int safeStageId =931 ;
	private int pvpStageId  = 9321;
	
	private int escortTime = 99;// 每天的运镖次数
	private int robTime = 99;// 每天的抢标次数
	private int robBaseAwardMaxTime = 99;// 每天的最多的保底奖励次数
	private int loseProtectTime = 1000;// 被劫后的无敌时间
	private int winProtectTime= 1000;// 被劫后的无敌时间

	private String robedStar;// 每次被抢的随机星星及其权重 格式 权重=星星数,奖励&权重=星星数,奖励 如:  100=1&500=2&300=3&100=4

	private String robAward;// 每次抢的随机星星及其权重 格式 权重=星星数,奖励&权重=星星数,奖励 如:  100=301&500=301&300=301&100=301

	
	

	private String escortStartAward ="";// 运镖星星奖励及额外奖励 星星数=基本奖励,额外奖励 如: 1=100,102&2=100,104&3=105,106
	
	private Map<Integer,Integer> robedStarMap = new HashMap<>();// 每次被抢的随机星星及其权重
	private Map<Integer,Integer> robAwardMap = new HashMap<>();// 每次抢的星星奖励
	private Map<Integer,int[]> escortStartAwardMap = new HashMap<>();// 运镖星星奖励及额外奖励
	
	
	private int escortPathTime = 120000;//运镖路长时间
	private int robMinStar;// 保底星星

	
	private int robBaseAward; //劫镖的保底奖励
	private int robShowAward;// 劫镖展示奖励
	private int escortShowAward;// 运镖展示奖励
	

	private List<int[]> escortBornPosList; // 运镖出生点
	private List<int[]> robBornPosList;// 抢镖出生点

	private int roadBlockDistance = 10;// 障碍物触发距离
	private int roadBlockOdds = 500;// 障碍物触发几率

	/* 1v1pvp相关 */
	private int stageId; // 关卡id
	
	public static void main(String[] args){
		System.err.println(int[].class.getConstructors().toString());
	}

	public void init() {
		escortTime = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "familyescort_escortTime", 2);
		robTime = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "familyescort_robTime", 2);
		
		robBaseAwardMaxTime = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "familyescort_robBaseAwardMaxTime", 20);
		
		loseProtectTime = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "familyescort_loseProtectTime", 1000);
		winProtectTime = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "familyescort_winProtectTime", 2);
		
		robedStar= com.stars.util.MapUtil.getString(DataManager.commonConfigMap, "familyescort_robedStar","");
		robedStarMap = StringUtil.toMap(robedStar, Integer.class, Integer.class, '=', '&');
		
		robAward = com.stars.util.MapUtil.getString(DataManager.commonConfigMap, "familyescort_robAward","");
		robAwardMap = StringUtil.toMap(robedStar, Integer.class, Integer.class, '=', '&');
		
		escortPathTime = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "familyescort_escortPathTime",60);
		escortStartAward = com.stars.util.MapUtil.getString(DataManager.commonConfigMap, "familyescort_escortStarAward", "");
		robMinStar = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "familyescort_robMinStar", 2);
		
		
		robShowAward = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "familyescort_robShowAward", 1);
		escortShowAward = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "familyescort_escortShowAward", 1);

		roadBlockDistance = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "familyescort_roadBlockDistance", 2);
		roadBlockOdds = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "familyescort_roadBlockOdds", 2);
		
		robBaseAward = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "familyescort_robBaseAward", 0);

		String strs = com.stars.util.MapUtil.getString(DataManager.commonConfigMap, "familyescort_robAwardPecent", "");
		String[] sa = strs.split(",");
		
		robedStarMap = StringUtil.toMap(robedStar, Integer.class, Integer.class, '=', '&');
		robAwardMap = StringUtil.toMap(robAward, Integer.class, Integer.class, '=', '&');
		
		Map<Integer,String> tmpEscortStartAwardMap = StringUtil.toMap(escortStartAward, Integer.class, String.class,  '=', '&');
		
		
		Map<Integer,int[]> tEscortStartAwardMap = new HashMap<>();
		
		for(Entry<Integer,String> entry:tmpEscortStartAwardMap.entrySet()){
			String[] as = entry.getValue().trim().split(",");
			int[] awards =  new int[as.length];
			for(int i = 0;i < awards.length;i ++){
				awards[i] = Integer.parseInt(as[i]);
			}
			tEscortStartAwardMap.put(entry.getKey(), awards);
			
		}
		
		escortStartAwardMap = tEscortStartAwardMap;
		
		
		List<int[]> tmpEscortBornPosList = new ArrayList<int[]>(); // 运镖出生点
		strs = com.stars.util.MapUtil.getString(DataManager.commonConfigMap, "familyescort_escortBornPosList", "100+100+100");
		sa = strs.split("\\|");
		if (sa.length > 0) {
			for (String pos : sa) {
				if (pos.trim().length() >0) {
					String[] ps = pos.split("\\+");
					int[] bornP = new int[] { Integer.parseInt(ps[0]), Integer.parseInt(ps[1]),Integer.parseInt(ps[2]) };
					tmpEscortBornPosList.add(bornP);
				}
			}
		}
		escortBornPosList = tmpEscortBornPosList;

		List<int[]> tmpRobBornPosList = new ArrayList<int[]>(); // 劫镖出生点
		strs = MapUtil.getString(DataManager.commonConfigMap, "familyescort_robBornPosList", "100+100+100");
		sa = strs.split("\\|");
		if (sa.length > 0) {
			for (String pos : sa) {
				if (pos.trim().length() >0) {
					String[] ps = pos.split("\\+");
					int[] bornP = new int[] { Integer.parseInt(ps[0]), Integer.parseInt(ps[1]), Integer.parseInt(ps[2]) };
					tmpRobBornPosList.add(bornP);
				}
			}
		}

		/* 1v1相关 */
		stageId = DataManager.getCommConfig("familyescort_pvp_stageid", 9321); // pvp的stageid
		robBornPosList = tmpRobBornPosList;
	}

	public int getEscortTime() {
		return escortTime;
	}

	public void setEscortTime(int escortTime) {
		this.escortTime = escortTime;
	}

	public int getRobTime() {
		return robTime;
	}

	public void setRobTime(int robTime) {
		this.robTime = robTime;
	}

	public int getLoseProtectTime() {
		return loseProtectTime;
	}

	public void setLoseProtectTime(int loseProtectTime) {
		this.loseProtectTime = loseProtectTime;
	}

	public int getWinProtectTime() {
		return winProtectTime;
	}

	public void setWinProtectTime(int winProtectTime) {
		this.winProtectTime = winProtectTime;
	}

	public List<int[]> getEscortBornPosList() {
		return escortBornPosList;
	}

	public void setEscortBornPosList(List<int[]> escortBornPosList) {
		this.escortBornPosList = escortBornPosList;
	}

	public List<int[]> getRobBornPosList() {
		return robBornPosList;
	}

	public void setRobBornPosList(List<int[]> robBornPosList) {
		this.robBornPosList = robBornPosList;
	}

	public int getRoadBlockDistance() {
		return roadBlockDistance;
	}

	public void setRoadBlockDistance(int roadBlockDistance) {
		this.roadBlockDistance = roadBlockDistance;
	}

	public int getRoadBlockOdds() {
		return roadBlockOdds;
	}

	public void setRoadBlockOdds(int roadBlockOdds) {
		this.roadBlockOdds = roadBlockOdds;
	}

	public int getSafeStageId() {
		return safeStageId;
	}

	public void setSafeStageId(int safeStageId) {
		this.safeStageId = safeStageId;
	}

	public int getPvpStageId() {
		return pvpStageId;
	}

	public void setPvpStageId(int pvpStageId) {
		this.pvpStageId = pvpStageId;
	}

	public int getStageId() {
		return stageId;
	}

	public void setStageId(int stageId) {
		this.stageId = stageId;
	}

	public Map<Integer, Integer> getRobedStarMap() {
		return robedStarMap;
	}

	public void setRobedStarMap(Map<Integer, Integer> robedStarMap) {
		this.robedStarMap = robedStarMap;
	}

	public int getRobMinStar() {
		return robMinStar;
	}

	public void setRobMinStar(int robMinStar) {
		this.robMinStar = robMinStar;
	}


	public String getRobedStar() {
		return robedStar;
	}

	public void setRobedStar(String robedStar) {
		this.robedStar = robedStar;
	}

	public String getRobAward() {
		return robAward;
	}

	public void setRobAward(String robAward) {
		this.robAward = robAward;
	}

	public int getEscortPathTime() {
		return escortPathTime;
	}

	public void setEscortPathTime(int escortPathTime) {
		this.escortPathTime = escortPathTime;
	}

	public String getEscortStartAward() {
		return escortStartAward;
	}

	public void setEscortStartAward(String escortStartAward) {
		this.escortStartAward = escortStartAward;
	}



	public Map<Integer, Integer> getRobAwardMap() {
		return robAwardMap;
	}

	public void setRobAwardMap(Map<Integer, Integer> robAwardMap) {
		this.robAwardMap = robAwardMap;
	}

	public Map<Integer, int[]> getEscortStartAwardMap() {
		return escortStartAwardMap;
	}

	public void setEscortStartAwardMap(Map<Integer, int[]> escortStartAwardMap) {
		this.escortStartAwardMap = escortStartAwardMap;
	}

	public int getRobShowAward() {
		return robShowAward;
	}

	public void setRobShowAward(int robShowAward) {
		this.robShowAward = robShowAward;
	}

	public int getEscortShowAward() {
		return escortShowAward;
	}

	public void setEscortShowAward(int escortShowAward) {
		this.escortShowAward = escortShowAward;
	}

	public int getRobBaseAward() {
		return robBaseAward;
	}

	public void setRobBaseAward(int robBaseAward) {
		this.robBaseAward = robBaseAward;
	}

	public int getRobBaseAwardMaxTime() {
		return robBaseAwardMaxTime;
	}

	public void setRobBaseAwardMaxTime(int robBaseAwardMaxTime) {
		this.robBaseAwardMaxTime = robBaseAwardMaxTime;
	}
	
	
	
}
