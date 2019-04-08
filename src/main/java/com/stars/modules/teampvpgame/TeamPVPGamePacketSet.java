package com.stars.modules.teampvpgame;

import com.stars.modules.teampvpgame.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2016/12/5.
 */
public class TeamPVPGamePacketSet extends PacketSet {
    public static short S_TPG_DATA = 0x61E0;// 数据请求
    public static short C_TPG_DATA = 0x61E1;// 数据下发
    public static short S_TPG_SIGNUP = 0x61E2;// 报名请求
    public static short C_TPG_SIGNUP = 0x61E3;// 报名下发
    public static short S_TPG_SCORERANK = 0x61E4;// 积分排行榜请求
    public static short C_TPG_SCORERANK = 0x61E5;// 积分排行榜下发
    public static short S_TPG_ENTERFIGHT = 0x61E6;// 进入战斗
    public static short C_TPG_COLLECT_DAMAGE = 0x61E7;// 队员伤害同步

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerTPGData.class);
        list.add(ClientTPGData.class);
        list.add(ServerTPGSignUp.class);
        list.add(ClientTPGSignUp.class);
        list.add(ServerTPGEnterFight.class);
        list.add(ServerTPGScoreRank.class);
        list.add(ClientTPGScoreRank.class);
        list.add(ClientTPGFightDamageCollect.class);
        return list;
    }
}
