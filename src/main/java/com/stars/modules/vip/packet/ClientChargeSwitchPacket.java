package com.stars.modules.vip.packet;

import com.stars.modules.vip.VipManager;
import com.stars.modules.vip.VipPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * 下发充值开关设置
 * Created by huwenjun on 2017/3/31.
 */
public class ClientChargeSwitchPacket extends Packet {

    @Override
    public short getType() {
        return VipPacketSet.C_CHARGESWITCH;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte((byte) VipManager.chargeSwitchState);
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }
}
