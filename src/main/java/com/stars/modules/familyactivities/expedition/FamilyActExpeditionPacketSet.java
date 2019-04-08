package com.stars.modules.familyactivities.expedition;

import com.stars.modules.familyactivities.expedition.packet.ClientFamilyActExpedition;
import com.stars.modules.familyactivities.expedition.packet.ClientFamilyActExpeditionSceneFinished;
import com.stars.modules.familyactivities.expedition.packet.ServerFamilyActExpedition;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2016/10/12.
 */
public class FamilyActExpeditionPacketSet extends PacketSet {

    public static final short S_EXPEDITION = 0x6068; //
    public static final short C_EXPEDITION = 0x6069; //
    public static final short C_SCENE_FINISHED = 0x606A; //

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        /* 家族远征(0x6068 - 0x606F) */
        list.add(ServerFamilyActExpedition.class);
        list.add(ClientFamilyActExpedition.class);
        list.add(ClientFamilyActExpeditionSceneFinished.class);
        return list;
    }
}
