package com.stars.modules.opactsecondskill;


import com.stars.modules.opactsecondskill.packet.ClientOpActSecondKill;
import com.stars.modules.opactsecondskill.packet.ServerOpActSecondKill;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanghaizhen on 2017/7/27.
 */
public class OpActSecondKillPacketSet extends PacketSet {

    public static short S_OpActSecondSkill = 0x6248;
    public static short C_OpActSecondSkill = 0x6249;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ClientOpActSecondKill.class);
        al.add(ServerOpActSecondKill.class);
        return al;
    }
}
