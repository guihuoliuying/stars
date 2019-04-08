package com.stars.modules.mind;

import com.stars.modules.mind.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * 心法协议相关;
 * Created by gaopeidian on 2016/9/21.
 */
public class MindPacketSet extends PacketSet {
    public static short S_MIND_VO = 0x0138;
    public static short C_MIND_VO = 0x0139;
    
    public static short S_MIND_INFO = 0x013A;
    public static short C_MIND_INFO = 0x013B;
    
    public static short S_UPGRADE_MIND = 0x013C;
    public static short C_UPGRADE_MIND = 0x013D;
    
    public MindPacketSet() {

    }

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerMindVo.class);
        al.add(ClientMindVo.class);
        al.add(ServerMindInfo.class);
        al.add(ClientMindInfo.class);
        al.add(ServerUpgradeMind.class);
        al.add(ClientUpgradeMind.class);
        return al;
    }

}