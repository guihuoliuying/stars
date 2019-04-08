package com.stars.modules.newofflinepvp;

import com.stars.modules.newofflinepvp.packet.ClientNewOfflinePvp;
import com.stars.modules.newofflinepvp.packet.ServerNewOfflinePvp;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017-03-08 15:35
 */
public class NewOfflinePvpPacketSet extends PacketSet {
    public static short S_OFFLINEPVP = 0x6205;
    public static short C_OFFLINEPVP = 0x6206;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ServerNewOfflinePvp.class);
        al.add(ClientNewOfflinePvp.class);
        return al;
    }
}
