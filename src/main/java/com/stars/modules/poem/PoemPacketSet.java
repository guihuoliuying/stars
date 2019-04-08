package com.stars.modules.poem;

import com.stars.modules.poem.packet.ClientPoem;
import com.stars.modules.poem.packet.ClientPoemBoss;
import com.stars.modules.poem.packet.ServerPoem;
import com.stars.modules.poem.packet.ServerPoemBoss;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaopeidian on 2017/1/9.
 */
public class PoemPacketSet extends PacketSet {
    public static short S_POEM = 0x01DE;
    public static short C_POEM = 0x01DF;
    
    public static short S_POEM_BOSS = 0x01E1;
    public static short C_POEM_BOSS = 0x01E2;
    
    public PoemPacketSet() {

    }

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerPoem.class);
        al.add(ClientPoem.class);
        al.add(ServerPoemBoss.class);
        al.add(ClientPoemBoss.class);
        return al;
    }

}