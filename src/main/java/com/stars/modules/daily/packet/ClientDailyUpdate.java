//package com.stars.modules.daily.packet;
//
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//
//import org.omg.CORBA.PUBLIC_MEMBER;
//
//import com.stars.modules.daily.DailyPacketSet;
//import com.stars.network.server.buffer.NewByteBuffer;
//import com.stars.network.server.packet.Packet;
//
//public class ClientDailyUpdate extends Packet {
//
//	private Map<Short, Integer>map;
//
//	private int dailyCounter;
//
//
//	public ClientDailyUpdate(){
//		map = new HashMap<Short, Integer>();
//	}
//
//	@Override
//	public short getType() {
//		return DailyPacketSet.Client_DailyUpdate;
//	}
//
//	@Override
//	public void writeToBuffer(NewByteBuffer buff) {
//		buff.writeInt(dailyCounter);
//		short size = (short)map.size();
//		buff.writeShort(size);
//		Iterator<Short>it = map.keySet().iterator();
//		while (it.hasNext()) {
//			Short short1 = (Short) it.next();
//			buff.writeShort(short1);
//			buff.writeInt(map.get(short1));
//		}
//	}
//
//	@Override
//	public void readFromBuffer(NewByteBuffer buff) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void execPacket() {
//		// TODO Auto-generated method stub
//
//	}
//
//	public Map<Short, Integer> getMap() {
//		return map;
//	}
//
//	public void setMap(Map<Short, Integer> map) {
//		this.map = map;
//	}
//
//	public int getDailyCounter() {
//		return dailyCounter;
//	}
//
//	public void setDailyCounter(int dailyCounter) {
//		this.dailyCounter = dailyCounter;
//	}
//
//	public void putUpdateData(short dailyId,int count){
//		this.map.put(dailyId, count);
//	}
//
//}
