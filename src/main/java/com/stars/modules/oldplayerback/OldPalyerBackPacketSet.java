package com.stars.modules.oldplayerback;

import com.stars.modules.oldplayerback.packet.ClientOldPalyerBackPacket;
import com.stars.modules.oldplayerback.packet.ServerOldPalyerBackPacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/7/13.
 */
public class OldPalyerBackPacketSet extends PacketSet {
    public static final short S_OLDPLAYERBACK = 0x02DC;
    public static final short C_OLDPLAYERBACK = 0x02DD;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> packetSet = new ArrayList<>();
        packetSet.add(ServerOldPalyerBackPacket.class);
        packetSet.add(ClientOldPalyerBackPacket.class);
        return packetSet;
    }
}
