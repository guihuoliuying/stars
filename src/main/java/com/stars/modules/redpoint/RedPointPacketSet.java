package com.stars.modules.redpoint;

import com.stars.modules.redpoint.packet.ClientRedPoint;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daiyaorong on 2016/11/16.
 */
public class RedPointPacketSet extends PacketSet{
    public static short C_REDPOINT = 0x017B;// 红点下发

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ClientRedPoint.class);
        return al;
    }
}
