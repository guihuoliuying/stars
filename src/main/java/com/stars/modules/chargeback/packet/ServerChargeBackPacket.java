package com.stars.modules.chargeback.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.chargeback.ChargeBackModule;
import com.stars.modules.chargeback.ChargeBackPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/3/21.
 */
public class ServerChargeBackPacket extends PlayerPacket {
    private byte reqType;//请求类型
    private static final byte reqRule = 1;
    private static final byte reqYb = 2;

    public ServerChargeBackPacket() {

    }

    @Override
    public void execPacket(Player player) {
        ChargeBackModule module = module(MConst.ChargeBack);
        module.reqRule();
        module.reqYb();
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        super.readFromBuffer(buff);
    }

    @Override
    public short getType() {
        return ChargeBackPacketSet.serverChargeBack;
    }
}
