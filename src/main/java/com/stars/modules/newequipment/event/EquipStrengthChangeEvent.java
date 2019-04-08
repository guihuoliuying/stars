package com.stars.modules.newequipment.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/19.
 */
public class EquipStrengthChangeEvent extends Event {
    private int totalStrengthLevel;
    private Map<Byte,Integer> strengthLevelMap;

    public EquipStrengthChangeEvent(int totalStrengthLevel,Map<Byte,Integer> strengthLevelMap) {
        this.totalStrengthLevel = totalStrengthLevel;
        this.strengthLevelMap = strengthLevelMap;
    }

    public int getTotalStrengthLevel() {
        return totalStrengthLevel;
    }

    public Map<Byte, Integer> getStrengthLevelMap() {
        return strengthLevelMap;
    }
}
