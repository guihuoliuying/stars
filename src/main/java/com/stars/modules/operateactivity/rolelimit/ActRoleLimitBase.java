package com.stars.modules.operateactivity.rolelimit;

import java.util.HashMap;
import java.util.Map;

/**
 * 活动开发条件基类;
 * Created by gaopeidian on 2016/12/6.
 */
public class ActRoleLimitBase  {
    public static final byte LevelLimitType = 1;
    public static final byte SystemLimitType = 2;
    public static final byte OpenServerDayLimitType = 3;
    public static final byte DateLimitType = 4;
    
    protected int type;
    
    public int getType(){
    	return this.type;
    }
    
    public static Map<Integer, ActRoleLimitBase> getOpenLimitsByStr(String str){
    	Map<Integer, ActRoleLimitBase> retMap = new HashMap<Integer, ActRoleLimitBase>();
    	if (str == null || str.equals("") || str.equals("0")){
    		return null;
    	}
    	
    	String sts[] = str.split("\\,");
    	for (String unitStr : sts) {
			ActRoleLimitBase actShowLimit = newActOpenLimitBaseByStr(unitStr);
			if (actShowLimit != null) {
				retMap.put(actShowLimit.getType(), actShowLimit);
			}
		}
    	
    	return retMap;
    }
    
    public static ActRoleLimitBase newActOpenLimitBaseByStr(String str){
    	if (str == null || str.equals("") || str.equals("0")){
    		return null;
    	}
    	int limitType = -1;
    	String sts[] = str.split("\\+");
    	if (sts.length > 0) {
  			if (!sts[0].equals("")) {
  				limitType = Integer.parseInt(sts[0]);
  			}	
  		}
    	
    	switch (limitType) {
		case LevelLimitType:
			return new ActLevelLimit(str); 
		case SystemLimitType:
			return new ActSystemLimit(str); 
		case OpenServerDayLimitType:
			return new ActOpenServerDayLimit(str); 
		case DateLimitType:
			return new ActDateLimit(str); 
		default:
			return null;
		}
    }
}
