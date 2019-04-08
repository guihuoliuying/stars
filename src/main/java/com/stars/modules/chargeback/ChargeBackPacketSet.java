package com.stars.modules.chargeback;

import com.stars.modules.chargeback.packet.ClientChargeBackPacket;
import com.stars.modules.chargeback.packet.ServerChargeBackPacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/3/20.
 */
public class ChargeBackPacketSet extends PacketSet {
    public static short serverChargeBack = 0x0214;//客户端发过来的数据请求
    public static short clientChargeBack = 0x0215;//服务器返回的数据请求
    List<Class<? extends com.stars.network.server.packet.Packet>> packets = new ArrayList<>();

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        packets.add(ClientChargeBackPacket.class);
        packets.add(ServerChargeBackPacket.class);
        return packets;
    }
}
