package com.stars.modules.sevendaygoal.event;

import com.stars.core.event.Event;

/**
 * Created by gaopeidian on 2016/12/19.
 */
public class RewardCountChangeEvent extends Event {
	private int goalId;
	private int leftGetCount;
	
    public RewardCountChangeEvent(Integer goalId , int leftGetCount) {
    	this.goalId = goalId;
    	this.leftGetCount = leftGetCount;
    }
    
    public int getGoalId() {
		return goalId;
	}
    
	public int getLeftGetCount() {
		return leftGetCount;
	}
}
