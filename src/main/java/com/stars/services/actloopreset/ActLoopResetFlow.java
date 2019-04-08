package com.stars.services.actloopreset;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.services.ServiceHelper;

/**
 * Created by huwenjun on 2017/11/28.
 */
public class ActLoopResetFlow extends ActivityFlow {
    @Override
    public String getActivityFlowName() {
        return "活动周重置循环服务";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        switch (step) {
            case 1: {
                ServiceHelper.actLoopResetService().resetAndLoop();
            }
            break;
        }
    }
}
