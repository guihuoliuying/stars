package com.stars.multiserver.daregod;

import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.modules.data.DataManager;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-08-23.
 */
public class DareGodConst {
    public static final int GETED = 1;//已领取
    public static final int UN_GETED = 0;//未领取
    public static final int CANT_GET = -1;//不可领取

    public static final int MAX_RANK = 50;

    public static final int MAX_RANK_FOR_FIGHT = 10;

    public static final int RANK_EMAIL_ID = 28101;
    public static final int UN_GETAWARD_EMAIL_ID = 28102;

    public static long getTimeL(int step, int actConst) {
        long time = 0;
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(actConst);
        if (StringUtil.isNotEmpty(flowMap) || flowMap.containsKey(step)) {
            time = ActivityFlowUtil.getTimeInMillisByCronExpr(flowMap.get(step));
        }
        return time;
    }
}
