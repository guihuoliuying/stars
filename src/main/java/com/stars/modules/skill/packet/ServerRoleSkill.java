package com.stars.modules.skill.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.skill.SkillModule;
import com.stars.modules.skill.SkillPacketSet;

public class ServerRoleSkill extends PlayerPacket {

	@Override
	public void execPacket(Player player) {
		SkillModule sm = (SkillModule)module(MConst.Skill);
		sm.sendSkillList2Client();
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return SkillPacketSet.Server_Skill_List;
	}

}
