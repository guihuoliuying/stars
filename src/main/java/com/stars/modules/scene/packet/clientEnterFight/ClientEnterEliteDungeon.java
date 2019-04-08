package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2017/4/7.
 */
public class ClientEnterEliteDungeon extends ClientEnterDungeon {
    private short spawnMonsterNumber;// 刷怪总数

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        super.writeToBuffer(buff);
        buff.writeShort(spawnMonsterNumber);
    }

    public void setSpawnMonsterNumber(short spawnMonsterNumber) {
        this.spawnMonsterNumber = spawnMonsterNumber;
    }
}
