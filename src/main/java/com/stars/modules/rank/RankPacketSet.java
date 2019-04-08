package com.stars.modules.rank;

import com.stars.modules.rank.packet.ClientRank;
import com.stars.modules.rank.packet.ServerRank;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/8/24.
 */
public class RankPacketSet extends PacketSet {
    public static short S_RANK = 0x6050;// 排行榜上行
    public static short C_RANK = 0x6051;// 排行榜下行

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ServerRank.class);
        al.add(ClientRank.class);
        return al;
    }
}
