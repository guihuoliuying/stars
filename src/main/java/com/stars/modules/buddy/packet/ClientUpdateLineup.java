package com.stars.modules.buddy.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.buddy.BuddyPacketSet;
import com.stars.modules.buddy.userdata.RoleBuddyLineup;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * Created by liuyuheng on 2016/8/11.
 */
public class ClientUpdateLineup extends PlayerPacket {
    private List<RoleBuddyLineup> list;

    public ClientUpdateLineup() {
    }

    public ClientUpdateLineup(List<RoleBuddyLineup> list) {
        this.list = list;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return BuddyPacketSet.C_UPDATE_LINEUP;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        byte size = (byte) (list == null ? 0 : list.size());
        buff.writeByte(size);
        for (RoleBuddyLineup roleBuddyLineup : list) {
            roleBuddyLineup.writeToBuff(buff);
        }
    }
}
