package com.stars.services.newofflinepvp;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.services.ServiceHelper;

/**
 * Created by chenkeyu on 2017-03-13 16:43
 */
public class NewOfflinePvpFlow extends ActivityFlow {
    private static final int closeOfflinePvp = 1;
    private static final int sendRankAward = 3;
    private static final int openOfflinePvp = 2;

    @Override
    public String getActivityFlowName() {
        return "offlinepvp";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        switch (step) {
            case STEP_START_CHECK:
                if (between(1, 2)) {
                    ServiceHelper.newOfflinePvpService().closeOfflinePvp();
                } else {
                    ServiceHelper.newOfflinePvpService().openOfflinePvp();
                }
                break;
            case closeOfflinePvp:
                ServiceHelper.newOfflinePvpService().closeOfflinePvp();
                break;
            case sendRankAward:
                ServiceHelper.newOfflinePvpService().sendRankAward();
                break;
            case openOfflinePvp:
                ServiceHelper.newOfflinePvpService().openOfflinePvp();
                break;
            default:
                break;
        }
    }
}
