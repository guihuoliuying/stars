package com.stars.modules.family.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.modules.family.prodata.FamilySkillVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashMap;
import java.util.Map;

import static com.stars.modules.family.FamilyManager.skillVoMap;

/**
 * Created by zhaowenshuo on 2016/9/8.
 */
public class ClientFamilySkill extends PlayerPacket {

    public static final byte SUBTYPE_VIEW = 0x00;
    public static final byte SUBTYPE_UPGRADE = 0x10;
    public static final byte SUBTYPE_UPGRADE_AMAP = 0x11;

    private byte subtype;

    private Map<String, Integer> totalSkillLevelMap;
    private Map<String, Integer> partSkillLevelMap;

    public ClientFamilySkill() {
    }

    public ClientFamilySkill(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyPacketSet.C_SKILL;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case SUBTYPE_VIEW:
                writeViewList(buff);
                break;
            case SUBTYPE_UPGRADE:
                writeUpgrade(buff);
                break;
            case SUBTYPE_UPGRADE_AMAP:
                writeUpgradeAmap(buff);
                break;
        }
    }

    private void writeSkill(com.stars.network.server.buffer.NewByteBuffer buff, String skillName, int skillLevel) {
        FamilySkillVo currentSkillVo = skillVoMap.get(skillName).get(skillLevel);
        FamilySkillVo nextSkillVo = skillVoMap.get(skillName).get(skillLevel+1);
        buff.writeString(skillName); // 心法名字
        buff.writeInt(skillLevel); // 当前心法等级
        buff.writeInt(currentSkillVo == null ? 0 : currentSkillVo.getValue()); // 当前心法属性值
        buff.writeInt(nextSkillVo == null ? -1 : nextSkillVo.getLevel()); // 下一等级心法等级
        buff.writeInt(nextSkillVo == null ? -1 : nextSkillVo.getValue()); // 下一等级心法属性值
        buff.writeInt(nextSkillVo == null ? -1 : nextSkillVo.getReqContribution()); // 下一等级心法需要的贡献值
    }

    private void writeViewList(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte((byte) skillVoMap.size());
        for (String skillName : skillVoMap.keySet()) {
            Integer skillLevel = totalSkillLevelMap.get(skillName);
            writeSkill(buff, skillName, skillLevel == null ? 0 : skillLevel);
        }
    }

    private void writeUpgrade(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte((byte) partSkillLevelMap.size());
        for (String skillName : partSkillLevelMap.keySet()) {
            writeSkill(buff, skillName, partSkillLevelMap.get(skillName));
        }
    }

    private void writeUpgradeAmap(NewByteBuffer buff) {
        buff.writeByte((byte) partSkillLevelMap.size());
        for (String skillName : partSkillLevelMap.keySet()) {
            writeSkill(buff, skillName, partSkillLevelMap.get(skillName));
        }
    }

    /* Getter/Setter */
    public Map<String, Integer> getTotalSkillLevelMap() {
        return totalSkillLevelMap;
    }

    public void setTotalSkillLevelMap(Map<String, Integer> totalSkillLevelMap) {
        this.totalSkillLevelMap = totalSkillLevelMap;
    }

    public Map<String, Integer> getPartSkillLevelMap() {
        return partSkillLevelMap;
    }

    public void setPartSkillLevelMap(Map<String, Integer> partSkillLevelMap) {
        this.partSkillLevelMap = partSkillLevelMap;
    }

    public void setSingleSkillLevelMap(String attribute, int currentLevel) {
        this.partSkillLevelMap = new HashMap<>();
        this.partSkillLevelMap.put(attribute, currentLevel);
    }
}
