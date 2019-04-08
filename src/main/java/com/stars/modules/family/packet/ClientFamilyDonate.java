package com.stars.modules.family.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/9/5.
 */
public class ClientFamilyDonate extends PlayerPacket {

    public static final byte SUBTYPE_INFO = 0x00;
    public static final byte SUBTYPE_DONATE = 0x01;
    public static final byte SUBTYPE_DNOATE_RMB = 0x02;

    private byte subtype;
    private byte donateResidue;
    private byte donateRmbResidue;

    public ClientFamilyDonate() {
    }

    public ClientFamilyDonate(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyPacketSet.C_DONATE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype);
        buff.writeByte(donateResidue);
        buff.writeByte(donateRmbResidue);
    }

    public void setDonateResidue(byte donateResidue) {
        this.donateResidue = donateResidue;
    }

    public void setDonateRmbResidue(byte donateRmbResidue) {
        this.donateRmbResidue = donateRmbResidue;
    }
}
