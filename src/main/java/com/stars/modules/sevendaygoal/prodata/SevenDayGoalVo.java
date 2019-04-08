package com.stars.modules.sevendaygoal.prodata;

/**
 * Created by gaopeidian on 2016/12/17.
 */
public class SevenDayGoalVo implements Comparable<SevenDayGoalVo>{
    private int goalId;
    private int operateActId;
    private int days;
    private String name;
    private String condition;
    private String desc;
    private String reward;
    private int quality;
    private int numlimit;
    
    //内存数据
    private int goalType = -1;
    private int goalNum = 0;
    //private Map<Integer, Integer> rewardMap = new HashMap<Integer, Integer>();
    private int groupId = 0;
    
    public int getGoalId() {
        return goalId;
    }

    public void setGoalId(int value) {
        this.goalId = value;
    }
    
    public int getOperateActId() {
        return operateActId;
    }

    public void setOperateActId(int value) {
        this.operateActId = value;
    }
    
    public int getDays() {
        return days;
    }

    public void setDays(int value) {
        this.days = value;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }
    
    public String getCondition() {
        return condition;
    }

    public void setCondition(String value) {
        this.condition = value;
        
        if (condition == null || condition.equals("") || condition.equals("0")) {
			return;
		}
		String sts[] = condition.split("\\+");
		if (sts.length >= 2) {
			if (!sts[0].equals("")) {
				goalType = Integer.parseInt(sts[0]);
			}
			if (!sts[1].equals("")) {
				goalNum = Integer.parseInt(sts[1]);
			}
		}
    }
    
    public String getDesc() {
        return desc;
    }

    public void setDesc(String value) {
        this.desc = value;
    }
    
    public String getReward() {
        return reward;
    }

    public void setReward(String value) {
        this.reward = value;
        
        if (reward == null || reward.equals("") || reward.equals("0")) {
			return;
		}
        
//		String[] sts = reward.split("\\,");
//		String[] ts;
//		for(String tmp : sts){
//			ts = tmp.split("\\+");
//			if (ts.length >= 2) {
//				rewardMap.put(Integer.parseInt(ts[0]), Integer.parseInt(ts[1]));
//			}
//		}
        
        this.groupId = Integer.parseInt(this.reward);
    }
    
    public int getQuality() {
        return quality;
    }

    public void setQuality(int value) {
        this.quality = value;
    }
    
    public int getNumlimit() {
        return numlimit;
    }

    public void setNumlimit(int value) {
        this.numlimit = value;
    }
    
    
    public int getGoalType() {
        return goalType;
    }
    
    public int getGoalNum() {
        return goalNum;
    }
    
//    public Map<Integer, Integer> getRewardMap(){
//    	return rewardMap;
//    }
    
    public int getGroupId(){
    	return this.groupId;
    }
    
    /**
     * 按活动goalId从小到大排
     */
	@Override
	public int compareTo(SevenDayGoalVo o) {
		if (this.getGoalId() < o.getGoalId()) {
			return -1;
		}else if (this.getGoalId() > o.getGoalId()) {
			return 1;
		}else{
			return 0;
		}
	}
}
