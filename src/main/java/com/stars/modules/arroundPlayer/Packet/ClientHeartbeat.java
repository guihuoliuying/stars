package com.stars.modules.arroundPlayer.Packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.arroundPlayer.ArroundPlayerPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 响应心跳包;
 * Created by panzhenfeng on 2016/10/9.
 */
public class ClientHeartbeat extends PlayerPacket {

    public ClientHeartbeat() {

    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ArroundPlayerPacketSet.Client_Heartbeat;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeLong(System.currentTimeMillis());
    }
}
