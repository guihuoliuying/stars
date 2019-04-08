package com.stars.modules.tool.event;


import com.stars.core.event.Event;

/**
 * Created by zhoujin on 2017/3/22.
 *
 * 
 *
 * GM 删除道具
 */
public class GMDelToolEvent extends Event {
    long toolid = 0;
    int amount = 0;
    public GMDelToolEvent(long toolid,int amount){
        this.toolid = toolid;
        this.amount = amount;
    }
    public long getToolid() {
    	return this.toolid;
    }
    public int getAmount() {
    	return this.amount;
    }
}
