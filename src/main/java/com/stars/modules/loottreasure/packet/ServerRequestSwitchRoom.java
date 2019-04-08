package com.stars.modules.loottreasure.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.loottreasure.LootTreasurePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 客户端请求切换房间;
 * Created by panzhenfeng on 2016/10/28.
 */
public class ServerRequestSwitchRoom  extends PlayerPacket {
    private byte roomType;

    public ServerRequestSwitchRoom(){

    }

    @Override
    public short getType() {
        return LootTreasurePacketSet.S_LOOTTREASURE_SWITCH_ROOM;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(this.roomType);
        buff.writeString(String.valueOf(this.getRoleId()));
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        roomType = buff.readByte();
        this.setRoleId(Long.parseLong(buff.readString()));
    }

    @Override
    public void execPacket(Player player) {

    }

    public byte getRoomType() {
        return roomType;
    }

    public void setRoomType(byte roomType){
        this.roomType = roomType;
    }
}
