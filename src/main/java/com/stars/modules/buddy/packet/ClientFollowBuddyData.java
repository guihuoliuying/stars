package com.stars.modules.buddy.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.buddy.BuddyPacketSet;
import com.stars.modules.buddy.userdata.RoleBuddy;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/8/11.
 */
public class ClientFollowBuddyData extends PlayerPacket {
    private RoleBuddy roleBuddy;

    public ClientFollowBuddyData() {
    }

    public ClientFollowBuddyData(RoleBuddy roleBuddy) {
        this.roleBuddy = roleBuddy;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return BuddyPacketSet.C_FOLLOWBUDDY_DATA;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        byte isFollow = (byte) (roleBuddy == null ? 0 : 1);
        buff.writeByte(isFollow);// 0=没有跟随,1=有跟随
        if (isFollow == 0)
            return;
        roleBuddy.writeFollowBuddy(buff);
    }
}
