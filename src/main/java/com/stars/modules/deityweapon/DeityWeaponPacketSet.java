package com.stars.modules.deityweapon;

import com.stars.modules.deityweapon.packet.ClientDeityWeaponLevelVo;
import com.stars.modules.deityweapon.packet.ClientDeityWeaponOpr;
import com.stars.modules.deityweapon.packet.ServerDeityWeaponLevelVo;
import com.stars.modules.deityweapon.packet.ServerDeityWeaponOpr;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panzhenfeng on 2016/12/14.
 */
public class DeityWeaponPacketSet extends PacketSet {

    /**客户端请求神兵操作;*/
    public static short S_DEITYWEAPON_OPR = 0x61aa;
    /**响应客户端请求神兵操作;*/
    public static short C_DEITYWEAPON_OPR = 0x61ab;
    /**客户端请求神兵等级数据;*/
    public static short S_DEITYWEAPONLEVEL_VO = 0x61ac;
    /**响应客户端请求神兵等级数据;*/
    public static short C_DEITYWEAPONLEVEL_VO = 0x61ad;

    public DeityWeaponPacketSet(){

    }

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerDeityWeaponOpr.class);
        al.add(ClientDeityWeaponOpr.class);
        al.add(ServerDeityWeaponLevelVo.class);
        al.add(ClientDeityWeaponLevelVo.class);
        return al;
    }
}
