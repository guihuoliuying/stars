package com.stars.modules.newservermoney;

import com.stars.modules.newservermoney.packet.ClientNewServerMoney;
import com.stars.modules.newservermoney.packet.ServerNewServerMoney;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyuheng on 2017/1/4.
 */
public class NewServerMoneyPacketSet extends PacketSet {
    public static short S_NSMONEY = 0x01C4;
    public static short C_NSMONEY = 0x01C5;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerNewServerMoney.class);
        list.add(ClientNewServerMoney.class);
        return list;
    }
}
