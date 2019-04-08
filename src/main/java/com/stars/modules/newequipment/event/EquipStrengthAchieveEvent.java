package com.stars.modules.newequipment.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/8/11.
 */
public class EquipStrengthAchieveEvent extends Event {
    private int totalStrengthLevel;
    private Map<Byte,Integer> strengthLevelMap;

    public EquipStrengthAchieveEvent(int totalStrengthLevel,Map<Byte,Integer> strengthLevelMap) {
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
