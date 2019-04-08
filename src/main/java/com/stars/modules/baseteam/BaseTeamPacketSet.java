package com.stars.modules.baseteam;

import com.stars.modules.baseteam.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/11/9.
 */
public class BaseTeamPacketSet extends PacketSet {
    public static short Client_TeamApply = 0x0142;// 申请下行
    public static short Server_TeamApply = 0x0143;// 申请上行
    public static short Client_TeamInfo = 0x0144;// 队伍信息下行
    public static short Server_TeamOption = 0x0145;// 队伍操作上行
    public static short Client_TeamInvite = 0x0146;// 邀请下行
    public static short Server_TeamInvite = 0x0147;// 邀请上行
    public static short Client_TeamMatch = 0x0148;// 匹配下行
    public static short Server_TeamMatch = 0x0149;// 匹配上行

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ClientBaseTeamApply.class);
        list.add(ServerBaseTeamApply.class);
        list.add(ClientBaseTeamInfo.class);
        list.add(ServerBaseTeamOption.class);
        list.add(ClientBaseTeamInvite.class);
        list.add(ServerBaseTeamInvite.class);
        list.add(ClientBaseTeamMatch.class);
        list.add(ServerBaseTeamMatch.class);
        return list;
    }
}
