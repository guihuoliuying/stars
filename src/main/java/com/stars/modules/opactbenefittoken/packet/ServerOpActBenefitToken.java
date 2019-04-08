package com.stars.modules.opactbenefittoken.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.opactbenefittoken.OpActBenefitTokenModule;
import com.stars.modules.opactbenefittoken.OpActBenefitTokenPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2017/6/16.
 */
public class ServerOpActBenefitToken extends PlayerPacket {

    private byte subtype;
    private static final byte REQ_LIMIT_TIME = 1; // 请求副本次数
    private static final byte REQ_ENTER_SCENE = 2; // 请求进入副本

    public ServerOpActBenefitToken() {
    }

    @Override
    public short getType() {
        return OpActBenefitTokenPacketSet.S_OPACT_BENEFIT_TOKEN;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
    }

    @Override
    public void execPacket(Player player) {
        OpActBenefitTokenModule module = module(MConst.OpActBenefitToken);
        switch (subtype) {
            case REQ_LIMIT_TIME:
                module.sendTimes();
                break;
            case REQ_ENTER_SCENE:
                module.enterScene();
                break;
        }
    }

}
