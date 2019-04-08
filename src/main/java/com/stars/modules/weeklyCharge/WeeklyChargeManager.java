package com.stars.modules.weeklyCharge;

import com.stars.modules.weeklyCharge.prodata.WeeklyChargeVo;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by chenxie on 2017/5/5.
 */
public class WeeklyChargeManager {

    public static Map<Integer, WeeklyChargeVo> weeklyChargeInfoMap;

    public static int MAIL_ID = 25005;

    public static WeeklyChargeVo getWeeklyChargeInfoById(int id){
        if(StringUtil.isEmpty(weeklyChargeInfoMap)) return null;
        return weeklyChargeInfoMap.get(id);
    }


}
