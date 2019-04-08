package com.stars.modules.daregod;

import com.stars.modules.daregod.packet.ClientDareGod;
import com.stars.modules.daregod.packet.ServerDareGod;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017-08-23.
 */
public class DareGodPacketSet extends PacketSet {
    public static final short S_DAREGOD = 0x624A;
    public static final short C_DAREGOD = 0x624B;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerDareGod.class);
        list.add(ClientDareGod.class);
        return list;
    }
}
