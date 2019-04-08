package com.stars.modules.scene.packet.fightSync;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/9/23.
 */
public class ClientSyncAttr extends PlayerPacket {
    private Map<String, Integer> curHpMap = new HashMap<>();// <uId, curhp>
    private Map<String, Integer> damageMap = new HashMap<>();// <uId, damage>

    public ClientSyncAttr() {
    }

    public ClientSyncAttr(Map<String, Integer> curHpMap) {
        this.curHpMap = curHpMap;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_SYNC_ATTR;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        writeMap(buff, curHpMap);
        writeMap(buff, damageMap);
    }

    private void writeMap(NewByteBuffer buff, Map<String, Integer> map) {
        byte size = (byte) (map == null ? 0 : map.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            buff.writeString(entry.getKey());
            buff.writeInt(entry.getValue());
        }
    }

    public void addSyncCurHp(String uniqueId, int curHp) {
        curHpMap.put(uniqueId, curHp);
    }

    public void setDamageMap(Map<String, Integer> damageMap) {
        this.damageMap = damageMap;
    }
}
