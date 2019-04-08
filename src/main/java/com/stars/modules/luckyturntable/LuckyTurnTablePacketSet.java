package com.stars.modules.luckyturntable;

import com.stars.modules.luckyturntable.packet.ClientLuckyTurnTable;
import com.stars.modules.luckyturntable.packet.ServerLuckyTurnTable;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017-07-12.
 */
public class LuckyTurnTablePacketSet extends PacketSet {
    public static final short S_LUCKYTURNTABLE = 0x02D8;//上行
    public static final short C_LUCKYTURNTABLE = 0x02D9;//下行

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ClientLuckyTurnTable.class);
        list.add(ServerLuckyTurnTable.class);
        return list;
    }
}
