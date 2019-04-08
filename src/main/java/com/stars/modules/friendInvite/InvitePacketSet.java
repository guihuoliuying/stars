package com.stars.modules.friendInvite;

import com.stars.modules.friendInvite.packet.ClientBeInvite;
import com.stars.modules.friendInvite.packet.ClientInvite;
import com.stars.modules.friendInvite.packet.ClientServerInfo;
import com.stars.modules.friendInvite.packet.ServerInvite;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxie on 2017/6/8.
 */
public class InvitePacketSet extends PacketSet {

    public static short S_INVATE = 0x028A;          // 好友邀请上行
    public static short C_INVATE = 0x028B;          // 好友邀请下行（邀请方）
    public static short C_BE_INVATE = 0x028C;       // 好友邀请下行（被邀请方）
    public static short C_SERVER_INFO = 0x028D;     // 好友邀请下行（邀请方服务器信息）

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList();
        list.add(ClientInvite.class);
        list.add(ServerInvite.class);
        list.add(ClientBeInvite.class);
        list.add(ClientServerInfo.class);
        return list;
    }

}
