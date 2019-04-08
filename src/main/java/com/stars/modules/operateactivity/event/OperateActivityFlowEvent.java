package com.stars.modules.operateactivity.event;

import com.stars.core.event.Event;

/**
 * Created by gaopeidian on 2016/12/7.
 */
public class OperateActivityFlowEvent extends Event {
	private int stepType;
	
    public OperateActivityFlowEvent(int stepType) {
    	this.stepType = stepType;
    }
    
    public int getStepType(){
    	return this.stepType;
    }
}
