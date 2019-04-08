package com.stars.modules.loottreasure;

import com.stars.modules.loottreasure.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panzhenfeng on 2016/10/10.
 */
public class LootTreasurePacketSet  extends PacketSet {
    public static final short S_LOOTTREASURE_RANKLIST = 0x6190;
    public static final short S_LOOTTREASURE_INFO = 0x6191;
    public static final short C_LOOTTREASURE_INFO = 0x6192;
    public static final short ATTEND_LOOTTREASURE = 0x6193;
    public static final short ATTEND_LOOTTREASURE_BACK = 0x6194;
    public static final short S_ATTEND_LOOTTREASURE = 0x6195;
    public static final short C_LOOTTREASURE_RANKLIST = 0x6196;
    public static final short C_LOOTTREASURE_OPR = 0x6197;
    public static final short S_LOOTTREASURE_RANK_REQ = 0x6198;
    public static final short C_LOOTTREASURE_RANK_BACK = 0x6199;
    public static final short C_LOOTTREASURE_ADDREMOVE_PLAYER = 0x619A;
    public static final short S_LOOTTREASURE_SWITCH_ROOM = 0x619B;
    public static final short C_LOOTTREASURE_ENTER_BACK = 0x619C;


    public LootTreasurePacketSet() {

    }

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerLootTreasureRankList.class);
        al.add(ServerLootTreasureInfo.class);
        al.add(ClientLootTreasureInfo.class);
        al.add(AttendLootTreasure.class);
        al.add(AttendLootTreasureBack.class);
        al.add(ServerAttendLootTreasure.class);
        al.add(ClientLootTreasureRankList.class);
        al.add(ClientLootTreasureOpr.class);
        al.add(ServerLootTreasureRankReq.class);
        al.add(ClientLootTreasureRankBack.class);
        al.add(ClientLootTreasureAddRemovePlayer.class);
        al.add(ServerRequestSwitchRoom.class);
        al.add(ClientLootTreasureEnterBack.class);
        return al;
    }

}
