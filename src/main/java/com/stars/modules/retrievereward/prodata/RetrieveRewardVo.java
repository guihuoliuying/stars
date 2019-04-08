package com.stars.modules.retrievereward.prodata;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/12/6.
 */
public class RetrieveRewardVo {
	public static final byte RewardTypeGoldRetrieve = 1;
	public static final byte RewardTypeDiamondRetrieve = 2;
	
    private int retrieveRewardId;
    private int count;
    private byte type;
    private short dailyid;
    private String cost;
    private String reward;
    
    //内存数据
    private Map<Integer, Integer> costMap = new HashMap<Integer, Integer>();
    //private Map<Integer, Integer> rewardMap = new HashMap<Integer, Integer>();
    private int groupId = 0;
    
    public int getRetrieveRewardId() {
        return retrieveRewardId;
    }

    public void setRetrieveRewardId(int value) {
        this.retrieveRewardId = value;
    }
    
    public int getCount(){
        return count;
    }

    public void setCount(int value) {
       this.count = value;
    }
    
    public byte getType(){
    	return type;
    }
    
    public void setType(byte value) {
        this.type = value;
    }
    
    public short getDailyid(){
    	return dailyid;
    }
    
    public void setDailyid(short value) {
        this.dailyid = value;
    }
    
    public String getCost() {
        return cost;
    }

    public void setCost(String value) {
        this.cost = value;
        
        if (cost == null || cost.equals("") || cost.equals("0")) {
			return;
		}
		String[] sts = cost.split("\\,");
		String[] ts;
		for(String tmp : sts){
			ts = tmp.split("\\+");
			if (ts.length >= 2) {
				costMap.put(Integer.parseInt(ts[0]), Integer.parseInt(ts[1]));
			}
		}
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
    
    public Map<Integer, Integer> getCostMap(){
    	return costMap;
    }
    
//    public Map<Integer, Integer> getRewardMap(){
//    	return rewardMap;
//    }
    
    public int getGroupId(){
    	return this.groupId;
    }
}
