package com.stars.modules.ride;

import com.stars.modules.ride.prodata.RideAwakeLvlVo;
import com.stars.modules.ride.prodata.RideInfoVo;
import com.stars.modules.ride.prodata.RideLevelVo;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/19.
 */
public class RideManager {

    public static Map<Integer, RideInfoVo> rideInfoVoMap;
    public static Map<Integer, Map<Integer, RideLevelVo>> rideLevelVoMap; // rideId -> (stage_level -> RideLevelVo)
    public static Map<Integer, RideLevelVo> rideLevelIdMap;// rideId -> (id -> RideLevelVo)
    public static Map<Integer, Map<Integer, RideAwakeLvlVo>> rideAwakeLevelVoMap; // rideId -> (awakeLevel -> RideAwakeLvlVo)
    
    public static RideInfoVo getRideInfoVo(int rideId) {
        return rideInfoVoMap.get(rideId);
    }

    public static RideLevelVo getRideLevelVo(int stageLevel, int level) {
    	Map<Integer, RideLevelVo> stageMap = rideLevelVoMap.get(stageLevel);
    	if (stageMap == null) return null;
        return stageMap.get(level);
    }
    
    /**
     * 根据等级Id获取坐骑等级对象
     * @param id
     * @return
     */
    public static RideLevelVo getRideLvById(int id) {
    	return rideLevelIdMap.get(id);
    }

    public static RideAwakeLvlVo getRideAwakeLvlVo(int rideId, int awakeLevel) {
        if (rideAwakeLevelVoMap.containsKey(rideId)) {
            return rideAwakeLevelVoMap.get(rideId).get(awakeLevel);
        }
        return null;
    }

}
