package com.stars.modules.camp;

import com.google.common.collect.Lists;
import com.stars.modules.camp.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.List;

/**
 * Created by huwenjun on 2017/6/26.
 */
public class CampPackset extends PacketSet {

    public static final short S_CAMP = 0x02B0;
    public static final short C_CAMP = 0x02B1;
    public static final short S_CITY = 0x02B2;
    public static final short C_CITY = 0x02B3;
    public static final short S_OFFICER = 0x02B4;
    public static final short C_OFFICER = 0x02B5;
    public static final short S_ACTIVITY = 0x02B6;
    public static final short C_ACTIVITY = 0x02B7;
    public static final short S_MISSION = 0x02B8;
    public static final short C_MISSION = 0x02B9;
    public static final short Client_CampCiytFight = 0x02BA;
    public static final short Server_CampCityFight = 0x02BB;
    public static final short S_CAMP_FIGHT = 0x02BC;
    public static final short C_CAMP_FIGHT = 0x02BD;
    public static final short C_CAMP_FIGHT_CLEAR = 0x02BE;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = Lists.newArrayList();
        list.add(ServerCampPacket.class);
        list.add(ClientCampPacket.class);
        list.add(ServerCampCityPacket.class);
        list.add(ClientCampCityPacket.class);
        list.add(ServerOfficerPacket.class);
        list.add(ClientOfficerPacket.class);
        list.add(ServerCampActivityPacket.class);
        list.add(ClientCampActivityPacket.class);
        list.add(ServerCampMissionPacket.class);
        list.add(ClientCampMissionPacket.class);
        list.add(ClientCampCiytFight.class);
        list.add(ServerCampCityFight.class);
        list.add(ServerCampFightPacket.class);
        list.add(ClientCampFightPacket.class);
        list.add(ClientCampFightClearPacket.class);
        return list;
    }
}
