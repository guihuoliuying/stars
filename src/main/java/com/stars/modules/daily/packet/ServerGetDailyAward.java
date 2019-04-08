//package com.stars.modules.daily.packet;
//
//import com.stars.core.player.Player;
//import com.stars.core.player.PlayerPacket;
//import com.stars.modules.MConst;
//import com.stars.modules.daily.DailyModule;
//import com.stars.modules.daily.DailyPacketSet;
//import com.stars.network.server.buffer.NewByteBuffer;
//
//public class ServerGetDailyAward extends PlayerPacket {
//
//	private int award;
//
//	@Override
//	public void execPacket(Player player) {
//
//		DailyModule dm = module(MConst.Daily);
//		//dm.getDailyAward(award);
//	}
//
//	@Override
//	public short getType() {
//		return DailyPacketSet.Server_GetDailyAward;
//	}
//
//
//	 @Override
//	 public void readFromBuffer(NewByteBuffer buff) {
//		 this.award = buff.readInt();
//	 }
//}
