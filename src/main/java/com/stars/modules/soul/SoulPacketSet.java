package com.stars.modules.soul;

import com.stars.modules.soul.packet.ClientSoulPacket;
import com.stars.modules.soul.packet.ServerSoulPacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class SoulPacketSet extends PacketSet {
    public static short S_SOUL = 0x0310;
    public static short C_SOUL = 0x0311;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> packets = new ArrayList<>();
        packets.add(ServerSoulPacket.class);
        packets.add(ClientSoulPacket.class);
        return packets;
    }
}
