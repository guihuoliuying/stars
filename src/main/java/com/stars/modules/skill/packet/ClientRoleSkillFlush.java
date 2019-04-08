package com.stars.modules.skill.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.skill.SkillPacketSet;
import com.stars.modules.skill.prodata.SkillvupVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

public class ClientRoleSkillFlush extends PlayerPacket {
    private Map<Integer, SkillvupVo> voMap;
    private Map<Integer, String> reqItemStr;
    private Map<Integer, Integer> calcDamege;
    private Map<Integer, Integer> nextCalcDamege;
    private Map<Integer, String> nextLvReqSkillLv;
    private Map<Integer, Integer> nextReqLv;
    private Map<Integer, SkillvupVo> nextVoMap;
    private int roleAttack = 0;

    private Map<Integer, Integer> skillCDMap;

    public ClientRoleSkillFlush() {
    }

    @Override
    public void execPacket(Player player) {
        // TODO Auto-generated method stub

    }

    @Override
    public short getType() {
        return SkillPacketSet.Client_Skill_Flush;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte((byte) getVoMap().size());
        for (SkillvupVo vo : voMap.values()) {
            buff.writeInt(vo.getSkillId());
            buff.writeInt(vo.getLevel());
            buff.writeString(getReqItemStr().get(vo.getSkillId()));
            buff.writeInt(vo.getCoefficient());
            buff.writeString(vo.getSkilldesc());
            int delCd = skillCDMap.containsKey(vo.getSkillId()) ? skillCDMap.get(vo.getSkillId()) : 0;
            buff.writeInt(vo.getCooldown() >= delCd ? vo.getCooldown() - delCd : 0);
            buff.writeString(getNextLvReqSkillLv().get(vo.getSkillId()));
            buff.writeInt(getNextReqLv().get(vo.getSkillId()));
            buff.writeString(vo.getDescrib());

            SkillvupVo.writeDamageDescDataToBuff(buff, roleAttack, vo.getDamagedesc(), vo.getCoefficient(), vo.getDamage());
            if (nextVoMap.containsKey(vo.getSkillId())) {
                SkillvupVo nextVo = nextVoMap.get(vo.getSkillId());
                SkillvupVo.writeDamageDescDataToBuff(buff, roleAttack, nextVo.getDamagedesc(), nextVo.getCoefficient(), nextVo.getDamage());
            } else {
                SkillvupVo.writeDamageDescDataToBuff(buff, roleAttack, "", 0, 0);
            }

        }
    }

    public Map<Integer, SkillvupVo> getVoMap() {
        return voMap;
    }

    public void setVoMap(Map<Integer, SkillvupVo> voMap) {
        this.voMap = voMap;
    }

    public Map<Integer, String> getReqItemStr() {
        return reqItemStr;
    }

    public void setReqItemStr(Map<Integer, String> reqItemStr) {
        this.reqItemStr = reqItemStr;
    }

    public Map<Integer, Integer> getCalcDamege() {
        return calcDamege;
    }

    public void setCalcDamege(Map<Integer, Integer> calcDamege) {
        this.calcDamege = calcDamege;
    }

    public Map<Integer, String> getNextLvReqSkillLv() {
        return nextLvReqSkillLv;
    }

    public void setNextLvReqSkillLv(Map<Integer, String> nextLvReqSkillLv) {
        this.nextLvReqSkillLv = nextLvReqSkillLv;
    }

    public Map<Integer, Integer> getNextReqLv() {
        return nextReqLv;
    }

    public void setNextReqLv(Map<Integer, Integer> nextReqLv) {
        this.nextReqLv = nextReqLv;
    }

    public Map<Integer, Integer> getNextCalcDamege() {
        return nextCalcDamege;
    }

    public void setNextCalcDamege(Map<Integer, Integer> nextCalcDamege) {
        this.nextCalcDamege = nextCalcDamege;
    }

    public void setNextVoMap(Map<Integer, SkillvupVo> nextVoMap) {
        this.nextVoMap = nextVoMap;
    }

    public void setRoleAttack(int roleAttack) {
        this.roleAttack = roleAttack;
    }

    public void setSkillCDMap(Map<Integer, Integer> skillCDMap) {
        this.skillCDMap = skillCDMap;
    }
}
