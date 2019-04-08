package com.stars.modules.vip;

import com.stars.modules.vip.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/12/3.
 */
public class VipPacketSet extends PacketSet {
    public static short S_VIPDATA = 0x0188;// 打开界面请求数据
    public static short C_VIPDATA = 0x0189;// 数据下发
    public static short S_VIPOPTION = 0x018A;// vip操作
    public static short C_CHARGEDATA = 0x018B;// 充值数据
    public static short C_CHARGESWITCH = 0x018C;// 下发充值开关状态

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerVipData.class);
        list.add(ClientVipData.class);
        list.add(ServerVipOption.class);
        list.add(ClientChargeData.class);
        list.add(ClientChargeSwitchPacket.class);
        return list;
    }
}
