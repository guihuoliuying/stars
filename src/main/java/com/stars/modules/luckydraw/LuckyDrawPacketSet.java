package com.stars.modules.luckydraw;

import com.stars.modules.luckydraw.packet.ClientLuckyDrawPacket;
import com.stars.modules.luckydraw.packet.ServerLuckyDrawPacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDrawPacketSet extends PacketSet {
    public static short S_LUCKY_DRAW = 0x02F2;
    public static short C_LUCKY_DRAW = 0x02F3;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> packets = new ArrayList<>();
        packets.add(ServerLuckyDrawPacket.class);
        packets.add(ClientLuckyDrawPacket.class);
        return packets;
    }
}
