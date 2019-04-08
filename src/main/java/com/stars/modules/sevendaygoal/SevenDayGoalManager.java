package com.stars.modules.sevendaygoal;

import com.stars.modules.sevendaygoal.prodata.SevenDayGoalVo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gaopeidian on 2016/12/13.
 */
public class SevenDayGoalManager {
	private static Map<Integer, SevenDayGoalVo> sevenDayGoalVoMap = null;
	private static Map<Integer , Map<Integer, Map<Integer, SevenDayGoalVo>>> daysGoalsMap = new HashMap<Integer , Map<Integer, Map<Integer, SevenDayGoalVo>>>();
	
	public static void setSevenDayGoalVoMap(Map<Integer, SevenDayGoalVo> map){
		
		Map<Integer , Map<Integer, Map<Integer, SevenDayGoalVo>>> tempDaysGoalsMap = new HashMap<Integer , Map<Integer, Map<Integer, SevenDayGoalVo>>>();
		for (SevenDayGoalVo vo : map.values()) {
			int activityId = vo.getOperateActId();
			int days = vo.getDays();
			Map<Integer, Map<Integer, SevenDayGoalVo>> activityMap = tempDaysGoalsMap.get(activityId);
			if (activityMap == null) {
				activityMap = new HashMap<Integer, Map<Integer,SevenDayGoalVo>>();
				tempDaysGoalsMap.put(activityId, activityMap);
			}
			
			Map<Integer, SevenDayGoalVo> tempMap = activityMap.get(days);
			if (tempMap == null) {
				tempMap = new LinkedHashMap<Integer, SevenDayGoalVo>();
				activityMap.put(days, tempMap);
			}
			tempMap.put(vo.getGoalId(), vo);
		}

		sevenDayGoalVoMap = map;
		daysGoalsMap = tempDaysGoalsMap;
    }
	
	public static Map<Integer, SevenDayGoalVo> getSevenDayGoalVoMap(){
		return sevenDayGoalVoMap;
	}
	
	public static SevenDayGoalVo getSevenDayGoalVo(int goalId){
		return sevenDayGoalVoMap.get(goalId);
	}
	
	public static Map<Integer, SevenDayGoalVo> getDayGoalsVoMap(int activityId , int days){
		Map<Integer, Map<Integer, SevenDayGoalVo>> activityMap = daysGoalsMap.get(activityId);
		if (activityMap != null) {
			return activityMap.get(days);
		}else{
			return null;
		}
	}
	
	public static Map<Integer, Map<Integer, SevenDayGoalVo>> getSevenDayVoMap(int activityId){
		return daysGoalsMap.get(activityId);
	}
	
	public static int getMaxDay(int activityId){
		Map<Integer, Map<Integer, SevenDayGoalVo>> activityMap = daysGoalsMap.get(activityId);
		if (activityMap == null) {
			return 0;
		}
		int maxDay = 0;
		Set<Map.Entry<Integer, Map<Integer, SevenDayGoalVo>>> entrySet = activityMap.entrySet();
		for (Map.Entry<Integer, Map<Integer, SevenDayGoalVo>> entry : entrySet) {
			int day = entry.getKey();
			if (day > maxDay) {
				maxDay = day;
			}
		}
		
		return maxDay;
	}
}
