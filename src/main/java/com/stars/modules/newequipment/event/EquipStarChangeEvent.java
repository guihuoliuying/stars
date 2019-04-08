package com.stars.modules.newequipment.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/19.
 */
public class EquipStarChangeEvent extends Event {
    private int totalStarLevel;
    private Map<Byte,Integer> starLevelMap;

    public EquipStarChangeEvent(int totalStarLevel,Map<Byte,Integer> starLevelMap) {
        this.totalStarLevel = totalStarLevel;
        this.starLevelMap = starLevelMap;
    }

    public int getTotalStarLevel() {
        return totalStarLevel;
    }

    public Map<Byte, Integer> getStarLevelMap() {
        return starLevelMap;
    }
}
