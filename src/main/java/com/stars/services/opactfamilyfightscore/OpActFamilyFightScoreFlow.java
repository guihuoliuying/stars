package com.stars.services.opactfamilyfightscore;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.services.ServiceHelper;

/**
 * Created by chenkeyu on 2017-03-21 17:43
 */
public class OpActFamilyFightScoreFlow extends ActivityFlow {
    @Override
    public String getActivityFlowName() {
        return "opActFamilyFightScoreFlow";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        if (step == OperateActivityConstant.FLOW_STEP_NEW_DAY) {
            ServiceHelper.opActFamilyFightScore()
                    .checkActivityFlowState(OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_NewServerFamilyFightScore));
        }
    }
}
