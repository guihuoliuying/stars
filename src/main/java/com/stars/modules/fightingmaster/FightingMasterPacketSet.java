package com.stars.modules.fightingmaster;

import com.stars.modules.fightingmaster.packet.ServerEnterFightingMaster;
import com.stars.modules.fightingmaster.packet.ServerFightReady;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyaohui on 2016/11/8.
 */
public class FightingMasterPacketSet extends PacketSet {

    public final static short S_ENTER_FIGHTINGMASTER = 0x61a0;  // 进入斗神殿
    public final static short S_FIGHTINGMASTER = 0x61a1;                 // 斗神殿业务请求
    public final static short S_FIGHT_READY = 0x61a2;           // 战斗准备完成
    public final static short C_FIGHTINGMASTER = 0x61a3;             // 斗神殿业务响应

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerEnterFightingMaster.class);
        list.add(ServerFightReady.class);
        return list;
    }
}
