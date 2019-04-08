package com.stars.modules.familyactivities.expedition;

import com.stars.core.activityflow.ActivityFlow;

/**
 * Created by zhaowenshuo on 2016/10/9.
 */
public class FamilyActExpeditionFlow extends ActivityFlow {

    public static final int SELF_STEP_START = 1;
    public static final int SELF_STEP_STOP = 2;

    @Override
    public String getActivityFlowName() {
        return "family.expedition";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        switch (step) {
            case STEP_START_CHECK:
                if (outside(SELF_STEP_STOP, SELF_STEP_START)) {
                    FamilyActExpeditionModule.start();
                }
                break;
            case 1:
                FamilyActExpeditionModule.start();
                break;
            case 2:
                FamilyActExpeditionModule.end();
                break;
        }
    }

}
