package com.stars.modules.familyTask;

import com.stars.modules.familyTask.packet.ClientFamilyTask;
import com.stars.modules.familyTask.packet.ServerFamilyTask;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyuxing on 2017/3/28.
 */
public class FamilyTaskPacketSet extends PacketSet {

    public static short S_FAMILYTASK = 0x6224;// 上行
    public static short C_FAMILYTASK = 0x6225;// 下行

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends com.stars.network.server.packet.Packet>> list = new ArrayList<Class<? extends Packet>>();
        list.add(ServerFamilyTask.class);
        list.add(ClientFamilyTask.class);
        return list;
    }
}
