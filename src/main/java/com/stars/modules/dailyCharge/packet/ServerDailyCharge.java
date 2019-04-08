package com.stars.modules.dailyCharge.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.dailyCharge.DailyChargeModule;
import com.stars.modules.dailyCharge.DailyChargePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by wuyuxing on 2017/3/29.
 */
public class ServerDailyCharge extends PlayerPacket {

    public static final byte REQ_VIEW = 0x01; // 请求打开活动界面

    public byte subtype;

    @Override
    public void execPacket(Player player) {
        DailyChargeModule dailyChargeModule = module(MConst.DailyCharge);
        switch (subtype) {
            case REQ_VIEW:
                dailyChargeModule.viewMainUI();
                break;
        }
    }

    @Override
    public short getType() {
        return DailyChargePacketSet.S_DAILYCHARGE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case REQ_VIEW:

                break;
        }
    }
}
