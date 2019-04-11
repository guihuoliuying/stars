package com.stars.modules.foreshow;


import com.stars.modules.foreshow.packet.ClientForeShow;
import com.stars.modules.foreshow.packet.ServerForeShow;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2016/10/28.
 */
public class ForeShowPacketSet extends PacketSet {

    public static final short C_FORESHOW = 0x0160;//响应
    public static final short S_FORESHOW = 0x0161;//请求

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ServerForeShow.class);
        al.add(ClientForeShow.class);
        return al;
    }
}
