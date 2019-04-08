package com.stars.modules.gem.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gem.GemPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by panzhenfeng on 2016/11/25.
 */
public class ClientGemAllComposeInfo extends PlayerPacket {
    private Map<Integer, Map<Integer, Integer>> map = new ConcurrentHashMap<>();
    @Override
    public short getType() {
        return GemPacketSet.C_EQUIPMENT_ALLGEM_COMPOSEINFO;
    }

    public void addCanComposedGemLevelIdInfo(int waitToCanGemLevelId, int canComposedGemLevelId, int canComposeCount){
        Map<Integer, Integer> subMap = new ConcurrentHashMap();
        subMap.put(canComposedGemLevelId, canComposeCount);
        map.put(waitToCanGemLevelId, subMap);
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        int size = map.size();
        buff.writeInt(size);
        for (Map.Entry<Integer, Map<Integer, Integer>> kvp : map.entrySet()){
            buff.writeInt(kvp.getKey());
            buff.writeInt(kvp.getValue().size());
            for (Map.Entry<Integer, Integer> subKey : kvp.getValue().entrySet()){
                buff.writeInt(subKey.getKey());
                buff.writeInt(subKey.getValue());
            }
        }
    }

    @Override
    public void execPacket(Player player) {

    }

}
