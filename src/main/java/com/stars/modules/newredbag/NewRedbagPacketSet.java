package com.stars.modules.newredbag;

import com.stars.modules.newredbag.packet.ClientNewRedbag;
import com.stars.modules.newredbag.packet.ServerNewRedbag;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyaohui on 2017/2/14.
 */
public class NewRedbagPacketSet extends PacketSet {

    public final static short S_NEW_REDBAG = 0x0210;
    public final static short C_NEW_REDBAG = 0x0211;


    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerNewRedbag.class);
        list.add(ClientNewRedbag.class);
        return list;
    }
}
