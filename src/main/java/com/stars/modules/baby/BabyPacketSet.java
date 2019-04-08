package com.stars.modules.baby;

import com.stars.modules.baby.packet.ClientBaby;
import com.stars.modules.baby.packet.ServerBaby;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017-07-20.
 */
public class BabyPacketSet extends PacketSet {
    public static final short S_BABY = 0x02E0;
    public static final short C_BABY = 0x02E1;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ClientBaby.class);
        al.add(ServerBaby.class);
        return al;
    }
}
