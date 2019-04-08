package com.stars.modules.elitedungeon;

import com.stars.modules.elitedungeon.prodata.EliteDungeonRobotVo;
import com.stars.modules.elitedungeon.prodata.EliteDungeonVo;
import com.stars.modules.elitedungeon.userdata.ElitePlayerImagePo;

import java.util.*;

/**
 * Created by gaopeidian on 2017/3/8.
 */
public class EliteDungeonManager {
	private static long uidCreator;
	
	public static byte minTeamCount = 0;
    public static byte maxTeamCount = 0;
    public static int delayTime = 0;
	
    public static Map<Integer, EliteDungeonVo> eliteDungeonVoMap;
    
    public static Map<Integer , Integer> rewardTimesMap = new LinkedHashMap();
    public static Map<Integer , Integer> helpTimesMap = new LinkedHashMap();
    
    public static List<int[]> timerRandomList = new ArrayList<>();//匹配机器人  时间随机区间
    public static List<EliteDungeonRobotVo> robotList = new ArrayList<>();//机器人信息
    
    public static List<String> firstName = new ArrayList<String>();
    public static List<String> secondName = new ArrayList<String>();
    public static List<String> thirdName = new ArrayList<String>();
    
    public static List<ElitePlayerImagePo> playerImageList = new ArrayList<>();//玩家镜像信息
    public static Map<Integer, List<ElitePlayerImagePo>> stagePlayerMap = new HashMap<Integer, List<ElitePlayerImagePo>>();//副本玩家镜像信息
    public static int Max_Robot_StageId = 0;
    public static int Min_Robot_StageId = 0;
    public static int MATCH_TIME = 30;
    public static int KEEP_MAX_NUM = 200;
   
    public static void setEliteDungeonVoMap(Map<Integer, EliteDungeonVo> value) {
    	eliteDungeonVoMap = value;
    }

    public static Map<Integer, EliteDungeonVo> getEliteDungeonVoMap() {
        return eliteDungeonVoMap;
    }
    
    public static EliteDungeonVo getEliteDungeonVo(int eliteId){
    	return eliteDungeonVoMap.get(eliteId);
    }
    
    public static int getRewardTimesByLevel(int level){
    	Set<Map.Entry<Integer , Integer>> entrySet = rewardTimesMap.entrySet();
    	int times = 0;
    	for (Map.Entry<Integer , Integer> entry : entrySet) {
    		times = entry.getValue();
			if (entry.getKey() >= level) {
				return times;
			}
		}
    	
    	return times;
    }
    
    public static int getHelpTimesByLevel(int level){
    	Set<Map.Entry<Integer , Integer>> entrySet = helpTimesMap.entrySet();
    	int times = 0;
    	for (Map.Entry<Integer , Integer> entry : entrySet) {
    		times = entry.getValue();
			if (entry.getKey() >= level) {
				return times;
			}
		}
    	
    	return times;
    }
    
    public static String randomName(){
		int maxIndex = firstName.size();
		Random r = new Random();
		int index = r.nextInt(maxIndex);
		String firtName = firstName.get(index);
		maxIndex = secondName.size();
		index = r.nextInt(maxIndex);
		String name = firtName + secondName.get(index);
		index = r.nextInt(100);
		if (index >= 20) {
			maxIndex = thirdName.size();
			index = r.nextInt(maxIndex);
			name = name + thirdName.get(index);
		}
		return name;
	}
    
    public static synchronized long getUid(){
		long uid = ++uidCreator;
		return uid;
	}
}
