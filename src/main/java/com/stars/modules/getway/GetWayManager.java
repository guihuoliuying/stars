package com.stars.modules.getway;

import com.stars.modules.getway.prodata.GetWayVo;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class GetWayManager {

    public static Map<Integer, GetWayVo> getWayVoMap;

    public static GetWayVo getGetWayVo(int getWayId) {
        return getWayVoMap.get(getWayId);
    }

}
