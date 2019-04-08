package com.stars.modules.dailyCharge;

import com.stars.modules.dailyCharge.packet.ClientDailyCharge;
import com.stars.modules.dailyCharge.packet.ServerDailyCharge;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyuxing on 2017/3/29.
 */
public class DailyChargePacketSet extends PacketSet {

    public static short S_DAILYCHARGE = 0x6229;// 上行
    public static short C_DAILYCHARGE = 0x622A;// 下行

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<Class<? extends Packet>>();
        list.add(ServerDailyCharge.class);
        list.add(ClientDailyCharge.class);
        return list;
    }
}
