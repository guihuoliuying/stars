package com.stars.services.opactsceondkill;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.services.ServiceHelper;

/**
 * Created by chenkeyu on 2017-09-20.
 */
public class OpActSecondKillFlow extends ActivityFlow {

    private static final int PERIOD_START = 1;
    private static final int PERIOD_END = 0;

    @Override
    public String getActivityFlowName() {
        return "限时秒杀";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        if (step == STEP_START_CHECK) {

            return;
        }
        if (step % 2 == PERIOD_START) {
            ServiceHelper.opActSecondKillService().updateSceondKillState(true, step + 1);
            return;
        }
        if (step % 2 == PERIOD_END) {
            ServiceHelper.opActSecondKillService().updateSceondKillState(false, step + 1);
            return;
        }

    }
}
