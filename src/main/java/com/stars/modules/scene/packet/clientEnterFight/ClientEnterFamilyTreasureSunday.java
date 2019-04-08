package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-02-28 16:00
 */
public class ClientEnterFamilyTreasureSunday extends ClientEnterDungeon {
    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        writeBase(buff);
    }
}
