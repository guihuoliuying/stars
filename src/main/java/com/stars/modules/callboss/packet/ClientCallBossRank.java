package com.stars.modules.callboss.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.callboss.CallBossPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.callboss.cache.RoleDamageCache;

import java.util.List;

/**
 * Created by liuyuheng on 2016/9/8.
 */
public class ClientCallBossRank extends PlayerPacket {
    private int rankUniqueId;
    private List<RoleDamageCache> list;

    public ClientCallBossRank() {
    }

    public ClientCallBossRank(int rankUniqueId, List<RoleDamageCache> list) {
        this.rankUniqueId = rankUniqueId;
        this.list = list;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return CallBossPacketSet.C_CALLBOSS_RANKINFO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(rankUniqueId);
        short size = (short) (list == null ? 0 : list.size());
        buff.writeShort(size);
        if (size == 0)
            return;
        for (RoleDamageCache roleDamageCache : list) {
            roleDamageCache.writeToBuff(buff);
        }
    }
}
