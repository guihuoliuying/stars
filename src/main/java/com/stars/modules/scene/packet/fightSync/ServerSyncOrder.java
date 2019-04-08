package com.stars.modules.scene.packet.fightSync;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by daiyaorong on 2016/8/26.
 */
public class ServerSyncOrder extends PlayerPacket {
    private byte sceneType;// 战斗场景类型
    private byte[] orders;// 指令

    public ServerSyncOrder() {
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        sceneType = buff.readByte();
        int length = buff.readInt();
        orders = buff.readBytes(length);
    }

    @Override
    public short getType() {
        return ScenePacketSet.S_SYNCORDER;
    }

    @Override
    public void execPacket(Player player) {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        sceneModule.receiveFightPacket(sceneType, this);
    }

    public byte[] getOrders() {
        return orders;
    }
}
