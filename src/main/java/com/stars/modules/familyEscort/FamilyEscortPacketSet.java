package com.stars.modules.familyEscort;

import com.stars.modules.familyEscort.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class FamilyEscortPacketSet extends PacketSet {
	
//	check((short) 0x0242, (short) 0x024F, packetTypes, bitmap, new FamilyEscortPacketSet()); // 家族运镖
	
	public static short S_ESCORT_OP = 0x0242;//运镖相关的一些请求
	public static short C_ESCORT_CAR = 0x0243;//增加镖车
	public static short C_CAR_FLUSH = 0x0244;//镖车状态刷新
	public static short C_BARRIER_INFO = 0x0245;//障碍物
	public static short C_ESCORT_LIST = 0x0246;//家族运镖列表
	public static short C_ESCORT_MAINUI = 0x0247;//家族运镖入口UI
	public static short C_ESCORT_SCENE = 0x0248;//运镖场景信息
	public static short C_ESCORT_RESULT = 0x0249;//运镖结果
	public static short C_ESCORT_PLAYERSTATUS = 0x024a;//运镖场景玩家状态信息
	public static short C_ESCORT_CLEARUP = 0x024b;//运镖场景清场提示
	public static short C_ESCORT_TIMETIPS = 0x024c;//运镖时间提示
	

	@Override
	public List<Class<? extends Packet>> getPacketList() {
		List<Class<? extends Packet>> list = new ArrayList<>();
		list.add(ClientEscCarFlush.class);
		list.add(ClientEscortResult.class);
		list.add(ClientFamEscBarrier.class);
		list.add(ClientFamilyEscortCars.class);
		list.add(ClientFamilyEscortList.class);
		list.add(ClientFamilyEscortMainUI.class);
		list.add(ClientFamilyEscortSceneInfo.class);
		list.add(ServerFamilyEscortOp.class);
		list.add(ClientEscortPlayerStatus.class);
		list.add(ClientFamilyEscortClearTips.class);
		list.add(ClientEscTimeTips.class);
		return list;
	}

}
