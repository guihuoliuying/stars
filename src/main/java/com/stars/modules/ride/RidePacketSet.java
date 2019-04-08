package com.stars.modules.ride;

import com.stars.modules.ride.packet.ClientRide;
import com.stars.modules.ride.packet.ClientRideAwakeLevelVo;
import com.stars.modules.ride.packet.ClientRideLevelVo;
import com.stars.modules.ride.packet.ServerRide;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2016/9/18.
 */
public class RidePacketSet extends PacketSet {

    public static final short S_RIDE = 0x0120; // 请求
    public static final short C_RIDE = 0x0121; // 响应
    public static final short C_LEVEL = 0x0122; // 坐骑产品数据
    public static final short C_AWAKE_LEVEL = 0x0123; // 坐骑觉醒等级产品数据

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerRide.class);
        list.add(ClientRide.class);
        list.add(ClientRideLevelVo.class);
        list.add(ClientRideAwakeLevelVo.class);
        return list;
    }

}
