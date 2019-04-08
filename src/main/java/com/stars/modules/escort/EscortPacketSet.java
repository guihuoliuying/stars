package com.stars.modules.escort;

import com.stars.modules.escort.packet.ClientEscort;
import com.stars.modules.escort.packet.ServerEscort;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyuxing on 2016/12/2.
 */
public class EscortPacketSet extends PacketSet {

    public static short S_ESCORT = 0x019C;// 上行
    public static short C_ESCORT = 0x019D;// 下行
    public static short C_ESCORT_SAFE = 0x019E;// 下行

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends com.stars.network.server.packet.Packet>> list = new ArrayList<Class<? extends Packet>>();
        list.add(ServerEscort.class);
        list.add(ClientEscort.class);
        return list;
    }
}
