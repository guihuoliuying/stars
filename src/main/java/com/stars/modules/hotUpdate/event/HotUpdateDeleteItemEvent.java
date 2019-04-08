package com.stars.modules.hotUpdate.event;

import com.stars.core.event.Event;
import com.stars.modules.hotUpdate.HotUpdateConstant;

import java.util.Map;

/**
 * Created by wuyuxing on 2017/1/4.
 */
public class HotUpdateDeleteItemEvent extends Event {

    private Map<Integer,Integer> awardMap;
    private String logSignal = HotUpdateConstant.DELETE_LOG_SIGNAL;

    public HotUpdateDeleteItemEvent(Map<Integer, Integer> awardMap) {
        this.awardMap = awardMap;
    }

    public Map<Integer, Integer> getAwardMap() {
        return awardMap;
    }

    public void setAwardMap(Map<Integer, Integer> awardMap) {
        this.awardMap = awardMap;
    }

    public String getLogSignal() {
        return logSignal;
    }

    public void setLogSignal(String logSignal) {
        this.logSignal = logSignal;
    }
}
