package com.stars.modules.teampvpgame.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.teampvpgame.TeamPVPGamePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/12/27.
 */
public class ClientTPGFightDamageCollect extends PlayerPacket {
    private Map<Long, Integer> collectDamage;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return TeamPVPGamePacketSet.C_TPG_COLLECT_DAMAGE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        byte size = (byte) (collectDamage == null ? 0 : collectDamage.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (Map.Entry<Long, Integer> entry : collectDamage.entrySet()) {
            buff.writeString(String.valueOf(entry.getKey()));// roleId
            buff.writeInt(entry.getValue());// damage
        }
    }

    public void setCollectDamage(Map<Long, Integer> collectDamage) {
        this.collectDamage = collectDamage;
    }
}
