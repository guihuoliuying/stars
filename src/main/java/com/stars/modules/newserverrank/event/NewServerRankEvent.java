package com.stars.modules.newserverrank.event;

import com.stars.core.event.Event;

/**
 * Created by gaopeidian on 2016/12/21.
 */
public class NewServerRankEvent extends Event {
	public static final byte Event_Type_Open_Activity = 0;
	public static final byte Event_Type_Close_Activity = 1;
	
	private byte eventType;
	private int activityId;
	
    public NewServerRankEvent(byte eventType , int activityId) {
    	this.eventType = eventType;
        this.activityId = activityId;
    }
    
    public byte getEventType(){
    	return this.eventType;
    }
    
    public int getActivityId(){
    	return this.activityId;
    }
}
