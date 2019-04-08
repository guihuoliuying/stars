package com.stars.modules.offlinepvp.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.offlinepvp.OfflinePvpPacketSet;

/**
 * Created by liuyuheng on 2016/10/12.
 */
public class ServerOfflinePvpData extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
//        OfflinePvpModule opm = module(MConst.OfflinePvp);
//        opm.reqAllData();
    }

    @Override
    public short getType() {
        return OfflinePvpPacketSet.S_OFFLINEPVP_DATA;
    }
}
