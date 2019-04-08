package com.stars.modules.foreshow.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by chenkeyu on 2016/11/10.
 */
public class ForeShowChangeEvent extends Event {
    private Map<String,String> map ;// openname,--openinduct

    public ForeShowChangeEvent(Map<String,String> map){
        this.map = map;
    }

    public Map<String, String> getMap() {
        return map;
    }
}
