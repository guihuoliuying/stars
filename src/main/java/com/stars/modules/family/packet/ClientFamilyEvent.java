package com.stars.modules.family.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.family.event.userdata.FamilyEventPo;

import java.util.List;

/**
 * Created by zhaowenshuo on 2016/9/13.
 */
public class ClientFamilyEvent extends PlayerPacket {

    public static final byte SUBTYPE_DONATE = 0x00;
    public static final byte SUBTYPE_EVENT = 0x01;

    private byte subtype;
    private List<FamilyEventPo> eventPoList;

    public ClientFamilyEvent() {
    }

    public ClientFamilyEvent(byte subtype, List<FamilyEventPo> eventPoList) {
        this.subtype = subtype;
        this.eventPoList = eventPoList;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyPacketSet.C_EVENT;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype);
        if (eventPoList == null) {
            buff.writeInt(0);
        } else {
            buff.writeInt(eventPoList.size());
            for (FamilyEventPo eventPo : eventPoList) {
                eventPo.writeToBuffer(buff);
            }
        }
    }

    public List<FamilyEventPo> getEventPoList() {
        return eventPoList;
    }

    public void setEventPoList(List<FamilyEventPo> eventPoList) {
        this.eventPoList = eventPoList;
    }
}
