package com.stars.modules.title;

import com.stars.modules.title.packet.ClientTitle;
import com.stars.modules.title.packet.ServerTitle;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/7/21.
 */
public class TitlePacketSet extends PacketSet {
    public static short S_TITLE = 0x0040; // 称号 上行
    public static short C_TITLE = 0x0041;// 称号 下行

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ServerTitle.class);
        al.add(ClientTitle.class);
        return al;
    }
}
