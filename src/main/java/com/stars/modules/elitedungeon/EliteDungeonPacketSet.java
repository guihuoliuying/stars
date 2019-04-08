package com.stars.modules.elitedungeon;

import com.stars.modules.elitedungeon.packet.ClientEliteData;
import com.stars.modules.elitedungeon.packet.ClientEliteDungeonPacket;
import com.stars.modules.elitedungeon.packet.ServerEliteData;
import com.stars.modules.elitedungeon.packet.ServerEliteDungeonPacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaopeidian on 2017/4/11.
 */
public class EliteDungeonPacketSet extends PacketSet {
    public static short Server_EliteDungeon = 0x0230;// 精英副本上行
    public static short Client_EliteDungeon = 0x0231;// 精英副本下行
    public static short Server_EliteData = 0x0232;// 精英副本数据上行
    public static short Client_EliteData = 0x0233;// 精英副本数据下行

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerEliteDungeonPacket.class);
        list.add(ClientEliteDungeonPacket.class);
        list.add(ServerEliteData.class);
        list.add(ClientEliteData.class);
        return list;
    }
}
