package com.stars.modules.friend;

import com.stars.modules.friend.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2016/8/13.
 */
public class FriendPacketSet extends PacketSet {

    public static final short S_FRIEND_RECOMMENDATION = 0x6008; // 推荐好友请求
    public static final short C_FRIEND_RECOMMENDATION = 0x6009; // 推荐好友响应
    public static final short S_FRIEND = 0x600A; // 好友请求
    public static final short C_FRIEND = 0x600B; // 好友响应
    public static final short S_BLACKER = 0x600C; // 黑名单请求
    public static final short C_BLACKER = 0x600D; // 黑名单响应
    public static final short S_CONTACTS = 0x600E; // 联系人请求
    public static final short C_CONTACTS = 0x600F; // 联系人响应
    public static final short S_OTHER_DETAILS = 0x6010; // 玩家详细信息请求
    public static final short C_OTHER_DETAILS = 0x6011; // 玩家详细信息响应


    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerRecommendation.class);
        list.add(ClientRecommendation.class);
        list.add(ServerFriend.class);
        list.add(ClientFriend.class);
        list.add(ServerBlacker.class);
        list.add(ClientBlacker.class);
        list.add(ServerContacts.class);
        list.add(ClientContacts.class);
        list.add(ServerOtherDetails.class);
        list.add(ClientOtherDetails.class);
        return list;
    }
}
