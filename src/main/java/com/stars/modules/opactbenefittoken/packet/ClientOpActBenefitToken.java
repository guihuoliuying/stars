package com.stars.modules.opactbenefittoken.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.opactbenefittoken.OpActBenefitTokenPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2017/6/16.
 */
public class ClientOpActBenefitToken extends PlayerPacket {

    private int times;
    private int timesLimit;

    public ClientOpActBenefitToken() {
    }

    public ClientOpActBenefitToken(int times, int timesLimit) {
        this.times = times;
        this.timesLimit = timesLimit;
    }

    @Override
    public void execPacket(Player player) {
    }

    @Override
    public short getType() {
        return OpActBenefitTokenPacketSet.C_OPACT_BENEFIT_TOKEN;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(timesLimit);
        buff.writeInt(times);
    }
}
