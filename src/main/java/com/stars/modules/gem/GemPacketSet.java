package com.stars.modules.gem;

import com.stars.modules.gem.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * 装备相关的协议;
 * Created by panzhenfeng on 2016/6/24.
 */
public class GemPacketSet extends PacketSet {
    /**响应客户端请求所有装备提升信息*/
    public static short C_ROLE_GEM_INFO = 0x0062;
    public static short S_ROLE_GEM_INFO = 0x0063;
    /**响应客户端请求装备提升操作*/
    public static short C_GEM_TISHEN_OPR = 0x0064;
    /**客户端请求装备提升操作;*/
    public static short S_GEM_TISHEN_OPR = 0x0065;
    /**客户端请求装备提升的VO数据;*/
    public static short S_EQUIPMENT_TISHEN_VO = 0x0068;
    /**响应客户端请求装备提升的VO数据;*/
    public static short C_EQUIPMENT_TISHEN_VO = 0x0069;
    /**客户端请求宝石可合成列表;*/
    public static short S_EQUIPMENT_ALLGEM_COMPOSEINFO = 0x006C;
    /**响应客户端请求宝石可合成列表;*/
    public static short C_EQUIPMENT_ALLGEM_COMPOSEINFO = 0x006D;
    /**推送客户端的响应信息;*/
    public static short C_EQUIPMENT_RESPONSE = 0x006E;

    public GemPacketSet() {

    }

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ClientRoleGemInfo.class);
        al.add(ServerGemTishenOpr.class);
        al.add(ClientGemTishenOpr.class);
        al.add(ServerGemTishenVo.class);
        al.add(ClientGemTishenVo.class);
        al.add(ServerGemAllGemComposeInfo.class);
        al.add(ClientGemAllComposeInfo.class);
        al.add(ClientGemResponse.class);
        al.add(ServerRoleGemInfo.class);
        return al;
    }

}