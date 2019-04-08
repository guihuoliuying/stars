package com.stars.modules.dragonboat;

import com.stars.modules.dragonboat.packet.ClientDragonBoatPacket;
import com.stars.modules.dragonboat.packet.ServerDragonBoatPacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/5/9.
 */
public class DragonBoatPacketSet extends PacketSet {
    public static final short S_DragonBoat = 0x025A;
    public static final short C_DragonBoat = 0x025B;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> packetList = new ArrayList<>();
        packetList.add(ClientDragonBoatPacket.class);
        packetList.add(ServerDragonBoatPacket.class);
        return packetList;
    }
}
