package com.stars.modules.induct;

import com.stars.modules.induct.packet.ClientInduct;
import com.stars.modules.induct.packet.ServerInduct;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/8/18.
 */
public class InductPacketSet extends PacketSet {
    public static short S_INDUCT = 0x0056;// 引导上行
//        byte 1:// 触发引导
//        byte 2:// 完成引导
    public static short C_INDUCT = 0x0057;// 引导下行
//        byte UPDATE_INDUCT = 1;// 更新玩家引导记录

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ServerInduct.class);
        al.add(ClientInduct.class);
        return al;
    }
}
