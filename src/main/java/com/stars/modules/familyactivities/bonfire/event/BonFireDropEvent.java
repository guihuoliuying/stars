package com.stars.modules.familyactivities.bonfire.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by wuyuxing on 2017/3/10.
 */
public class BonFireDropEvent extends Event {

    public static final byte TYPE_EXP = 1;
    public static final byte TYPE_THROW_WOOD = 2;
    public static final byte TYPE_ANSWER = 3;

    private Byte type;
    private Map<Integer,Integer> map;

    public Map<Integer, Integer> getMap() {
        return map;
    }

    public BonFireDropEvent(byte type,Map<Integer, Integer> map) {
        this.type = type;
        this.map = map;
    }

    public Byte getType() {
        return type;
    }
}
