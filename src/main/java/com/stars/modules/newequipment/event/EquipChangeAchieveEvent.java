package com.stars.modules.newequipment.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/19.
 */
public class EquipChangeAchieveEvent extends Event {
    private Map<Byte,Integer> equipLevelMap;
    private Map<Byte,Byte> equipQualityMap;

    public Map<Byte,Integer> getEquipLevelMap() {
        return equipLevelMap;
    }

    public Map<Byte, Byte> getEquipQualityMap() {
        return equipQualityMap;
    }

    public EquipChangeAchieveEvent(Map<Byte, Integer> equipLevelMap, Map<Byte, Byte> equipQualityMap) {
        this.equipLevelMap = equipLevelMap;
        this.equipQualityMap = equipQualityMap;
    }

    public EquipChangeAchieveEvent() {
    }
}
