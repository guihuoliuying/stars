package com.stars.modules.skill.packet;


import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.skill.SkillModule;
import com.stars.modules.skill.SkillPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ServerSetSkillPosition extends PlayerPacket {
	
	private byte type;
	
	private int skillId;
	
	private byte position;

	@Override
	public void execPacket(Player player) {
		SkillModule sm = (SkillModule)module(MConst.Skill);
		if (type  == 0) {
			sm.setSkillPosition(skillId, position);
			return;
		}
		sm.autoSetSkillPosition();
	}

	@Override
	public short getType() {
		return SkillPacketSet.Server_Skill_Position;
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		type = buff.readByte();
		if (type == 0) {//设置技能
			skillId = buff.readInt();
			position = buff.readByte();
		}
		//type==1 一键设置
    }
}
