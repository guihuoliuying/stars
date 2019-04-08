package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-08-24.
 */
public class ClientEnterDareGod extends ClientEnterDungeon {

    private int vitoryTimes;

    public void writeToBuffer(NewByteBuffer buff) {
        writeBase(buff);
        buff.writeInt(vitoryTimes);

    }

    public void setVitoryTimes(int vitoryTimes) {
        this.vitoryTimes = vitoryTimes;
    }
}
