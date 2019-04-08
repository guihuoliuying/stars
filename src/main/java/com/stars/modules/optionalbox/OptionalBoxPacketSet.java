package com.stars.modules.optionalbox;

import com.stars.modules.optionalbox.packet.ClientOptionalBoxPacket;
import com.stars.modules.optionalbox.packet.ServerOptionalBoxPacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class OptionalBoxPacketSet extends PacketSet {
    public static short S_TOOLCHOOSE = 0x0314;
    public static short C_TOOLCHOOSE = 0x0315;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> packets = new ArrayList<>();
        packets.add(ServerOptionalBoxPacket.class);
        packets.add(ClientOptionalBoxPacket.class);
        return packets;
    }
}
