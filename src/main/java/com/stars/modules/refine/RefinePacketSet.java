package com.stars.modules.refine;

import com.stars.modules.refine.packet.ClientRefine;
import com.stars.modules.refine.packet.ServerRefine;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017-08-02.
 */
public class RefinePacketSet extends PacketSet {
    public static final short S_REFINE = 0x02F0;
    public static final short C_REFINE = 0x02F1;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerRefine.class);
        list.add(ClientRefine.class);
        return list;
    }
}
