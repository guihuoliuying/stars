package com.stars.modules.dungeon;

import com.stars.modules.dungeon.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/6/21.
 */
public class DungeonPacketSet extends PacketSet {
    public static short S_WORLD = 0x0050;// 章节上行
    public static short C_WORLD = 0x0051;// 章节下行
    public static short S_DUNGEON = 0x0052;// 关卡上行
    public static short C_DUNGEON = 0x0053;// 关卡下行
    public static short S_PRODUCEDUNGEON = 0x0054;// 产出副本数据 请求
    public static short C_PRODUCEDUNGEON = 0x0055;// 产出副本数据 响应

    public DungeonPacketSet() {

    }

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        // TODO Auto-generated method stub
        List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerWorld.class);
        al.add(ClientWorld.class);
        al.add(ServerDungeon.class);
        al.add(ClientDungeon.class);
        al.add(ServerProduceDungeon.class);
        al.add(ClientProduceDungeon.class);
        return al;
    }
}
