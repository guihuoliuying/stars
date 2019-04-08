package com.stars.modules.newdailycharge;

import com.stars.modules.newdailycharge.prodata.NewDailyChargeInfo;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-07-10.
 */
public class NewDailyChargeManager {
    public static Map<Integer, NewDailyChargeInfo> newDailyChargeInfoMap;
    public static final int EMAIL_ID = 25008;

    public static Map<Integer, NewDailyChargeInfo> getNewDailyChargeInfoMap() {
        return newDailyChargeInfoMap;
    }

    public static void setNewDailyChargeInfoMap(Map<Integer, NewDailyChargeInfo> newDailyChargeInfoMap) {
        NewDailyChargeManager.newDailyChargeInfoMap = newDailyChargeInfoMap;
    }
}
