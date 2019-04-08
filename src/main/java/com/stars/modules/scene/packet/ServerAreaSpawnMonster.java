package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/7/7.
 */
public class ServerAreaSpawnMonster extends PlayerPacket {
    private byte sceneType;// 战斗场景类型
    private int spawnId;

    @Override
    public void execPacket(Player player) {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        sceneModule.areaSpawnMonster(sceneType, this);
    }

    @Override
    public short getType() {
        return ScenePacketSet.S_AREASPAWN;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.sceneType = buff.readByte();
        this.spawnId = buff.readInt();
    }

    public int getSpawnId() {
        return spawnId;
    }
}
