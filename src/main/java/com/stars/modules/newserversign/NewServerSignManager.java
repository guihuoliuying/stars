package com.stars.modules.newserversign;

import com.stars.modules.newserversign.prodata.NewServerSignVo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gaopeidian on 2016/12/22.
 */
public class NewServerSignManager {
	private static Map<Integer, NewServerSignVo> newServerSignVoMap = null;

	public static Set<String> FIRST_TEST_REWARD_SET;
	
	//<活动id，该活动的排名奖励配置>
	private static Map<Integer, Map<Integer, NewServerSignVo>> activityMap = new HashMap<Integer, Map<Integer, NewServerSignVo>>();
	
	public static void setNewServerSignVoMap(Map<Integer, NewServerSignVo> map){
		newServerSignVoMap = map;
		
		Map<Integer, Map<Integer, NewServerSignVo>> tempActivityMap = new HashMap<Integer, Map<Integer, NewServerSignVo>>();
		for (NewServerSignVo vo : map.values()) {
			int activityId = vo.getOperateActId();
			Map<Integer, NewServerSignVo> tempMap = tempActivityMap.get(activityId);
			if (tempMap == null) {
				tempMap = new HashMap<Integer, NewServerSignVo>();
				tempActivityMap.put(activityId, tempMap);
			}
			tempMap.put(vo.getNewServerSignId(), vo);
		}
		
		activityMap = tempActivityMap;
    }
	
	public static Map<Integer, NewServerSignVo> getNewServerSignVoMap(){
		return newServerSignVoMap;
	}
	
	public static Map<Integer, NewServerSignVo> getActivityVosMap(int activityId){
		return activityMap.get(activityId);
	}
	
	public static NewServerSignVo getNewServerSignVo(int id){
		return newServerSignVoMap.get(id);
	}
	
	public static int getMaxDay(int activityId){
		Map<Integer, NewServerSignVo> map = activityMap.get(activityId);
		if (map == null) {
			return 0;
		}
		int maxDay = 0;
		Set<Map.Entry<Integer, NewServerSignVo>> entrySet = map.entrySet();
		for (Map.Entry<Integer, NewServerSignVo> entry : entrySet) {
			int day = entry.getValue().getDays();
			if (day > maxDay) {
				maxDay = day;
			}
		}
		
		return maxDay;
	}
}
