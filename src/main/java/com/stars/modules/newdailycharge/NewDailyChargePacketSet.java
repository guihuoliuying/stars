package com.stars.modules.newdailycharge;

import com.stars.modules.newdailycharge.packet.ClientNewDailyCharge;
import com.stars.modules.newdailycharge.packet.ServerNewDailyCharge;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017-07-10.
 */
public class NewDailyChargePacketSet extends PacketSet {
    public static final short C_NEW_DAILY_CHARGE = 0x02D0;
    public static final short S_NEW_DAILY_CHARGE = 0x02D1;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ServerNewDailyCharge.class);
        al.add(ClientNewDailyCharge.class);
        return al;
    }
}
