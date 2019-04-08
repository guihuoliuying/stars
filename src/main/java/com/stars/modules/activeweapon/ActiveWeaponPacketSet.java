package com.stars.modules.activeweapon;

import com.google.common.collect.Lists;
import com.stars.modules.activeweapon.packet.ClientActiveWeaponPacket;
import com.stars.modules.activeweapon.packet.ServerActiveWeaponPacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/6/15.
 */
public class ActiveWeaponPacketSet extends PacketSet {
    public static final short S_ACTIVE_WEAPON = 0x02A0;
    public static final short C_ACTIVE_WEAPON = 0x02A1;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        ArrayList<Class<? extends Packet>> arrayList = Lists.newArrayList();
        arrayList.add(ServerActiveWeaponPacket.class);
        arrayList.add(ClientActiveWeaponPacket.class);
        return arrayList;
    }
}
