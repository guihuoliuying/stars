package com.stars.modules.operateactivity.event;

import com.stars.core.event.Event;

/**
 * Created by gaopeidian on 2016/12/7.
 */
public class OperateActivityEvent extends Event {
	public static final byte Flag_Send_All = 0;//通知活动显示控制，推送最新的活动显示信息到客户端(一般活动开启或关闭时用)
	public static final byte Flag_Open_Activity = 1;
	public static final byte Flag_Close_Activity = 2;
	
	private byte flag;
	private int activityType;
	private int activityId;
	
	/**
	 * @param flag为Flag_Send_All时，activityType和activityId传入-1即可
	 */
    public OperateActivityEvent(byte flag , int activityType , int activityId) {
    	this.flag = flag;
        this.activityType = activityType;
        this.activityId = activityId;
    }
    
    public byte getFlag(){
    	return this.flag;
    }
    
    public int getActivityType(){
    	return this.activityType;
    }
    
    public int getActivityId(){
    	return this.activityId;
    }
}
