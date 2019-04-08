package com.stars.modules.operateactivity;

import com.stars.modules.operateactivity.packet.ClientAllActivityInfo;
import com.stars.modules.operateactivity.packet.ServerAllActivityInfo;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaopeidian on 2016/12/7.
 */
public class OperateActivityPacketSet extends PacketSet {
    public static short S_ALL_ACTIVITY_INFO = 0x0190;
    public static short C_ALL_ACTIVITY_INFO = 0x0191;
    
    public OperateActivityPacketSet() {

    }

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerAllActivityInfo.class);
        al.add(ClientAllActivityInfo.class);
        return al;
    }
}