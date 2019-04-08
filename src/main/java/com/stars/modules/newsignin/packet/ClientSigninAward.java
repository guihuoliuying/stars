package com.stars.modules.newsignin.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.newsignin.NewSigninPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by chenkeyu on 2017/2/6 19:42
 */
public class ClientSigninAward extends PlayerPacket {
    public static final byte signAward = 0x00;
    public static final byte specialAward = 0x01;
    public static final byte accAward = 0x02;

    private byte subtype;
    Map<Integer, Integer> itemMap;

    public ClientSigninAward() {}

    public ClientSigninAward(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return NewSigninPacketSet.C_SigninAward;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype);
        buff.writeByte((byte) itemMap.size());
        for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
            buff.writeInt(entry.getKey());
            buff.writeInt(entry.getValue());
        }
    }

    public Map<Integer, Integer> getItemMap() {
        return itemMap;
    }

    public void setItemMap(Map<Integer, Integer> itemMap) {
        this.itemMap = itemMap;
    }
}
