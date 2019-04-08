package com.stars.modules.gem.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gem.GemPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 做一些反馈提示用;
 * Created by panzhenfeng on 2016/11/26.
 */
public class ClientGemResponse extends PlayerPacket {

    public byte type;//0合成宝石提示;
    public String param;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(type);
        buff.writeString(param);
    }

    @Override
    public short getType() {
        return GemPacketSet.C_EQUIPMENT_RESPONSE;
    }
}
