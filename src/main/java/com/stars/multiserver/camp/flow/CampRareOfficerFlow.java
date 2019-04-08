package com.stars.multiserver.camp.flow;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by huwenjun on 2017/6/30.
 */
public class CampRareOfficerFlow extends ActivityFlow {
    public static final int TYPE_INIT = 0;
    public static final int TYPE_RARE_OFFICER_RESET = 1;//稀有官职重置
    public static final int TYPE_DAILY_PROPEROUS_ADD = 2;//每日繁荣度自动繁荣度添加

    @Override
    public String getActivityFlowName() {
        return "阵营稀有官职授予";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        switch (step) {
            case TYPE_INIT: {
                LogUtil.info("阵营定时工作流初始化完成");
            }
            break;
            case TYPE_RARE_OFFICER_RESET: {
                ServiceHelper.campRemoteMainService().grantRareOfficer();
            }
            break;
            case TYPE_DAILY_PROPEROUS_ADD: {
                ServiceHelper.campRemoteMainService().dailyProperousAdd();
            }
            break;
        }
    }
}
