package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 清除CD;
 * Created by panzhenfeng on 2016/8/25.
 */
public class ClientClearCd extends PlayerPacket {

    private byte type;
    public ClientClearCd() {

    }

    public ClientClearCd(byte type) {
        this.type = type;
    }


    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_SERVER_CLEAR_CD;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(this.type);
    }
}
