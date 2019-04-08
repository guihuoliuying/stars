package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/8/10.
 */
public class ClientFightTime extends PlayerPacket {
    private int spendTime;

    public ClientFightTime() {
    }

    public ClientFightTime(int spendTime) {
        this.spendTime = spendTime;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_FIGHT_TIME;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(spendTime);
    }
}
