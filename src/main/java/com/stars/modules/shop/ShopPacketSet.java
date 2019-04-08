package com.stars.modules.shop;

import com.stars.modules.shop.packet.ClientShopData;
import com.stars.modules.shop.packet.ServerShopData;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyaohui on 2016/9/5.
 */
public class ShopPacketSet extends PacketSet {

    public final static short C_SHOP_PRODUCT = 0x0090; // 发送商店产品数据
    public final static short S_SHOP_PRODUCT = 0x0091;  // 请求产品数据

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ClientShopData.class);
        list.add(ServerShopData.class);
        return list;
    }
}
