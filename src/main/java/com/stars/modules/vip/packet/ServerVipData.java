package com.stars.modules.vip.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.vip.VipPacketSet;

/**
 * Created by liuyuheng on 2016/12/7.
 */
public class ServerVipData extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
    }

    @Override
    public short getType() {
        return VipPacketSet.S_VIPDATA;
    }

}
