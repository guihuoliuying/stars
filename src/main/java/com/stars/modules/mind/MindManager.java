package com.stars.modules.mind;

import com.stars.modules.mind.prodata.MindLevelVo;
import com.stars.modules.mind.prodata.MindVo;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 心法管理器;
 * Created by gaopeidian on 2016/9/21.
 */
public class MindManager {
	private static Map<Integer, MindVo> mindVoMap = null;
	private static Map<String, MindLevelVo> mindLevelVoMap = null;
	
	public static String getMindLevelKey(int mindId , int mindLevel){
		return mindId + "_" + mindLevel;
	}
	
	public static void setMindVoMap(Map<Integer, MindVo> map){
		mindVoMap = map;
    }
	
	public static Map<Integer, MindVo> getMindVoMap(){
		return mindVoMap;
	}
	
	public static void setMindLevelVoMap(Map<String, MindLevelVo> map){
		mindLevelVoMap = map;
    }
	
	public static Map<String, MindLevelVo> getMindLevelVoMap(){
		return mindLevelVoMap;
	}
	
	public static MindVo getMindVo(int mindId){
		if (mindVoMap.containsKey(mindId)) {
			return mindVoMap.get(mindId);
		}
		return null;
	}
	
	public static MindLevelVo getMindLevelVo(int mindId , int mindLevel){
		String key = getMindLevelKey(mindId, mindLevel);
		if (mindLevelVoMap.containsKey(key)) {
			return mindLevelVoMap.get(key);
		}
		return null;
	}
	
	public static int getMindMaxLevel(int mindId){
		int maxLevel = 0;
		if (mindLevelVoMap != null) {
			Set<Entry<String , MindLevelVo>> set = mindLevelVoMap.entrySet();
			for (Entry<String , MindLevelVo> entry : set){
				MindLevelVo mindLevelVo = entry.getValue();
				if (mindLevelVo.getMindId() == mindId) {
					int tempLevel = mindLevelVo.getLevel();
					if (tempLevel > maxLevel) {
						maxLevel = tempLevel;
					}				   
				}
			}		
		}
		
		return maxLevel;
	}
	
	@SuppressWarnings("unused")
	public static void test(){
		int mindId = 1001;
		int mindLevel0 = 0;
		int mindLevel1 = 1;
		MindVo mindVo = MindManager.getMindVo(mindId);
		MindLevelVo mindlevelVo0 = MindManager.getMindLevelVo(mindId, mindLevel0);
		MindLevelVo mindlevelVo1 = MindManager.getMindLevelVo(mindId, mindLevel1);
		int t = 0;
	}
}
