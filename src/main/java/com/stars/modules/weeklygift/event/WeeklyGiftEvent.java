package com.stars.modules.weeklygift.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-06-29.
 */
public class WeeklyGiftEvent extends Event {
    private int giftId;
    private int charge;
    private Map<Integer, Integer> itemMap;

    public WeeklyGiftEvent(int giftId, int charge, Map<Integer, Integer> itemMap) {
        this.giftId = giftId;
        this.charge = charge;
        this.itemMap = itemMap;
    }

    public int getCharge() {
        return charge;
    }

    public int getGiftId() {
        return giftId;
    }

    public Map<Integer, Integer> getItemMap() {
        return itemMap;
    }
}
