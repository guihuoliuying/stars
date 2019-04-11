package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 非战斗场景传送请求
 * Created by liuyuheng on 2016/7/19.
 */
public class ServerEnterCity extends PlayerPacket {
    private String enterPosition;// 传送入口坐标

    @Override
    public void execPacket(Player player) {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        sceneModule.transferSafeStage(enterPosition);
    }

    @Override
    public short getType() {
        return ScenePacketSet.S_CITY_TRANSFER;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.enterPosition = buff.readString();
    }
}
