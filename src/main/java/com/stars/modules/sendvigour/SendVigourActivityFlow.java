package com.stars.modules.sendvigour;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.modules.sendvigour.event.SendVigourActEvent;
import com.stars.services.ServiceHelper;

import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by gaopeidian on 2017/3/29.
 */
public class SendVigourActivityFlow extends ActivityFlow {
    @Override
    public String getActivityFlowName() {
        return "sendvigour";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
    	if (step == ActivityFlow.STEP_START_CHECK) {
			startCheck();
		}else if (step % 2 == 1) {//step为奇数，开始领取活动
			start(step);
		}else if (step % 2 == 0) {//step为偶数，结束领取活动
			end(step);
		}
    }   
    
    private void startCheck(){
    	Set<Entry<Integer, Integer>> entrySet = SendVigourManager.STEP_MAP.entrySet();
    	for (Entry<Integer, Integer> entry : entrySet) {
    		int startStep = entry.getKey();
			int endStep = entry.getValue();
			if (startStep % 2 == 1 && endStep % 2 == 0 && between(startStep, endStep)) {
				ServiceHelper.sendVigourService().setCurStepId(startStep);
			}
		}
    }
    
    private void start(int step){
    	ServiceHelper.sendVigourService().setCurStepId(step);
    	ServiceHelper.roleService().noticeAll(new SendVigourActEvent(SendVigourActEvent.Event_Start, step));
    }
    
    private void end(int step){
    	ServiceHelper.sendVigourService().setCurStepId(-1);
    	ServiceHelper.roleService().noticeAll(new SendVigourActEvent(SendVigourActEvent.Event_End, -1));
    }
}
