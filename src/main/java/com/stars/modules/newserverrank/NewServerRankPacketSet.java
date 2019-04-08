package com.stars.modules.newserverrank;

import com.stars.modules.newserverrank.packet.ClientNewServerRank;
import com.stars.modules.newserverrank.packet.ServerNewServerRank;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaopeidian on 2016/12/20.
 */
public class NewServerRankPacketSet extends PacketSet {
    public static short S_NEW_SERVER_RANK = 0x01AE;
    public static short C_NEW_SERVER_RANK = 0x01AF;
    
    public NewServerRankPacketSet() {

    }

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerNewServerRank.class);
        al.add(ClientNewServerRank.class);
        return al;
    }
}