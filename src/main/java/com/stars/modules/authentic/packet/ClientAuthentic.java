package com.stars.modules.authentic.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.authentic.AuthenticPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2016/12/23.
 */
public class ClientAuthentic extends PlayerPacket {

    private List<Map<Integer, Integer>> itemMapList;//List:Map<itemid , count>

    public ClientAuthentic(){}

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return AuthenticPacketSet.C_AUTHENTIC;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte((byte) itemMapList.size());
        Iterator<Map<Integer, Integer>> iterator = itemMapList.iterator();
        while (iterator.hasNext()) {
            Map<Integer, Integer> map = iterator.next();
            buff.writeByte((byte) map.size());
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                buff.writeInt(entry.getKey());
                buff.writeInt(entry.getValue());
            }
        }
    }

    public void setItemMapList(List<Map<Integer, Integer>> itemMapList) {
        this.itemMapList = itemMapList;
    }
}
