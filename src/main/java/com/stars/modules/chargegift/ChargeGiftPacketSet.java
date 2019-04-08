package com.stars.modules.chargegift;

import com.stars.modules.chargegift.packet.ClientChargeGift;
import com.stars.modules.chargegift.packet.ServerChargeGift;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxie on 2017/5/18.
 */
public class ChargeGiftPacketSet extends PacketSet {

    public static short S_CHARGEGIFT = 0x026B;// 上行
    public static short C_CHARGEGIFT = 0x026C;// 下行

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends com.stars.network.server.packet.Packet>> list = new ArrayList<Class<? extends Packet>>();
        list.add(ServerChargeGift.class);
        list.add(ClientChargeGift.class);
        return list;
    }
}
