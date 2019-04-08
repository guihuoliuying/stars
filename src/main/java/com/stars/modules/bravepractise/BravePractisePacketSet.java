package com.stars.modules.bravepractise;

import com.stars.modules.bravepractise.packet.ClientBravePageInfo;
import com.stars.modules.bravepractise.packet.ClientBravePassAward;
import com.stars.modules.bravepractise.packet.ServerBravePageInfo;
import com.stars.modules.bravepractise.packet.ServerJoinBrave;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaopeidian on 2016/11/16.
 */
public class BravePractisePacketSet extends PacketSet {
    public static short S_BRAVE_PAGE_INFO = 0x0168;
    public static short C_BRAVE_PAGE_INFO = 0x0169;
    
    public static short S_JOIN_BRAVE = 0x016A;
    
    public static short C_PASS_AWARD = 0x016B;
    
    public BravePractisePacketSet() {

    }

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerBravePageInfo.class);
        al.add(ClientBravePageInfo.class);
        al.add(ServerJoinBrave.class);
        al.add(ClientBravePassAward.class);
        return al;
    }

}