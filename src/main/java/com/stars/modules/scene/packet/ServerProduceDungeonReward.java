package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/9/1.
 */
public class ServerProduceDungeonReward extends PlayerPacket {
    private byte isDouble;

    @Override
    public void execPacket(Player player) {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        sceneModule.receiveFightPacket(SceneManager.SCENETYPE_PRODUCEDUNGEON, this);
    }

    @Override
    public short getType() {
        return ScenePacketSet.S_PRODUCEDUNGEON_REWARD;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.isDouble = buff.readByte();
    }

    public byte getIsDouble() {
        return isDouble;
    }
}
