package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2017/1/17.
 */
public class ServerSpawnMonsterConfirm extends PlayerPacket {
    private byte sceneType;// 战斗场景类型
    private int spawnUniqueId;// 刷怪组唯一Id

    @Override
    public void execPacket(Player player) {
        SceneModule sceneModule = module(MConst.Scene);
        sceneModule.receiveFightPacket(sceneType, this);
    }

    @Override
    public short getType() {
        return ScenePacketSet.S_SPAWN_MONSTER_CONFIRM;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.sceneType = buff.readByte();// 战斗场景类型
        this.spawnUniqueId = buff.readInt();// 刷怪组唯一Id
    }

    public int getSpawnUniqueId() {
        return spawnUniqueId;
    }
}
