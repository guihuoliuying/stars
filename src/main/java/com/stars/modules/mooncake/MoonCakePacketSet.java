package com.stars.modules.mooncake;

import com.stars.modules.mooncake.packet.ClientMoonCake;
import com.stars.modules.mooncake.packet.ServerMoonCake;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangerjiang on 2017/9/14.
 */
public class MoonCakePacketSet extends PacketSet {
    public static short S_MOONCAKE = 0x02FE;//上行
    public static short C_MOONCAKE = 0x02FF;//下行

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends com.stars.network.server.packet.Packet>> list = new ArrayList<Class<? extends Packet>>();

        list.add(ServerMoonCake.class);
        list.add(ClientMoonCake.class);
        return list;
    }
}
