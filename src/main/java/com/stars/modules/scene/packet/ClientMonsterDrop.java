package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/9/22.
 */
public class ClientMonsterDrop extends PlayerPacket {
    private Map<String, List<Map<Integer, Integer>>> dropMapList;// <monsterUId,dropList>

    public ClientMonsterDrop() {
    }

    public ClientMonsterDrop(Map<String, List<Map<Integer, Integer>>> dropMapList) {
        this.dropMapList = dropMapList;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_MONSTER_DROP;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        // 下发掉落
        byte size = (byte) (dropMapList == null ? 0 : dropMapList.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (Map.Entry<String, List<Map<Integer, Integer>>> entry : dropMapList.entrySet()) {
            size = 0;
            buff.writeString(entry.getKey());// 怪物唯一Id
            // 先算一下物品总数
            for (Map<Integer, Integer> map : entry.getValue()) {
                size = (byte) (size + map.size());
            }
            buff.writeByte(size);
            if (size > 0) {
                for (Map<Integer, Integer> map : entry.getValue()) {
                    for (Map.Entry<Integer, Integer> drop : map.entrySet()) {
                        buff.writeInt(drop.getKey());// itemid
                        buff.writeInt(drop.getValue());// number
                    }
                }
            }
        }
    }
}
