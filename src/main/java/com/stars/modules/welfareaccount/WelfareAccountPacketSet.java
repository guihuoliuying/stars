package com.stars.modules.welfareaccount;

import com.stars.modules.welfareaccount.packet.ClientWelfareAccountPacket;
import com.stars.modules.welfareaccount.packet.ServerWelfareAccountPacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/4/11.
 */
public class WelfareAccountPacketSet extends PacketSet {
    public static final short S_VIRTUALMONERY = 0x023A;//上行福利号请求
    public static final short C_VIRTUALMONERY = 0x023B;//下发福利号信息

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> packets = new ArrayList<>();
        packets.add(ClientWelfareAccountPacket.class);
        packets.add(ServerWelfareAccountPacket.class);
        return packets;
    }
}
