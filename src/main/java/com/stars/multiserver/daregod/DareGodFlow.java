package com.stars.multiserver.daregod;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.modules.MConst;
import com.stars.multiserver.MultiServerHelper;
import com.stars.services.ServiceHelper;

/**
 * Created by chenkeyu on 2017-08-24.
 */
public class DareGodFlow extends ActivityFlow {
    public static final int START = 1;
    public static final int STOP = 2;
    public static final int DO_SOMETHING = 3;

    @Override
    public String getActivityFlowName() {
        return MConst.DareGod;
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        switch (step) {
            case STEP_START_CHECK:
//                if (between(START, STOP)) {
//                    ServiceHelper.dareGodService().updateFightState(MultiServerHelper.getServerId(), true);
//                } else {
//                    ServiceHelper.dareGodService().updateFightState(MultiServerHelper.getServerId(), false);
//                }
                break;
            case START:
                ServiceHelper.dareGodService().updateFightState(MultiServerHelper.getServerId(), true);
                break;
            case STOP:
                ServiceHelper.dareGodService().updateFightState(MultiServerHelper.getServerId(), false);
                break;
            case DO_SOMETHING:
                ServiceHelper.dareGodService().onDaliyReset(MultiServerHelper.getServerId());
                break;
            default:
                break;
        }
    }
}
