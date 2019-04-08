package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-02-28 15:40
 */
public class ClientEnterFamilyTreasure extends ClientEnterDungeon {
    private int vitoryTimes;
    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        writeBase(buff);
        buff.writeInt(vitoryTimes);
    }

    public void setVitoryTimes(int vitoryTimes) {
        this.vitoryTimes = vitoryTimes;
    }
}
