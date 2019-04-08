package com.stars.modules.callboss;

import com.stars.modules.callboss.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/9/2.
 */
public class CallBossPacketSet extends PacketSet {
    public static short S_CALLBOSSLIST = 0x6058;// 请求召唤boss数据
    public static short C_CALLBOSSVO = 0x6059;// 下发召唤boss产品数据
    public static short C_CALLBOSSPO = 0x605A;// 下发召唤boss状态数据
    public static short S_EXECUTE_CALLBOSS = 0x605B;// 请求执行召唤boss
    public static short S_CALLBOSS_RANKLIST = 0x605C;// 请求排行榜列表
    public static short C_CALLBOSS_RANKLIST = 0x605D;// 下发排行榜列表
    public static short S_CALLBOSS_RANKINFO = 0x605E;// 请求排行榜信息
    public static short C_CALLBOSS_RANKINFO = 0x605F;// 下发排行榜信息

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ServerCallBossList.class);
        al.add(ClientCallBossVo.class);
        al.add(ClientCallBossPo.class);
        al.add(ServerExecuteCallBoss.class);
        al.add(ServerCallBossRankList.class);
        al.add(ClientCallBossRankList.class);
        al.add(ServerCallBossRank.class);
        al.add(ClientCallBossRank.class);
        return al;
    }
}
