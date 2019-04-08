package com.stars.modules.marry.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.marry.MarryPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;


/**
 * Created by zhanghaizhen on 2017/5/22.
 */
public class ClientMarryBattle extends PlayerPacket {
    int remainReviveTimes;
    int reBornTime;
    public ClientMarryBattle() {
    }


    @Override
    public void execPacket(Player player) {
    }

    @Override
    public short getType() {
        return MarryPacketSet.C_MARRY_BATTLE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(remainReviveTimes);
        buff.writeInt(reBornTime);
    }

    public void setRemainReviveTimes(int remainReviveTimes) {
        this.remainReviveTimes = remainReviveTimes;
    }

    public void setReBornTime(int reBornTime) {
        this.reBornTime = reBornTime;
    }
}
