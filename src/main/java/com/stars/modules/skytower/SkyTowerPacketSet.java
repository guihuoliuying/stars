package com.stars.modules.skytower;

import com.stars.modules.skytower.packet.ClientSkyTowerInfo;
import com.stars.modules.skytower.packet.ClientSkyTowerVo;
import com.stars.modules.skytower.packet.ServerSkyTowerInfo;
import com.stars.modules.skytower.packet.ServerSkyTowerVo;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * 镇妖塔协议相关;
 * Created by panzhenfeng on 2016/8/10.
 */
public class SkyTowerPacketSet extends PacketSet {

    public static final short C_SKYTOWER_INFO = 0x0080;
    public static final short S_SKYTOWER_INFO = 0x0081;
    public static final short C_SKYTOWER_VO = 0x0082;
    public static final short S_SKYTOWER_VO = 0x0083;

    public SkyTowerPacketSet() {

    }

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerSkyTowerInfo.class);
        al.add(ClientSkyTowerInfo.class);
        al.add(ServerSkyTowerVo.class);
        al.add(ClientSkyTowerVo.class);
        return al;
    }

}