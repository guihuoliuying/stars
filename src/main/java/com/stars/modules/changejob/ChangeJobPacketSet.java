package com.stars.modules.changejob;

import com.stars.modules.changejob.packet.ClientChangeJobPacket;
import com.stars.modules.changejob.packet.ServerChangeJobPacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/5/24.
 */
public class ChangeJobPacketSet extends PacketSet {
    public static final short S_CHANGE_JOB = 0x0280;
    public static final short C_CHANGE_JOB = 0x0281;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> packets = new ArrayList<>();
        packets.add(ServerChangeJobPacket.class);
        packets.add(ClientChangeJobPacket.class);
        return packets;
    }
}
