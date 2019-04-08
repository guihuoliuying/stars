package com.stars.modules.newdailycharge.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.newdailycharge.NewDailyChargeModule;
import com.stars.modules.newdailycharge.NewDailyChargePacketSet;

/**
 * Created by chenkeyu on 2017-07-10.
 */
public class ServerNewDailyCharge extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
        NewDailyChargeModule chargeModule = module(MConst.NewDailyCharge);
        chargeModule.viewMainUI();
    }

    @Override
    public short getType() {
        return NewDailyChargePacketSet.S_NEW_DAILY_CHARGE;
    }
}
