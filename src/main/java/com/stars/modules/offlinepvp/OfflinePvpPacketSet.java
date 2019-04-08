package com.stars.modules.offlinepvp;

import com.stars.modules.offlinepvp.packet.ClientOfflinePvpData;
import com.stars.modules.offlinepvp.packet.ServerOfflinePvpData;
import com.stars.modules.offlinepvp.packet.ServerOfflinePvpOption;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/9/30.
 */
public class OfflinePvpPacketSet extends PacketSet {
    public static short S_OFFLINEPVP_DATA = 0x6170;// 打开离线pvp界面请求
    public static short S_OFFLINEPVP_OPTION = 0x6171;// 离线pvp操作请求
    public static short C_OFFLINEPVP_DATA = 0x6172;// 下发数据

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ServerOfflinePvpData.class);
        al.add(ServerOfflinePvpOption.class);
        al.add(ClientOfflinePvpData.class);
        return al;
    }
}
