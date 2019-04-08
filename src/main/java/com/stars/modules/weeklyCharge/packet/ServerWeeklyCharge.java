package com.stars.modules.weeklyCharge.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.weeklyCharge.WeeklyChargeModule;
import com.stars.modules.weeklyCharge.WeeklyChargePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenxie on 2017/5/5.
 */
public class ServerWeeklyCharge extends PlayerPacket {

    @Override
    public void execPacket(Player player) {
        WeeklyChargeModule weeklyChargeModule = module(MConst.WeeklyCharge);
        weeklyChargeModule.viewMainUI();
    }

    @Override
    public short getType() {
        return WeeklyChargePacketSet.S_WEEKLYCHARGE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

}
