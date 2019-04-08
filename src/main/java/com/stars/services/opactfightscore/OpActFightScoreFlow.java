package com.stars.services.opactfightscore;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.services.ServiceHelper;

/**
 * Created by chenkeyu on 2017-03-21 18:39
 */
public class OpActFightScoreFlow extends ActivityFlow {
    @Override
    public String getActivityFlowName() {
        return "opActFightScoreFlow";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        if (step == OperateActivityConstant.FLOW_STEP_NEW_DAY)
            ServiceHelper.opActFightScore()
                    .checkActivityFlowState(OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_NewServerFightScore0));
    }
}
