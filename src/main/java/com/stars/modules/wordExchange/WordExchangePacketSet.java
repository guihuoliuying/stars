package com.stars.modules.wordExchange;

import com.stars.modules.wordExchange.packet.ClientWordExchange;
import com.stars.modules.wordExchange.packet.ServerWordExchange;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyuxing on 2016/11/7.
 */
public class WordExchangePacketSet extends PacketSet {

    public static short S_WORDEXCHANGE = 0x6210;// 上行
    public static short C_WORDEXCHANGE = 0x6211;// 下行

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<Class<? extends Packet>>();
        list.add(ServerWordExchange.class);
        list.add(ClientWordExchange.class);
        return list;
    }
}
