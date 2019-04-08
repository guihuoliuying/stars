package com.stars.modules.loottreasure.packet;

import com.stars.modules.loottreasure.LootTreasurePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * 向夺宝服请求排行榜数据;
 * Created by panzhenfeng on 2016/10/21.
 */
public class ServerLootTreasureRankReq extends Packet{

    public ServerLootTreasureRankReq(){

    }

    @Override
    public short getType() {
        return LootTreasurePacketSet.S_LOOTTREASURE_RANK_REQ;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

}
