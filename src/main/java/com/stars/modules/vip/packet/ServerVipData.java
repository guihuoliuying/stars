package com.stars.modules.vip.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.vip.VipModule;
import com.stars.modules.vip.VipPacketSet;

/**
 * Created by liuyuheng on 2016/12/7.
 */
public class ServerVipData extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
        VipModule vipModule = module(MConst.Vip);
        vipModule.sendUpdateVipData();
    }

    @Override
    public short getType() {
        return VipPacketSet.S_VIPDATA;
    }

}
