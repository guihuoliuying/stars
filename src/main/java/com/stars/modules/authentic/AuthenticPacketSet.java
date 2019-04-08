package com.stars.modules.authentic;

import com.stars.modules.authentic.packet.ClientAuthentic;
import com.stars.modules.authentic.packet.ClientRoleAuth;
import com.stars.modules.authentic.packet.ServerAuthentic;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2016/12/22.
 */
public class AuthenticPacketSet extends PacketSet {
    public static final short S_AUTHENTIC = 0x01B9;//请求
    public static final short C_AUTHENTIC = 0x01BA;//下发鉴宝所得的物品
    public static final short C_ROLEAUTH = 0x01BB;//下发玩家鉴宝的次数，时间等等基本信息

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ServerAuthentic.class);
        al.add(ClientAuthentic.class);
        al.add(ClientRoleAuth.class);
        return al;
    }
}
