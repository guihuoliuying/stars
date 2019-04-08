package com.stars.modules.skill.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.SkillPacketSet;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.modules.skill.prodata.SkillvupVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author dengzhou
 *         <p/>
 *         技能列表数据
 */
public class ClientRoleSkills extends PlayerPacket {

    private Map<SkillVo, Integer> skillMap;
    private int roleAttack = 0;

    private Map<Integer, Integer> skillCDMap;

    public ClientRoleSkills() {

    }

    @Override
    public void execPacket(Player player) {
        // TODO Auto-generated method stub

    }

    @Override
    public short getType() {
        return SkillPacketSet.Client_Skill_List;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        byte size = (byte) skillMap.size();
        buff.writeByte(size);
        if (size <= 0) {
            return;
        }

        Set<Entry<SkillVo, Integer>> set = skillMap.entrySet();
        for (Entry<SkillVo, Integer> entry : set) {
            SkillVo skill = entry.getKey();
            int level = entry.getValue();
            SkillvupVo upVo = SkillManager.getSkillvupVo(skill.getSkillid(), level);
            SkillvupVo nextUpVo = SkillManager.getSkillvupVo(skill.getSkillid(), level + 1);
            if (nextUpVo == null) nextUpVo = upVo;
            buff.writeInt(skill.getSkillid());
            buff.writeString(skill.getName());
            buff.writeString(upVo.getDescrib());
            buff.writeString(skill.getIcon());
            buff.writeByte(upVo.getSkillType());
            buff.writeInt(level);
            buff.writeByte(skill.getIchange());
            buff.writeInt(upVo.getCoefficient());
            buff.writeString(upVo.getSkilldesc());
            int delCd = skillCDMap.containsKey(upVo.getSkillId()) ? skillCDMap.get(upVo.getSkillId()) : 0;
            buff.writeInt(upVo.getCooldown() >= delCd ? upVo.getCooldown() - delCd : 0);
            SkillvupVo.writeDamageDescDataToBuff(buff, roleAttack, upVo.getDamagedesc(), upVo.getCoefficient(), upVo.getDamage());
            SkillvupVo.writeDamageDescDataToBuff(buff, roleAttack, nextUpVo.getDamagedesc(), nextUpVo.getCoefficient(), nextUpVo.getDamage());

            int maxLevel = SkillManager.getMaxSkillLevel(skill.getSkillid());
            buff.writeInt(maxLevel);//最大等级
            if (level < maxLevel) {
                upVo = SkillManager.getSkillvupVo(skill.getSkillid(), level + 1);
                buff.writeInt(upVo.getReqlv());
                buff.writeString(upVo.getReqItemStr());
                buff.writeInt(upVo.getReqdungeon());
                buff.writeString(upVo.getReqskilllevel());
            }
        }
    }

    public Map<SkillVo, Integer> getSkillMap() {
        return skillMap;
    }

    public void setSkillMap(Map<SkillVo, Integer> skillMap) {
        this.skillMap = skillMap;
    }

    public void setRoleAttack(int roleAttack) {
        this.roleAttack = roleAttack;
    }

    public void setSkillCDMap(Map<Integer, Integer> skillCDMap) {
        this.skillCDMap = skillCDMap;
    }
}
