package com.stars.modules.poemdungeon;

import com.stars.modules.poemdungeon.packet.ClientPoemDungeon;
import com.stars.modules.poemdungeon.packet.ServerPoemDungeon;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaopeidian on 2017/5/12.
 */
public class PoemDungeonPacketSet extends PacketSet {
    public static short Server_PoemDungeon = 0x025E;// 诗歌副本上行
    public static short Client_PoemDungeon = 0x025F;// 诗歌副本下行

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerPoemDungeon.class);
        list.add(ClientPoemDungeon.class);
        return list;
    }
}
