package com.stars.modules.redpoint.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.redpoint.RedPointPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by daiyaorong on 2016/11/16.
 */
public class ClientRedPoint extends PlayerPacket {
    Map<Integer, String> addMap;
    Map<Integer, String> removeMap;

    @Override
    public short getType() {
        return RedPointPacketSet.C_REDPOINT;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        short size = (short) (addMap == null ? 0 : addMap.size());
        buff.writeShort(size);
        if (size > 0) {
            for (Map.Entry<Integer, String> entry : addMap.entrySet() ) {
                buff.writeInt(entry.getKey());
                buff.writeString(entry.getValue());
            }
        }
        size = (short) (removeMap == null ? 0 : removeMap.size());
        buff.writeShort(size);
        if (size > 0) {
            for (Map.Entry<Integer, String> entry : removeMap.entrySet()) {
                buff.writeInt(entry.getKey());
                buff.writeString(entry.getValue());
            }
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket(Player player) {

    }

    public Map<Integer, String> getAddMap() {
        return addMap;
    }

    public void setAddMap(Map<Integer, String> addMap) {
        this.addMap = addMap;
    }

    public Map<Integer, String> getRemoveMap() {
        return removeMap;
    }

    public void setRemoveMap(Map<Integer, String> removeMap) {
        this.removeMap = removeMap;
    }
}
