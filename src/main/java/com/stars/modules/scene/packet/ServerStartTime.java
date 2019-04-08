package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/8/10.
 */
public class ServerStartTime extends PlayerPacket {
	private byte sceneType;
	
    @Override
    public void execPacket(Player player) {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        sceneModule.startFightTime(sceneType);
    }

    @Override
    public short getType() {
        return ScenePacketSet.S_START_TIME;
    }
    
    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.sceneType = buff.readByte();
    }
}
