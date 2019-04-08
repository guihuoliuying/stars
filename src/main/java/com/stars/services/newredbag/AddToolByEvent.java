package com.stars.services.newredbag;

import com.stars.core.event.Event;
import com.stars.modules.serverLog.EventType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouyaohui on 2017/2/15.
 */
public class AddToolByEvent extends Event {

    private Map<Integer, Integer> map = new HashMap<>();
    private EventType eventType;

    public AddToolByEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public AddToolByEvent(int itemId, int count, EventType type) {
        map.put(itemId, count);
        eventType = type;
    }

    public EventType eventType() {
        return eventType;
    }

    public Map<Integer, Integer> toolMap() {
        return map;
    }

}
