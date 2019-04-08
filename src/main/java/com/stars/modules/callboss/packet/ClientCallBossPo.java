package com.stars.modules.callboss.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.callboss.CallBossPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.callboss.cache.CallBossCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/9/8.
 */
public class ClientCallBossPo extends PlayerPacket {
    Map<Integer, CallBossCache> map;

    public ClientCallBossPo() {
    }

    public ClientCallBossPo(Map<Integer, CallBossCache> map) {
        this.map = map;
    }

    public ClientCallBossPo(CallBossCache cache) {
        map = new HashMap<>();
        map.put(cache.getBossId(), cache);
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return CallBossPacketSet.C_CALLBOSSPO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        byte size = (byte) (map == null ? 0 : map.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (CallBossCache callBossCache : map.values()) {
            callBossCache.writeToBuff(buff);
        }
    }
}
