package com.stars.modules.giftcome520;

import com.stars.modules.giftcome520.packet.ClientGiftComePacket;
import com.stars.modules.giftcome520.packet.ServerGiftComePacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/4/15.
 */
public class GiftComePacketSet extends PacketSet {
    public final static short S_GiftCome = 0x023E;
    public final static short C_GiftCome = 0x023F;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> packetList = new ArrayList<>();
        packetList.add(ClientGiftComePacket.class);
        packetList.add(ServerGiftComePacket.class);
        return packetList;
    }
}
