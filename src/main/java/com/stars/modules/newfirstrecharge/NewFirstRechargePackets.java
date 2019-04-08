package com.stars.modules.newfirstrecharge;

import com.stars.modules.newfirstrecharge.packet.ClientNewFirstRechargePacket;
import com.stars.modules.newfirstrecharge.packet.ServerNewFirstRechargePacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/9/6.
 */
public class NewFirstRechargePackets extends PacketSet {
    public static short S_FIRST_RECHARGE = 0x02F6;
    public static short C_FIRST_RECHARGE = 0x02F7;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list=new ArrayList<>();
        list.add(ServerNewFirstRechargePacket.class);
        list.add(ClientNewFirstRechargePacket.class);
        return list;
    }
}
