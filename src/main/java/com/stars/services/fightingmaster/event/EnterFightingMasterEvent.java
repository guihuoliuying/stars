package com.stars.services.fightingmaster.event;

import com.stars.core.event.Event;
import com.stars.network.server.packet.Packet;

/**
 * Created by zhouyaohui on 2016/11/9.
 */
public class EnterFightingMasterEvent extends Event {

    private boolean success;
    Packet enterPacket;

    public EnterFightingMasterEvent(boolean r, Packet enter) {
        success = r;
        enterPacket = enter;
    }

    public boolean isSuccess() {
        return success;
    }

    public Packet getEnterPacket() {
        return enterPacket;
    }
}
