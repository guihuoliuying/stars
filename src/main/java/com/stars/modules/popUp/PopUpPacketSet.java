package com.stars.modules.popUp;

import com.stars.modules.popUp.packet.ClientPopUp;
import com.stars.modules.popUp.packet.ServerPopUp;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyuxing on 2016/11/7.
 */
public class PopUpPacketSet extends PacketSet {

    public static short S_POPUP = 0x6220;// 上行
    public static short C_POPUP = 0x6221;// 下行

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends com.stars.network.server.packet.Packet>> list = new ArrayList<Class<? extends Packet>>();
        list.add(ServerPopUp.class);
        list.add(ClientPopUp.class);
        return list;
    }
}
