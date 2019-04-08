//package com.stars.modules.daily.packet;
//
//import java.util.HashSet;
//
//import com.stars.modules.daily.DailyPacketSet;
//import com.stars.network.server.buffer.NewByteBuffer;
//import com.stars.network.server.packet.Packet;
//
//public class ClientDailyAward extends Packet {
//
//	private byte flag;//标记
//
//	private int awardKey;
//
//	HashSet<Integer> set;
//
//	public ClientDailyAward(){}
//
//	public ClientDailyAward(byte flag){
//		this.flag = flag;
//	}
//
//	public ClientDailyAward(byte flag,int awardKey){
//		this.flag = flag;
//		this.awardKey = awardKey;
//	}
//
//	public ClientDailyAward(byte flag,HashSet<Integer> set){
//		this.flag = flag;
//		this.set = set;
//	}
//
//	@Override
//	public short getType() {
//		// TODO Auto-generated method stub
//		return DailyPacketSet.Client_DailyAward;
//	}
//
//	@Override
//	public void writeToBuffer(NewByteBuffer buff) {
//		buff.writeByte(flag);
//		if (flag == 0) {
//			buff.writeInt(awardKey);
//		}else {
//			byte size = (byte)set.size();
//			buff.writeByte(size);
//			for (Integer integer : set) {
//				buff.writeInt(integer);
//			}
//		}
//	}
//
//	@Override
//	public void readFromBuffer(NewByteBuffer buff) {
//
//	}
//
//	@Override
//	public void execPacket() {
//		// TODO Auto-generated method stub
//
//	}
//
//}
