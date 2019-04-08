package com.stars.modules.daregod.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-08-24.
 */
public class DareGodGetAwardEvent extends Event {
    private Map<Integer, Integer> itemMap;

    public DareGodGetAwardEvent(Map<Integer, Integer> itemMap) {
        this.itemMap = itemMap;
    }

    public Map<Integer, Integer> getItemMap() {
        return itemMap;
    }
}
