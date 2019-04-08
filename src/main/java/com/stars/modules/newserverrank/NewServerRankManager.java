package com.stars.modules.newserverrank;

import com.stars.modules.newserverrank.prodata.NewServerRankVo;

import java.util.*;

/**
 * Created by gaopeidian on 2016/12/20.
 */
public class NewServerRankManager {
	private static Map<Integer, NewServerRankVo> newServerRankVoMap = null;
	
	//<活动id，该活动的排名奖励配置>
	private static Map<Integer, List<NewServerRankVo>> newServerRankVoListMap = new HashMap<Integer, List<NewServerRankVo>>();
	
	public static void setNewServerRankVoMap(Map<Integer, NewServerRankVo> map){
		newServerRankVoMap = map;
				
		Map<Integer, List<NewServerRankVo>> tempListMap = new HashMap<Integer, List<NewServerRankVo>>();
		for (NewServerRankVo vo : map.values()) {
			int activityId = vo.getOperateActId();
			List<NewServerRankVo> list = tempListMap.get(activityId);
			if (list == null) {
				list = new ArrayList<NewServerRankVo>();
				tempListMap.put(activityId, list);
			}
			list.add(vo);
		}
		//排列一下
		for (List<NewServerRankVo> list : tempListMap.values()) {
			Collections.sort(list);
		}
		
		newServerRankVoListMap = tempListMap;
    }
	
	public static int getRankType(int activityId){
		List<NewServerRankVo> list = getActivityRankVoList(activityId);
		if (list != null && list.size() > 0) {
			NewServerRankVo vo = list.get(0);
			return vo.getType();
		}
		
		return -1;
	}
	
	public static int getMaxRewardRank(int activityId){
		List<NewServerRankVo> list = getActivityRankVoList(activityId);
		if (list != null && list.size() > 0) {
			int size = list.size();
			NewServerRankVo vo = list.get(size - 1);
			return vo.getRankEnd();
		}
		
		return 0;
	}
	
	public static Map<Integer, NewServerRankVo> getNewServerRankVoMap(){
		return newServerRankVoMap;
	}
	
	public static List<NewServerRankVo> getActivityRankVoList(int activityId){
		return newServerRankVoListMap.get(activityId);
	}
	
	public static NewServerRankVo getNewServerRankVo(int id){
		return newServerRankVoMap.get(id);
	}
}
