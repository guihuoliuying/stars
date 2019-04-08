package com.stars.modules.weeklyCharge;

import com.stars.modules.weeklyCharge.packet.ClientWeeklyCharge;
import com.stars.modules.weeklyCharge.packet.ServerWeeklyCharge;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxie on 2017/5/5.
 */
public class WeeklyChargePacketSet extends PacketSet {

    public static short S_WEEKLYCHARGE = 0x0276;// 上行
    public static short C_WEEKLYCHARGE = 0x0277;// 下行

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<Class<? extends Packet>>();
        list.add(ServerWeeklyCharge.class);
        list.add(ClientWeeklyCharge.class);
        return list;
    }

}
