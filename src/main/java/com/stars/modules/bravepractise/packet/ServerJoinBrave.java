package com.stars.modules.bravepractise.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.bravepractise.BravePractiseModule;
import com.stars.modules.bravepractise.BravePractisePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2016/11/17.
 */
public class ServerJoinBrave  extends PlayerPacket {

    private byte opType;

    @Override
    public void execPacket(Player player) {
    	BravePractiseModule bravePractiseModule = (BravePractiseModule)this.module(MConst.BravePractise);
        if(opType == 0) {//请求开始勇者试炼
            bravePractiseModule.joinBravePractise();
        }else if(opType == 1){//立刻完成勇者试炼
            bravePractiseModule.finishAllTaskByGold();
        }
    }

    @Override
    public short getType() {
        return BravePractisePacketSet.S_JOIN_BRAVE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.opType = buff.readByte();
    }
}
