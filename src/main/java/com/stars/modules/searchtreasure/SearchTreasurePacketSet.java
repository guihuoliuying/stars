package com.stars.modules.searchtreasure;

import com.stars.modules.searchtreasure.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * 仙山探宝协议;
 * Created by panzhenfeng on 2016/8/24.
 */
public class SearchTreasurePacketSet  extends PacketSet {

    public static final short S_SEARCHTREASURE_INFO = 0x0084;
    public static final short C_SEARCHTREASURE_INFO = 0x0085;
    public static final short S_SEARCHTREASURE_VO = 0x0086;
    public static final short C_SEARCHTREASURE_VO = 0x0087;
    public static final short C_SEARCHTREASURE_PATH_POINT = 0x0088;
    public static final short S_SEARCHTREASURE_USETOOL = 0x0089;
    public static final short S_SEARCHTREASURE_PATH_POINT = 0x008A;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerSearchTreasureInfo.class);
        al.add(ClientSearchTreasureInfo.class);
        al.add(ServerSearchVo.class);
        al.add(ClientSearchVo.class);
        al.add(ClientSearchPathPoint.class);
        al.add(ServerUseSearchTreasureTool.class);
        al.add(ServerSearchPathPoint.class);
        return al;
    }
}
