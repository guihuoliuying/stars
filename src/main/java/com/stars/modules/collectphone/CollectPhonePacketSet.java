package com.stars.modules.collectphone;

import com.stars.modules.collectphone.packet.ClientCollectPhonePacket;
import com.stars.modules.collectphone.packet.ServerCollectPhonePacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/9/13.
 */
public class CollectPhonePacketSet extends PacketSet {
    public static final short S_COLLECT_PHONE = 0x02FA;
    public static final short C_COLLECT_PHONE = 0x02FB;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> packsets = new ArrayList<>();
        packsets.add(ServerCollectPhonePacket.class);
        packsets.add(ClientCollectPhonePacket.class);
        return packsets;
    }
}
