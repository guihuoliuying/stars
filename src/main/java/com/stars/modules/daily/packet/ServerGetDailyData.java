//package com.stars.modules.daily.packet;
//
//import com.stars.core.player.Player;
//import com.stars.core.player.PlayerPacket;
//import com.stars.modules.MConst;
//import com.stars.modules.daily.DailyModule;
//import com.stars.modules.daily.DailyPacketSet;
//
//public class ServerGetDailyData extends PlayerPacket {
//
//	@Override
//	public void execPacket(Player player) {
//		DailyModule dm = module(MConst.Daily);
//		dm.getDailyData();
//	}
//
//	@Override
//	public short getType() {
//		return DailyPacketSet.Server_GetDailyData;
//	}
//
//}
