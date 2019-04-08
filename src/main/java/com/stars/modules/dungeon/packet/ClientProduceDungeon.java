package com.stars.modules.dungeon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.dungeon.DungeonPacketSet;
import com.stars.modules.dungeon.prodata.ProduceDungeonVo;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/9/1.
 */
public class ClientProduceDungeon extends PlayerPacket {
    private ProduceDungeonVo produceDungeonVo;

    public ClientProduceDungeon() {
    }

    public ClientProduceDungeon(ProduceDungeonVo produceDungeonVo) {
        this.produceDungeonVo = produceDungeonVo;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return DungeonPacketSet.C_PRODUCEDUNGEON;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        produceDungeonVo.writeToBuff(buff);
    }
}
