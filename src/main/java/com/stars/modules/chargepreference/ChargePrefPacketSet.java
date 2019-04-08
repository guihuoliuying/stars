package com.stars.modules.chargepreference;

import com.stars.modules.chargepreference.packet.ClientChargePref;
import com.stars.modules.chargepreference.packet.ServerChargePref;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2017/3/30.
 */
public class ChargePrefPacketSet extends PacketSet {

    public static final short S_CHARGE_PREF = 0x022A;
    public static final short C_CHARGE_PREF = 0x022B;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerChargePref.class);
        list.add(ClientChargePref.class);
        return list;
    }

}
