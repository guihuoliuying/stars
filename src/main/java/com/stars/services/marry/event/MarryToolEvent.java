package com.stars.services.marry.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by zhouyaohui on 2016/12/9.
 */
public class MarryToolEvent extends Event {
    public final static byte ADD = 1;
    public final static byte SUB = 2;

    private byte operator;
    private byte handleType;
    private Map<Integer, Integer> toolMap;
    private Object arg;

    public MarryToolEvent(byte operator, byte type, Map<Integer, Integer> map, Object arg) {
        this.operator = operator;
        handleType = type;
        toolMap = map;
        this.arg = arg;
    }

    public Object getArg() {
        return arg;
    }

    public byte getOperator() {
        return operator;
    }

    public byte getHandleType() {
        return handleType;
    }

    public Map<Integer, Integer> getToolMap() {
        return toolMap;
    }
}
