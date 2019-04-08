package com.stars.modules.guest;

import com.stars.modules.guest.packet.ClientGuest;
import com.stars.modules.guest.packet.ServerGuest;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyaohui on 2017/1/3.
 */
public class GuestPacketSet extends PacketSet {
    public final static short S_GUEST = 0x01c0;
    public final static short C_GUEST = 0x01c1;


    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerGuest.class);
        list.add(ClientGuest.class);
        return list;
    }
}
