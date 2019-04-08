package com.stars.modules.fashioncard;

import com.stars.modules.fashioncard.packet.ClientFashionCard;
import com.stars.modules.fashioncard.packet.ServerFashionCard;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017-10-13.
 */
public class FashionCardPacketSet extends PacketSet {

    public static final short S_FASHION_CARD = 0x0306;
    public static final short C_FASHION_CARD = 0x0307;
    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerFashionCard.class);
        list.add(ClientFashionCard.class);
        return list;
    }
}
