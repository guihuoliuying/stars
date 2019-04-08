package com.stars.modules.newserversign;

import com.stars.modules.newserversign.packet.ClientNewServerSign;
import com.stars.modules.newserversign.packet.ServerNewServerSign;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaopeidian on 2016/12/20.
 */
public class NewServerSignPacketSet extends PacketSet {
    public static short S_NEW_SERVER_SIGN = 0x01B5;
    public static short C_NEW_SERVER_SIGN = 0x01B6;
    
    public NewServerSignPacketSet() {

    }

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerNewServerSign.class);
        al.add(ClientNewServerSign.class);
        return al;
    }
}