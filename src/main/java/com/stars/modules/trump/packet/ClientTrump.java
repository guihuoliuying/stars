package com.stars.modules.trump.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.modules.skill.prodata.SkillvupVo;
import com.stars.modules.trump.TrumpPacketSet;
import com.stars.modules.trump.prodata.TrumpLevelVo;
import com.stars.modules.trump.userdata.RoleTrumpRow;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Collection;
import java.util.Map;

/**
 * Created by zhouyaohui on 2016/9/20.
 */
public class ClientTrump extends PlayerPacket {
    /**
     * 常量
     */
    public final static byte IDLE = 0;
    public final static byte USER_DATA = 1;         // 发送用户数据
    public final static byte TRUMP_LEVEL_DATA = 2;  // 发送法宝等级产品数据
    public final static byte UPGRADE_SUCCESS = 3;   // 升级成功
    public final static byte PUT_ON = 4;            // 佩戴
    public final static byte SINGLE = 5;            // 发送某一级的产品数据
    public final static byte RESOLVE = 6;           // 分解

    private byte opType;

    /**
     * 用户数据
     */
    private Collection<RoleTrumpRow> roleTrumpList;

    /**
     * 法宝等级产品数据
     */
    private Collection<TrumpLevelVo> levelVoList;
    private Map<String, SkillvupVo> skillvupVoMap;
    private TrumpLevelVo levelVo;
    private Map<Integer, SkillVo> skillVoMap;
    private boolean has;
    private boolean isMax;
    private Map<Integer, Short> maxLevel;

    private int trumpId;
    private short level;
    private byte position;

    private Map<Integer, Integer> skillCDMap;

    private String resolveStr;

    public void setMax(boolean max) {
        isMax = max;
    }

    public void setMaxLevel(Map<Integer, Short> maxLevel) {
        this.maxLevel = maxLevel;
    }

    public void setHas(boolean has) {
        this.has = has;
    }

    public void setResolveStr(String resolveStr) {
        this.resolveStr = resolveStr;
    }

    public void setSkillVoMap(Map<Integer, SkillVo> skillVoMap) {
        this.skillVoMap = skillVoMap;
    }

    public void setLevelVo(TrumpLevelVo levelVo) {
        this.levelVo = levelVo;
    }

    public void setSkillvupVoMap(Map<String, SkillvupVo> skillMap) {
        this.skillvupVoMap = skillMap;
    }

    public void setPosition(byte position) {
        this.position = position;
    }

    public void setTrumpId(int trumpId) {
        this.trumpId = trumpId;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public void setLevelVoList(Collection<TrumpLevelVo> levelVoList) {
        this.levelVoList = levelVoList;
    }

    public void setRoleTrumpList(Collection<RoleTrumpRow> roleTrumpList) {
        this.roleTrumpList = roleTrumpList;
    }

    public void setOpType(byte opType) {
        this.opType = opType;
    }

    public void setSkillCDMap(Map<Integer, Integer> skillCDMap) {
        this.skillCDMap = skillCDMap;
    }

    @Override
    public short getType() {
        return TrumpPacketSet.CLIENT_TRUMP;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(opType);
        if (opType == USER_DATA) {
            buff.writeInt(roleTrumpList.size());
            for (RoleTrumpRow row : roleTrumpList) {
                buff.writeInt(row.getTrumpId());
                buff.writeShort(row.getLevel());
                buff.writeByte(row.getPosition());
//                buff.writeByte(row.getClick());
            }
        }

        if (opType == TRUMP_LEVEL_DATA) {
            buff.writeInt(levelVoList.size());
            for (TrumpLevelVo levelVo : levelVoList) {
                writeLevelVotoBuff(buff, levelVo);
                buff.writeByte(maxLevel.get(levelVo.getTrumpId()) > levelVo.getLevel() ? (byte) 0 : (byte) 1);
            }
            buff.writeInt(skillvupVoMap.size());
            for (SkillvupVo skill : skillvupVoMap.values()) {
                buff.writeInt(skill.getSkillId());
                buff.writeInt(skill.getLevel());
                buff.writeInt(skill.getDamage());
                int delCd = skillCDMap.containsKey(skill.getSkillId()) ? skillCDMap.get(skill.getSkillId()) : 0;
                buff.writeInt(skill.getCooldown() >= delCd ? skill.getCooldown() - delCd : 0);
            }
            buff.writeInt(skillVoMap.size());
            for (SkillVo skillVo : skillVoMap.values()) {
                buff.writeInt(skillVo.getSkillid());
                buff.writeString(skillVo.getName());
                buff.writeString(skillVo.getIcon());
                buff.writeString(skillVo.getDescrib());
            }
        }

        if (opType == UPGRADE_SUCCESS) {
            buff.writeInt(trumpId);
            buff.writeShort(level);
        }

        if (opType == PUT_ON) {
            buff.writeInt(trumpId);
            buff.writeByte(position);
        }

        if (opType == SINGLE) {
            buff.writeByte(has ? (byte) 1 : (byte) 0);
            if (has) {
                writeLevelVotoBuff(buff, levelVo);
                buff.writeByte(isMax ? (byte) 1 : (byte) 0);
                buff.writeInt(skillvupVoMap.size());
                for (SkillvupVo skill : skillvupVoMap.values()) {
                    buff.writeInt(skill.getSkillId());
                    buff.writeInt(skill.getLevel());
                    buff.writeInt(skill.getDamage());
                    int delCd = skillCDMap.containsKey(skill.getSkillId()) ? skillCDMap.get(skill.getSkillId()) : 0;
                    buff.writeInt(skill.getCooldown() >= delCd ? skill.getCooldown() - delCd : 0);
                }
            }
        }

        if (opType == RESOLVE) {
            buff.writeString(resolveStr);
        }
    }

    private void writeLevelVotoBuff(NewByteBuffer buff, TrumpLevelVo levelVo) {
        buff.writeInt(levelVo.getTrumpId());
        buff.writeShort(levelVo.getLevel());
        buff.writeByte(levelVo.getStage());
        buff.writeByte(levelVo.getDisplayLevel());
        buff.writeString(levelVo.getDisplay());
        buff.writeString(levelVo.getSmallScale());
        buff.writeString(levelVo.getMediumScale());
        buff.writeString(levelVo.getLargeScale());
        buff.writeString(levelVo.getSkill());
        buff.writeString(levelVo.getMaterial());
        buff.writeInt(levelVo.getTriggerRate());
        buff.writeInt(levelVo.getFightScore());
    }
}
