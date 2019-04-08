package com.stars.modules.scene.packet.fightSync;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by daiyaorong on 2016/8/26.
 */
public class ClientSyncOrder extends PlayerPacket {

    private byte[] orders;

    public ClientSyncOrder() {
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(orders.length);
        buff.writeBytes(orders);
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_SYNCORDER;
    }

    public void setOrders(byte[] orderStr) {
        orders = orderStr;
    }
}
