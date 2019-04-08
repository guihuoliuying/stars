package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/7/26.
 */
public class ServerTalkWithNpc extends PlayerPacket {
    private int npcId;

    @Override
    public void execPacket(Player player) {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        sceneModule.talkWithNpc(npcId);
    }

    @Override
    public short getType() {
        return ScenePacketSet.S_TALKWITH_NPC;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.npcId = buff.readInt();
    }
}
