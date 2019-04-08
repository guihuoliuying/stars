package com.stars.modules.gm;

import com.stars.modules.gm.packet.ServerGm;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2016/1/15.
 */
public class GmPacketSet extends PacketSet {

    public static short S_GM = 0x7D00;
    public static short B_RELOAD_DATA = 0x7D01;
    public static short G2C_RELOAD_DATA = 0x7D02;
	public static short M2M_WRONG_RELOAD = 0x7D03;
	public static short C2M_WRONG_RELOAD = 0x7D03;

    public GmPacketSet() {
//        reg(ServerGm.class);
//        reg(DataReloadBroadcast.class);
//        reg(DataReloadM2cPacket.class);
    }
    @Override
	public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
		// TODO Auto-generated method stub
		List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
		al.add(ServerGm.class); // 屏蔽游戏内GM
		return al;
	}
}
