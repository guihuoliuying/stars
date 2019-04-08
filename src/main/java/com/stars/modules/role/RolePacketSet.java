package com.stars.modules.role;

import com.stars.modules.role.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class RolePacketSet extends PacketSet {

	public static short C_ROLE = 0x0010;
	public static short S_FIGHTSCORE_REWARD = 0x0013; // 领取战力奖励请求
	public static short C_FIGHTSCORE_REWARD = 0x0014; // 领取战力奖励结果响应
	public static short S_ROLE_RESOURCE = 0x0015; // 角色资源操作请求(购买体力、金币)
	public static short C_ROLE_RESOURCE = 0x0016; // 角色资源操作响应(购买体力、金币)
	public static short C_HEARTBEAT_CHECK = 0x0017;//心跳包检测
	public static short S_HEARTBEAT_CHECK = 0x0018;//心跳包检测返回
	public static short S_ROLEATTR_CHECK = 0x0019;//角色属性检测返回
	
	
	public RolePacketSet() {
//		reg(ClientRole.class);
	}
	
	@Override
	public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
		// TODO Auto-generated method stub
		List<Class<? extends com.stars.network.server.packet.Packet>> list = new ArrayList<Class<? extends Packet>>();
		list.add(ClientRole.class);
		list.add(ServerFightScoreReward.class);
		list.add(ClientFighScoreReward.class);
		list.add(ServerRoleResource.class);
		list.add(ClientRoleResource.class);
		list.add(ClientHeartBeatCheck.class);
		list.add(ServerHeartBeatCheck.class);
		list.add(ServerRoleAttrCheck.class);
		return list;
	}
	
}
