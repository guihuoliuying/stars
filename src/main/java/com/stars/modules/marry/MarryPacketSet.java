package com.stars.modules.marry;

import com.stars.modules.marry.packet.ClientMarry;
import com.stars.modules.marry.packet.ClientMarryBattle;
import com.stars.modules.marry.packet.ClientMarryBattleInfo;
import com.stars.modules.marry.packet.ServerMarry;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyaohui on 2016/12/1.
 */
public class MarryPacketSet extends PacketSet {
    public final static short C_MARRY = 0x0184; // 下行
    public final static short S_MARRY = 0x0185; // 上行
    public final static short C_MARRY_BATTLE = 0x0186; //下行
    public final static short C_MARRY_BATTLE_INFO = 0x0187;//结婚对方数据

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ClientMarry.class);
        list.add(ServerMarry.class);
        list.add(ClientMarryBattle.class);
        list.add(ClientMarryBattleInfo.class);
        return list;
    }
}
