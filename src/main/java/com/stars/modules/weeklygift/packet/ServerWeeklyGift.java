package com.stars.modules.weeklygift.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.weeklygift.WeeklyGiftPacketSet;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2017-06-28.
 */
public class ServerWeeklyGift extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
        LogUtil.info("收包:{}=====", getRoleId());
        ServiceHelper.weeklyGiftService().view(getRoleId());
    }

    @Override
    public short getType() {
        return WeeklyGiftPacketSet.S_WEEKLYGIFT;
    }
}
