package com.stars.modules.guardofficial.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.guardofficial.GuardOfficialPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-07-12.
 */
public class ClientGuardOfficial extends PlayerPacket {
    private int times;
    private int timesLimit;

    public ClientGuardOfficial() {
    }

    public ClientGuardOfficial(int times, int timesLimit) {
        this.times = times;
        this.timesLimit = timesLimit;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(timesLimit);//限制次数
        buff.writeInt(times);//玩了多少次
    }
    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GuardOfficialPacketSet.C_GUARDOFFICIAL;
    }
}
