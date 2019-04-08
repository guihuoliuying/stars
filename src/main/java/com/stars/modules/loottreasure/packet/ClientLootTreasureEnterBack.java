package com.stars.modules.loottreasure.packet;

import com.stars.modules.loottreasure.LootTreasurePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * 通知主服,进入野外夺宝活动了;
 * Created by panzhenfeng on 2016/11/1.
 */
public class ClientLootTreasureEnterBack extends Packet {
    private int stageId;
    private long roleId;

    public ClientLootTreasureEnterBack(){}

    public ClientLootTreasureEnterBack(int stageId, long roleId){
        this.stageId = stageId;
        this.roleId = roleId;
    }

    @Override
    public short getType() {
        return LootTreasurePacketSet.C_LOOTTREASURE_ENTER_BACK;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.stageId = buff.readInt();
        this.roleId = buff.readLong();
    }

    @Override
    public void execPacket() {

    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(this.stageId);
        buff.writeLong(this.roleId);
    }

    public int getStageId(){
        return stageId;
    }

    public long getRoleId(){
        return roleId;
    }
}