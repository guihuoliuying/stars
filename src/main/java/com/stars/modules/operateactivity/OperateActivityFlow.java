package com.stars.modules.operateactivity;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.modules.operateactivity.event.OperateActivityFlowEvent;
import com.stars.services.ServiceHelper;

/**
 * 运营活动定时器控制
 * Created by gaopeidian on 2016/11/24.
 */
public class OperateActivityFlow extends ActivityFlow {
    @Override
    public String getActivityFlowName() {
        return "operateactivity";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
    	if (step == OperateActivityConstant.FLOW_STEP_NEW_DAY) {
    		ServiceHelper.roleService().noticeAll(new OperateActivityFlowEvent(OperateActivityConstant.FLOW_STEP_NEW_DAY));
    		//ServiceHelper.operateActivityService().checkOpActivities(false);
		}else if (step == OperateActivityConstant.FLOW_STEP_MINUTE) {
			ServiceHelper.operateActivityService().checkOpActivities(false);
		}
    }
}
