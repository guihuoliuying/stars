package com.stars.modules.bestcp520;

import com.stars.modules.bestcp520.packet.ClientBestCPPacket;
import com.stars.modules.bestcp520.packet.ServerBestCPPacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class BestCPPacketSet extends PacketSet {
    public static final short S_BEST_CP = 0x0250;
    public static final short C_BEST_CP = 0x0251;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ClientBestCPPacket.class);
        list.add(ServerBestCPPacket.class);
        return list;
    }
}
