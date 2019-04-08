package com.stars.modules.vip.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.vip.VipManager;
import com.stars.modules.vip.VipModule;
import com.stars.modules.vip.VipPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/12/7.
 */
public class ServerVipOption extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    @Override
    public void execPacket(Player player) {
        VipModule vipModule = module(MConst.Vip);
        switch (reqType) {
            case 1:// 领取月卡每日奖励
                vipModule.rewardMonthCard();
                break;
            case 2://请求充值数据
            	send(new ClientChargeData(VipManager.chargeVoMap.get("zyy")));
        }
    }

    @Override
    public short getType() {
        return VipPacketSet.S_VIPOPTION;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
        }
    }
}
