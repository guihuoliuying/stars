package com.stars.modules.buddy.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.buddy.BuddyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/8/31.
 */
public class ServerGuardPacket extends PlayerPacket {
    private short subType;
    public static final short REQ_BUDDY_GUARD_MAIN = 1;//请求守卫主界面数据包含产品数据和用户数据
    public static final short REQ_BUDDY_GUARD_USER_DATA = 2;//请求用户数据


    public ServerGuardPacket() {
    }

    @Override
    public void execPacket(Player player) {
        BuddyModule buddyModule = module(MConst.Buddy);
        switch (subType) {
            case REQ_BUDDY_GUARD_MAIN: {
                buddyModule.reqBuddyGuardMain(true);
            }
            break;
            case REQ_BUDDY_GUARD_USER_DATA: {
                buddyModule.reqBuddyGuardMain(false);
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readShort();
    }

    @Override
    public short getType() {
        return BuddyPacketSet.S_BUDDY_GUARD;
    }
}
