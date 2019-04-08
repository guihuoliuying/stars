package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 野外夺宝场景下发包;
 * Created by panzhenfeng on 2016/10/10.
 */
public class ClientEnterLootTreasurePVE extends ClientEnterFight{

    public int monsterRemainHp = 0;
    public int minusHpPerSecond = 0;
    public String timeStamp;
    public String monsterPos = "";

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        writeBase(buff);
        buff.writeInt(monsterRemainHp);
        buff.writeInt(minusHpPerSecond);
        buff.writeString(monsterPos);
        buff.writeString(timeStamp);
    }
}
