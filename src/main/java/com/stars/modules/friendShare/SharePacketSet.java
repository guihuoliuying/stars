package com.stars.modules.friendShare;

import com.stars.modules.friendShare.packet.ClientShare;
import com.stars.modules.friendShare.packet.ServerShare;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxie on 2017/6/7.
 */
public class SharePacketSet extends PacketSet {

    public static short S_SHARE = 0x0284;   // 朋友圈分享上行
    public static short C_SHARE = 0x0285;   // 朋友圈分享下行

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList();
        list.add(ClientShare.class);
        list.add(ServerShare.class);
        return list;
    }

}
