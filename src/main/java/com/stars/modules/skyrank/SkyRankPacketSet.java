package com.stars.modules.skyrank;

import com.stars.modules.skyrank.packet.ClientSkyRankAwardData;
import com.stars.modules.skyrank.packet.ClientSkyRankTimeDesc;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 天梯排行
 * 
 * @author xieyuejun
 *
 */
public class SkyRankPacketSet extends PacketSet {
	
	
	public static final short ServerSkyRankReq = 0x026F;
	public static final short ClientSkyRankGradData = 0x0270;//天梯表数据
	public static final short ClientSkyRankMyData = 0x0271;//我的积分数据
	public static final short ClientSkyRankRankData = 0x0272;//天梯排行数据
	public static final short ClientSkyRankGradAwardData = 0x0273;//升级获得的奖励数据
	public static final short ClientSkyRankAwardData = 0x0274;//所有的奖励数据集合
	public static final short ClientSkyRankTimeDesc = 0x0275;//天梯当前时间的描述
	

	public SkyRankPacketSet() {

	}

	@Override
	public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
		List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
		al.add(com.stars.modules.skyrank.packet.ServerSkyRankReq.class);
		al.add(com.stars.modules.skyrank.packet.ClientSkyRankGradData.class);
		al.add(com.stars.modules.skyrank.packet.ClientSkyRankMyData.class);
		al.add(com.stars.modules.skyrank.packet.ClientSkyRankRankData.class);
		al.add(com.stars.modules.skyrank.packet.ClientSkyRankGradAwardData.class);
		al.add(ClientSkyRankAwardData.class);
		al.add(ClientSkyRankTimeDesc.class);
		return al;
	}
}