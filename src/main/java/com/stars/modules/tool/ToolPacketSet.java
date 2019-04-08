package com.stars.modules.tool;

import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.tool.packet.ClientTool;
import com.stars.modules.tool.packet.ServerExchangeGift;
import com.stars.modules.tool.packet.ServerTool;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangjiahua on 2016/2/23.
 */
public class ToolPacketSet extends PacketSet {

    public static short C_TOOL = 0x0031;//道具系统下发
    public static short S_TOOL = 0x0032; // 道具系统上发
    public static short C_TOOL_AWARD = 0x0033;//奖励提示
	public static short S_EXCHANGE_GIFT = 0x0034;// 兑换礼包


    public ToolPacketSet(){
//        reg(ClientTool.class);
//        reg(ServerTool.class);
    }
    
    @Override
	public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
		// TODO Auto-generated method stub
		List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
		al.add(ClientTool.class);
		al.add(ServerTool.class);
		al.add(ClientAward.class);
		al.add(ServerExchangeGift.class);
		return al;
	}
}
