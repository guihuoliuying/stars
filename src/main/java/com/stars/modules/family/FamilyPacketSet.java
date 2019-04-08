package com.stars.modules.family;

import com.stars.modules.family.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2016/8/23.
 */
public class FamilyPacketSet extends PacketSet {

    public static final short S_RECOMM = 0x6020;
    public static final short C_RECOMM = 0x6021;
    public static final short S_AUTH = 0x6022;
    public static final short C_AUTH = 0x6023;
    public static final short S_MANAGEMENT = 0x6024; // 管理相关请求
    public static final short C_MANAGEMENT = 0x6025; // 管理相关响应
    public static final short S_CONTRIBUTION = 0x6026; // 占坑
    public static final short C_CONTRIBUTION = 0x6027; // 同步贡献数据
    public static final short S_DONATE = 0x6028; // 捐献请求
    public static final short C_DONATE = 0x6029; // 捐献响应
    public static final short S_EVENT = 0x602A; // 事迹请求
    public static final short C_EVENT = 0x602B; // 事迹响应

    /* 福利相关 */
    public static final short S_RED_PACKET = 0x6030; // 红包请求
    public static final short C_RED_PACKET = 0x6031; // 红包响应
    public static final short S_SKILL = 0x6032; // 心法请求
    public static final short C_SKILL = 0x6033; // 心法响应

    /* 活动相关 */
    public static final short S_ACT_ENTRY = 0x6040; // 活动入口请求
    public static final short C_ACT_ENTRY = 0x6041; // 活动入口响应

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerFamilyRecommendation.class);
        list.add(ClientFamilyRecommendation.class);
        list.add(ServerFamilyAuth.class);
        list.add(ClientFamilyAuth.class);
        list.add(ServerFamilyManagement.class);
        list.add(ClientFamilyManagement.class);
        list.add(ServerFamilyContribution.class);
        list.add(ClientFamilyContribution.class);
        list.add(ServerFamilyDonate.class);
        list.add(ClientFamilyDonate.class);
        list.add(ServerFamilyEvent.class); // 家族事迹
        list.add(ClientFamilyEvent.class);
        /* 福利 */
        list.add(ServerFamilyRedPacket.class);
        list.add(ClientFamilyRedPacket.class);
        list.add(ServerFamilySkill.class);
        list.add(ClientFamilySkill.class);
        /* 活动入口 */
        list.add(ServerFamilyActEntry.class);
        list.add(ClientFamilyActEntry.class);
        return list;
    }
}
