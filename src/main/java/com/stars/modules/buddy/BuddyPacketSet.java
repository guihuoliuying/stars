package com.stars.modules.buddy;

import com.stars.modules.buddy.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/8/5.
 */
public class BuddyPacketSet extends PacketSet {
    public static short S_ALL_BUDDY_DATA = 0x0070;// 打开伙伴界面请求
    public static short C_ALL_BUDDY_DATA = 0x0071;// 打开伙伴界面下发数据
    // 包体太大,可能拆分为:1.玩家头像列表数据;2.单个伙伴产品数据请求(等级,阶级,飞升等级)
    public static short S_BUDDY = 0x0072;// 伙伴操作
    public static short C_UPDATE_BUDDY = 0x0073;// 更新伙伴数据
    public static short S_LINEUP = 0x0074;// 阵型操作
    public static short C_UPDATE_LINEUP = 0x0075;// 更新阵型数据
    public static short C_FOLLOWBUDDY_DATA = 0x0076;// 下发跟随伙伴数据
    public static short S_BUDDY_GUARD = 0x0077;// 上行伙伴守卫
    public static short C_BUDDY_GUARD = 0x0078;// 下行伙伴守卫

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ServerAllBuddyData.class);
        al.add(ClientAllBuddyData.class);
        al.add(ServerBuddy.class);
        al.add(ClientUpdateBuddy.class);
        al.add(ServerLineup.class);
        al.add(ClientUpdateLineup.class);
        al.add(ClientFollowBuddyData.class);
        al.add(ServerGuardPacket.class);
        al.add(ClientGuardPacket.class);
        return al;
    }
}
