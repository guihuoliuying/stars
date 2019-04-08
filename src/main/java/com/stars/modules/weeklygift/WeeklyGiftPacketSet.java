package com.stars.modules.weeklygift;

import com.stars.modules.weeklygift.packet.ClientWeeklyGift;
import com.stars.modules.weeklygift.packet.ServerWeeklyGift;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017-06-28.
 */
public class WeeklyGiftPacketSet extends PacketSet {
    public static short S_WEEKLYGIFT = 0x6244;
    public static short C_WEEKLYGIFT = 0x6245;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ClientWeeklyGift.class);
        list.add(ServerWeeklyGift.class);
        return list;
    }
}
