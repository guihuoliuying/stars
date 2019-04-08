package com.stars.modules.opactchargescore;

import com.stars.modules.opactchargescore.packet.ClientOpActChargeScore;
import com.stars.modules.opactchargescore.packet.ServerOpActChargeScore;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by likang on 2017-04-18 10:42
 */
public class OpActChargeScorePacketSet extends PacketSet {
	public static short S_OpActChargeScore = 0x622D;
	public static short C_OpActChargeScore = 0x622E;

	@Override
	public List<Class<? extends Packet>> getPacketList() {
		List<Class<? extends Packet>> al = new ArrayList<>();
		al.add(ClientOpActChargeScore.class);
		al.add(ServerOpActChargeScore.class);
		return al;
	}
}
