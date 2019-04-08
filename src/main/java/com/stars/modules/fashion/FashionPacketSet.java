package com.stars.modules.fashion;

import com.stars.modules.fashion.packet.ClientFashion;
import com.stars.modules.fashion.packet.ServerFashion;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * 时装协议相关;
 * Created by gaopeidian on 2016/10/08.
 */
public class FashionPacketSet extends PacketSet {
    public static short S_FASHION = 0x0150;
    public static short C_FASHION = 0x0151;
    
    public FashionPacketSet() {

    }

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerFashion.class);
        al.add(ClientFashion.class);
        return al;
    }

}