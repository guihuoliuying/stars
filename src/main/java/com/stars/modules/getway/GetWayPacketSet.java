package com.stars.modules.getway;

import com.stars.modules.getway.packet.ClientGetWay;
import com.stars.modules.getway.packet.ServerGetWay;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.Arrays;
import java.util.List;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class GetWayPacketSet extends PacketSet {

    public static final int S_GETWAY = 0x02A4;
    public static final int C_GETWAY = 0x02A5;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        return Arrays.<Class<? extends Packet>>asList(
                ServerGetWay.class,
                ClientGetWay.class);
    }
}
