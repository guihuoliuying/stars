package com.stars.modules.data;

import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangjiahua on 2016/3/8.
 */
public class DataPacketSet extends PacketSet {
    public DataPacketSet() {
    }

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        // TODO Auto-generated method stub
        List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
        return al;
    }
}
