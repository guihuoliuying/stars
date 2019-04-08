package com.stars.modules.retrievereward;

import com.stars.modules.retrievereward.packet.ClientRetrieveReward;
import com.stars.modules.retrievereward.packet.ServerRetrieveReward;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaopeidian on 2016/12/5.
 */
public class RetrieveRewardPacketSet extends PacketSet {
    public static short S_RETRIEVE_REWARD = 0x01A0;
    public static short C_RETRIEVE_REWARD = 0x01A1;
    
    public RetrieveRewardPacketSet() {

    }

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerRetrieveReward.class);
        al.add(ClientRetrieveReward.class);
        return al;
    }
}