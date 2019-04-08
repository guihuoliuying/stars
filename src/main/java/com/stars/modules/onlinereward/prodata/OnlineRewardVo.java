package com.stars.modules.onlinereward.prodata;

/**
 * Created by gaopeidian on 2016/12/6.
 */
public class OnlineRewardVo {
    private int onlinerewardid;
    private int minute;
    private String reward;
    private String desc;
    
    //private Map<Integer, Integer> rewardMap = new HashMap<Integer, Integer>();
    private int groupId = 0;
    
    public int getOnlinerewardid() {
        return onlinerewardid;
    }

    public void setOnlinerewardid(int value) {
        this.onlinerewardid = value;
    }
    
    public int getMinute(){
        return minute;
    }

    public void setMinute(int value) {
       this.minute = value;
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
    
    public String getDesc() {
        return desc;
    }

    public void setDesc(String value) {
        this.desc = value;
    }
    
    
//    public Map<Integer, Integer> getRewardMap(){
//    	return rewardMap;
//    }
    
    public int getGroupId(){
    	return this.groupId;
    }
}
