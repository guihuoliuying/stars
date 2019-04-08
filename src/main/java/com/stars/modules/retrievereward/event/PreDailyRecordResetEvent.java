package com.stars.modules.retrievereward.event;

import com.stars.core.event.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/12/7.
 */
public class PreDailyRecordResetEvent extends Event {
	private Map<Short, Integer> recordMap = new HashMap<Short, Integer>();
	
    public PreDailyRecordResetEvent(Map<Short, Integer> recordMap) {
    	this.recordMap = recordMap;
    }
    
	public Map<Short, Integer> getRecordMap() {
		return recordMap;
	}
}
