package com.stars.modules.book.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by zhoujin on 2017/5/10.
 */
public class KickEvent extends Event {
    private long target;
    private byte result;
    private Map<Integer,Integer> toolmap;
    public KickEvent(byte result, long target, Map<Integer,Integer> toolmap) {
        this.target = target;
        this.result = result;
        this.toolmap = toolmap;
    }

    public long getTarget() {
        return target;
    }

    public byte getResult() {
        return result;
    }

    public Map<Integer, Integer> getToolmap() {
        return toolmap;
    }
}
