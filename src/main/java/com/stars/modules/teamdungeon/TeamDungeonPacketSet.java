package com.stars.modules.teamdungeon;

import com.stars.modules.teamdungeon.packet.ClientTeamDungeonPacket;
import com.stars.modules.teamdungeon.packet.ServerTeamDungeonPacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/11/11.
 */
public class TeamDungeonPacketSet extends PacketSet {
    public static short Client_TeamDungeon = 0x0140;// 组队副本下行
    public static short Server_TeamDungeon = 0x0141;// 组队副本上行

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerTeamDungeonPacket.class);
        list.add(ClientTeamDungeonPacket.class);
        return list;
    }
}
