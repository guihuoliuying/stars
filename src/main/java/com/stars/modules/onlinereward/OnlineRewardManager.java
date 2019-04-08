package com.stars.modules.onlinereward;

import com.stars.modules.onlinereward.prodata.OnlineRewardVo;

import java.util.Map;

/**
 * Created by gaopeidian on 2016/12/6.
 */
public class OnlineRewardManager {
	private static Map<Integer, OnlineRewardVo> onlineRewardVoMap = null;
	
	public static void setOnlineRewardVoMap(Map<Integer, OnlineRewardVo> map){
		onlineRewardVoMap = map;
    }
	
	public static Map<Integer, OnlineRewardVo> getOnlineRewardVoMap(){
		return onlineRewardVoMap;
	}
	
	public static OnlineRewardVo getOnlineRewardVo(int rewardId){
		return onlineRewardVoMap.get(rewardId);
	}
}
