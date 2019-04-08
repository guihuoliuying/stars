package com.stars.modules.raffle;

import com.stars.modules.raffle.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author likang by 2017/4/22
 * 
 */

public class RafflePacketSet extends PacketSet {

	/*
	 * 上行协议 S_* , 下行协议 C_*
	 */

	// 拿取信息
	public static short S_RaffleGetInfo = 0x0254;
	public static short C_RaffleGetInfo = 0x0255;

	// 抽奖操作
	public static short S_RaffleDo = 0x0256;
	public static short C_RaffleDo = 0x0257;

	// 选择奖励
	public static short S_RaffleSelectReward = 0x0258;
	public static short C_RaffleSelectReward = 0x0259;

	@Override
	public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
		List<Class<? extends Packet>> list = new ArrayList<>();
		list.add(ServerRaffleGetInfo.class);
		list.add(ClientRaffleGetInfo.class);
		list.add(ServerRaffleDo.class);
		list.add(ClientRaffleDo.class);
		list.add(ServerRaffleSelectReward.class);
		list.add(ClientRaffleSelectReward.class);
		return list;
	}

}
