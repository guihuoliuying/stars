package com.stars.modules.dailyCharge;

import com.stars.modules.dailyCharge.prodata.DailyChargeInfo;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by wuyuxing on 2017/3/29.
 */
public class DailyChargeManager {

    public static Map<Integer, DailyChargeInfo> DailyChargeInfoMap;

    public static int MAIL_ID = 26007;

    public static DailyChargeInfo getDailyChargeInfoById(int id){
        if(StringUtil.isEmpty(DailyChargeInfoMap)) return null;
        return DailyChargeInfoMap.get(id);
    }

}
