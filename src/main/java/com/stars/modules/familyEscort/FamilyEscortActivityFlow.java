package com.stars.modules.familyEscort;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.services.ServiceHelper;
import com.stars.services.activities.ActConst;
import com.stars.services.family.FamilyConst;

/**
 * Created by zhaowenshuo on 2017/4/18.
 */
public class FamilyEscortActivityFlow extends ActivityFlow {

    public static final int STEP_PREPARE = 1;
    public static final int STEP_START = 2;
    public static final int STEP_END = 3;
    public static final int STEP_ENDFIGHT = 4;
    public static final int STEP_CLEARUP = 5;

    @Override
    public String getActivityFlowName() {
        return "familyEscort";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        switch (step) {
            case STEP_START_CHECK: // 启动检查
                ServiceHelper.familyActEntryService().setOptions(
                        ActConst.ID_FAMILY_ESORT, FamilyConst.ACT_BTN_MASK_DISPLAY, -1, "");
                if (between(STEP_PREPARE, STEP_START)) {
                    ServiceHelper.familyEscortService().prepare();
                }
                if (between(STEP_START, STEP_END)) {
                	ServiceHelper.familyEscortService().prepare();
                    ServiceHelper.familyEscortService().start();
                }
                break;
            case STEP_PREPARE: // 准备
                ServiceHelper.familyEscortService().prepare();
                break;
            case STEP_START: // 开始
                ServiceHelper.familyEscortService().start();
                break;
            case STEP_END: // 结束
            	ServiceHelper.familyEscortService().escortEnd();
                break;
            case STEP_ENDFIGHT: // 结束
                ServiceHelper.familyEscortService().end();
                break;
            case STEP_CLEARUP: // 清场
                ServiceHelper.familyEscortService().clearup();
                break;
        }
    }
}
