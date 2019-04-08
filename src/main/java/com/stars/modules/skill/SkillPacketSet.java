package com.stars.modules.skill;

import com.stars.modules.skill.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class SkillPacketSet extends PacketSet {
	
	public static short Client_Skill_List = 0x0044;//技能列表
	public static short Server_Skill_Up = 0x0045;//升级技能
	public static short Client_Skill_Flush = 0x0046;//技能更新
	public static short Server_Skill_List = 0x0047;//请求技能列表
	public static short Server_Skill_Position = 0x0048;//设置技能位置
	public static short Client_Skill_Position = 0x0049;//技能设置返回

	@Override
	public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
		List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ClientRoleSkills.class);
        al.add(ServerUpRoleSkill.class);
        al.add(ClientRoleSkillFlush.class);
        al.add(ServerRoleSkill.class);
        al.add(ServerSetSkillPosition.class);
        return al;
	}

}
