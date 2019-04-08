package com.stars.modules.bravepractise;

import com.stars.modules.bravepractise.prodata.BraveInfoVo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/11/16.
 */
public class BravePractiseManager {
	public static int bravePractiseCount = 0;
	
	private static Map<Integer, BraveInfoVo> braveInfoVoMap = null;
	
	public static void setBraveInfoVoMap(Map<Integer, BraveInfoVo> map){
		braveInfoVoMap = map;
    }
	
	public static Map<Integer, BraveInfoVo> getBraveInfoVoMap(){
		return braveInfoVoMap;
	}
	
	public static BraveInfoVo getBraveInfoVo(int braveInfoId){
		if (braveInfoVoMap.containsKey(braveInfoId)) {
			return braveInfoVoMap.get(braveInfoId);
		}
		return null;
	}
	
	public static Map<Integer, BraveInfoVo> getBraveInfosByLevel(int level){
		Map<Integer, BraveInfoVo> retMap = new HashMap<Integer, BraveInfoVo>();
		
		if (braveInfoVoMap != null) {
			for (BraveInfoVo braveInfoVo : braveInfoVoMap.values()) {
				if (level >= braveInfoVo.getMinLevel() && level <= braveInfoVo.getMaxLevel()) {
    				retMap.put(braveInfoVo.getBraveId(), braveInfoVo);
    			}
			}
		}
		
		return retMap;
	}
}
