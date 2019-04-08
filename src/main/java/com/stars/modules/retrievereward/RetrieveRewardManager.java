package com.stars.modules.retrievereward;

import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.prodata.DailyVo;
import com.stars.modules.retrievereward.prodata.RetrieveRewardVo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/12/13.
 */
public class RetrieveRewardManager {
	private static Map<Integer, RetrieveRewardVo> retrieveRewardVoMap = null;
	
	public static void setRetrieveRewardVoMap(Map<Integer, RetrieveRewardVo> map){
		retrieveRewardVoMap = map;
    }
	
	public static Map<Integer, RetrieveRewardVo> getRetrieveRewardVoMap(){
		return retrieveRewardVoMap;
	}
	
	public static RetrieveRewardVo getRetrieveRewardVo(int rewardId){
		return retrieveRewardVoMap.get(rewardId);
	}
	
	public static Map<Short, Integer> getDefaultPreDailyRecordMap(){
		Map<Short, Integer> recordMap = new HashMap<Short, Integer>();
		
		Map<Short, DailyVo> dailyVos = DailyManager.getDailyVoMap();
		for (DailyVo dailyVo : dailyVos.values()) {
			recordMap.put(dailyVo.getDailyid(), -1);
		}
		
		return recordMap;
	}
}
