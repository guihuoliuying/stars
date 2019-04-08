package com.stars.modules.guardofficial;

import com.stars.modules.guardofficial.packet.ClientGuardOfficial;
import com.stars.modules.guardofficial.packet.ServerGuardOfficial;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017-07-12.
 */
public class GuardOfficialPacketSet extends PacketSet {
    public static final short S_GUARDOFFICIAL = 0x02D4;//上行
    public static final short C_GUARDOFFICIAL = 0x02D5;//下行

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ClientGuardOfficial.class);
        list.add(ServerGuardOfficial.class);
        return list;
    }
}
