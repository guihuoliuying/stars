package com.stars.modules.family.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2016/12/1.
 */
public class FamilyChangeRedPacketEvent extends Event {
    private int redPacketCount;
    public FamilyChangeRedPacketEvent(int redPacketCount){
        this.redPacketCount=redPacketCount;
    }

    public int getRedPacketCount() {
        return redPacketCount;
    }
}
