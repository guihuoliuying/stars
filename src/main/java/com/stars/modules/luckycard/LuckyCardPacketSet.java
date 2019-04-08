package com.stars.modules.luckycard;

import com.stars.modules.luckycard.packet.ClientLuckyCardPacket;
import com.stars.modules.luckycard.packet.ServerLuckyCardPacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/9/27.
 */
public class LuckyCardPacketSet extends PacketSet {
    public static final short S_LUCKY_CARD=0x0302;
    public static final short C_LUCKY_CARD =0x0303;
    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> packetSets = new ArrayList<>();
        packetSets.add(ClientLuckyCardPacket.class);
        packetSets.add(ServerLuckyCardPacket.class);
        return packetSets;
    }
}
