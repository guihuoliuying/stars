package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by daiyaorong on 2016/6/20.
 */
public class ServerEnterFight extends PlayerPacket {
    private byte dungeonType; //用于标明是一般副本，还是镇妖塔，以及之后的"副本"类型
    private int dungeonId;
    private byte isAgain;// 是否重复进入

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.dungeonType = buff.readByte();
        this.dungeonId = buff.readInt();
        this.isAgain = buff.readByte();
    }

    @Override
    public void execPacket(Player player) {
        LogUtil.info("enterFight|dungeonType:{},dungeonId:{}", dungeonType, dungeonId);
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        sceneModule.enterScene(dungeonType, dungeonId, dungeonId);
    }

    @Override
    public short getType() {
        return ScenePacketSet.S_ENTERFIGHT;
    }
}
