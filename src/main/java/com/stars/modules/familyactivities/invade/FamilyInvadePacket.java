package com.stars.modules.familyactivities.invade;

import com.stars.modules.familyactivities.invade.packet.ClientFamilyInvade;
import com.stars.modules.familyactivities.invade.packet.ClientFamilyInvadeNotice;
import com.stars.modules.familyactivities.invade.packet.ServerFamilyInvade;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/10/17.
 */
public class FamilyInvadePacket extends PacketSet {
//    public static short S_INVADE_TEAM = 0x6078;// 家族入侵组队请求------已废弃
//    public static short C_INVADE_TEAM = 0x6079;// 家族入侵组队响应------已废弃
    public static short C_INVADE_NOTICE = 0x607A;// 活动开始结束通知
    public static short S_INVADE = 0x607B;// 家族入侵操作请求
    public static short C_INVADE = 0x607C;// 家族入侵操作响应

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ClientFamilyInvadeNotice.class);
        list.add(ServerFamilyInvade.class);
        list.add(ClientFamilyInvade.class);
        return list;
    }
}
