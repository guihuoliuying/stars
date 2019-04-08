package com.stars.modules.discountgift;

import com.stars.modules.discountgift.packet.ClientDiscountGift;
import com.stars.modules.discountgift.packet.ServerDiscountGift;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxie on 2017/5/26.
 */
public class DiscountGiftPacketSet extends PacketSet {

    public static final short S_DICOUNT_GIFT = 0x027C;
    public static final short C_DICOUNT_GIFT = 0x027D;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerDiscountGift.class);
        list.add(ClientDiscountGift.class);
        return list;
    }

}
