package com.stars.modules.buddy.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.buddy.BuddyPacketSet;

/**
 * Created by liuyuheng on 2016/8/10.
 */
public class ServerAllBuddyData extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
        BuddyModule buddyModule = (BuddyModule) module(MConst.Buddy);
        buddyModule.sendAllBuddyData();
    }

    @Override
    public short getType() {
        return BuddyPacketSet.S_ALL_BUDDY_DATA;
    }
}
