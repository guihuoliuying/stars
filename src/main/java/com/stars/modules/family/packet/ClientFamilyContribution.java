package com.stars.modules.family.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/9/2.
 */
public class ClientFamilyContribution extends PlayerPacket {

    private int contribution;

    public ClientFamilyContribution() {
    }

    public ClientFamilyContribution(int contribution) {
        this.contribution = contribution;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyPacketSet.C_CONTRIBUTION;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(contribution);
    }

    public int getContribution() {
        return contribution;
    }

    public void setContribution(int contribution) {
        this.contribution = contribution;
    }
}
