package com.stars.modules.camp.packet;

import com.stars.modules.camp.CampPackset;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * Created by huwenjun on 2017/8/4.
 */
public class ClientCampFightClearPacket extends Packet {
    private short subType;
    public static final short SEND_REVIVE_NOTIFY = 1;//下发复活通知
    private String fightEntityId;

    public ClientCampFightClearPacket(short subType) {
        this.subType = subType;
    }

    public ClientCampFightClearPacket() {
    }

    @Override
    public short getType() {
        return CampPackset.C_CAMP_FIGHT_CLEAR;
    }

    public String getFightEntityId() {
        return fightEntityId;
    }

    public void setFightEntityId(String fightEntityId) {
        this.fightEntityId = fightEntityId;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeShort(subType);
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }
}
