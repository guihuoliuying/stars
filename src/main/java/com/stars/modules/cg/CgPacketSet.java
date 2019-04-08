package com.stars.modules.cg;

import com.stars.modules.cg.packet.ClientCg;
import com.stars.modules.cg.packet.ServerCg;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panzhenfeng on 2017/3/7.
 */
public class CgPacketSet  extends PacketSet {
    public static final short C_CG = 0x6209;
    public static final short S_CG = 0x620A;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ServerCg.class);
        al.add(ClientCg.class);
        return al;
    }
}

