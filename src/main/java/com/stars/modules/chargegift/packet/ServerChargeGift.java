package com.stars.modules.chargegift.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.chargegift.ChargeGiftModule;
import com.stars.modules.chargegift.ChargeGiftPacketSet;

/**
 * Created by chenxie on 2017/5/18.
 */
public class ServerChargeGift extends PlayerPacket {

    @Override
    public void execPacket(Player player) {
        ChargeGiftModule chargeGiftModule = module(MConst.ChargeGift);
        chargeGiftModule.viewMainUI();
    }

    @Override
    public short getType() {
        return ChargeGiftPacketSet.S_CHARGEGIFT;
    }
}
