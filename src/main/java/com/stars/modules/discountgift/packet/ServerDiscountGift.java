package com.stars.modules.discountgift.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.discountgift.DiscountGiftModule;
import com.stars.modules.discountgift.DiscountGiftPacketSet;

/**
 * Created by chenxie on 2017/5/26.
 */
public class ServerDiscountGift extends PlayerPacket {

    @Override
    public short getType() {
        return DiscountGiftPacketSet.S_DICOUNT_GIFT;
    }

    @Override
    public void execPacket(Player player) {
        DiscountGiftModule discountGiftModule = module(MConst.DiscountGift);
        discountGiftModule.view();
    }
}
