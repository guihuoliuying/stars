package com.stars.modules.familyactivities.treasure;

import com.stars.modules.familyactivities.treasure.packet.ClientFamilyTreasure;
import com.stars.modules.familyactivities.treasure.packet.ServerFamilyTreasure;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017/2/10 11:30
 */
public class FamilyTreasurePacket extends PacketSet {
    public static short S_TREASURE = 0x6200;//上行协议
    public static short C_TREASURE = 0x6201;//下行协议
    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ServerFamilyTreasure.class);
        al.add(ClientFamilyTreasure.class);
        return al;
    }
}
