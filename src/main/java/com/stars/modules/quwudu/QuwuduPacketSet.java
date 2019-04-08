package com.stars.modules.quwudu;

import com.stars.modules.quwudu.packet.ClientQuwuduPacket;
import com.stars.modules.quwudu.packet.ServerQuwuduPacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/5/18.
 */
public class QuwuduPacketSet extends PacketSet {
    public static final short S_QUWUDU = 0x0218;
    public static final short C_QUWUDU = 0x0219;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> packets = new ArrayList<>();
        packets.add(ServerQuwuduPacket.class);
        packets.add(ClientQuwuduPacket.class);
        return packets;
    }
}
