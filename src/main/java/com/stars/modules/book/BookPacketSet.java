package com.stars.modules.book;

import com.stars.modules.book.packet.ClientBook;
import com.stars.modules.book.packet.ServerBook;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by zhoujin on 2017/5/9.
 */
public class BookPacketSet extends PacketSet {
    public final static short C_BOOK = 0x0266; // 下行
    public final static short S_BOOK = 0x0267; // 上行
    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ClientBook.class);
        al.add(ServerBook.class);
        return al;
    }
}
