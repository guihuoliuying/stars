package com.stars.modules.skill.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.skill.SkillModule;
import com.stars.modules.skill.SkillPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * @author dengzhou
 *         技能升级
 */
public class ServerUpRoleSkill extends PlayerPacket {

    private int skillId;

    public ServerUpRoleSkill() {}

    @Override
    public void execPacket(Player player) {
        // TODO Auto-generated method stub
        SkillModule sm = (SkillModule) module(MConst.Skill);
        if (skillId == 0) {
            sm.upAllRoleSkillLv();
        } else {
            sm.upRoleSkill(skillId);
        }
    }

    @Override
    public short getType() {
        // TODO Auto-generated method stub
        return SkillPacketSet.Server_Skill_Up;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        skillId = buff.readInt();
    }

}
