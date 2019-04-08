package com.stars.multiserver.daily5v5;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.services.ServiceHelper;

public class Daily5v5MatchFlow extends ActivityFlow{
	
	private static final int start = 1;
	private static final int end = 2;
	private static boolean isStarted = false;

	@Override
	public String getActivityFlowName() {
		return "daily_5v5";
	}

	@Override
	public void onTriggered(int step, boolean isRedo) {
		int currentStep = (step - 1) % 2 + 1;
		switch (currentStep) {
		case STEP_START_CHECK:
			if (between(1, 2)||between(3, 4)) {
				ServiceHelper.daily5v5MatchService().activityStart();
				isStarted = true;
			}
			break;
		case start:
			ServiceHelper.daily5v5MatchService().activityStart();
			isStarted = true;
			break;
		case end:
			ServiceHelper.daily5v5MatchService().activityEnd();
			isStarted = false;
			break;
		}
	}
	
	public static boolean isStarted(){
		return isStarted;
	}

}
