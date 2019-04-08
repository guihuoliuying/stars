package com.stars.modules.masternotice;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.modules.masternotice.event.MasterNoticeOnClockEvent;
import com.stars.services.ServiceHelper;

/**
 * 皇榜悬赏整点控制
 * Created by gaopeidian on 2016/11/24.
 */
public class MasterNoticeFlow extends ActivityFlow {
    @Override
    public String getActivityFlowName() {
        return "masternotice";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
    	if (step == MasterNoticeConstant.MASTER_NOTICE_FLOW_STEP) {
    		ServiceHelper.roleService().noticeAll(new MasterNoticeOnClockEvent());
		}
    }
}
