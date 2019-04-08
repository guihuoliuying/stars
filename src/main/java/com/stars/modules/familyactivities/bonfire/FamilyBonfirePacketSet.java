package com.stars.modules.familyactivities.bonfire;

import com.stars.modules.familyactivities.bonfire.packet.ClientBonfire;
import com.stars.modules.familyactivities.bonfire.packet.ClientFamilyScene;
import com.stars.modules.familyactivities.bonfire.packet.ServerBonfire;
import com.stars.modules.familyactivities.bonfire.packet.ServerFamilyScene;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyaohui on 2016/10/8.
 */
public class FamilyBonfirePacketSet extends PacketSet {

    public static final short S_FAMILY_SCENE = 0x6070;
    public static final short C_FAMILY_SCENE = 0x6071;
    public static final short S_BONFIRE = 0x6072;
    public static final short C_BONFIRE = 0x6073;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ClientFamilyScene.class);
        list.add(ServerFamilyScene.class);
        list.add(ServerBonfire.class);
        list.add(ClientBonfire.class);
        return list;
    }
}
