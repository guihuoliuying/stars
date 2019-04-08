package com.stars.modules.opactbenefittoken;

import com.stars.modules.opactbenefittoken.packet.ClientOpActBenefitToken;
import com.stars.modules.opactbenefittoken.packet.ServerOpActBenefitToken;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.Arrays;
import java.util.List;

/**
 * Created by zhaowenshuo on 2017/6/16.
 */
public class OpActBenefitTokenPacketSet extends PacketSet {

    public static final short S_OPACT_BENEFIT_TOKEN = 0x0292;
    public static final short C_OPACT_BENEFIT_TOKEN = 0x0293;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        return Arrays.<Class<? extends Packet>>asList(
                ServerOpActBenefitToken.class,
                ClientOpActBenefitToken.class);
    }
}
