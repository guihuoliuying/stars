package com.stars.modules.sendvigour.event;

import com.stars.core.event.Event;

/**
 * Created by gaopeidian on 2017/3/30.
 */
public class SendVigourActEvent extends Event {
	public static final byte Event_Start = 0;
	public static final byte Event_End = 1;
	
	private byte eventType;
	private int stepId;
	
    public SendVigourActEvent(byte eventType , int stepId) {
    	this.eventType = eventType;
        this.stepId = stepId;
    }
    
    public byte getEventType(){
    	return this.eventType;
    }
    
    public int getStepId(){
    	return this.stepId;
    }
}
