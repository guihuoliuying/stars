package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 通知客户端给玩家添加BUFF;
 * Created by panzhenfeng on 2016/8/25.
 */
public class ClientAddBuff extends PlayerPacket {

    private int buffId;
    private int buffLevel;

    public ClientAddBuff() {

    }

    public ClientAddBuff(int buffId, int buffLevel) {
        this.buffId = buffId;
        this.buffLevel = buffLevel;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_SERVER_ADD_BUFF;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(this.buffId);
        buff.writeInt(this.buffLevel);
    }
}
