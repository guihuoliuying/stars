package com.stars.modules.skill.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.modules.newequipment.NewEquipmentManager;
import com.stars.modules.skill.SkillConstant;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillvupVo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class RoleSkill extends DbRow {

    /**
     * key:skillId  value:level
     */
    private Map<Integer, Integer> skillLevel;
    private Map<Byte, Integer> useSkills;
    private Set<Integer> pendingSkills;

    private long roleId;

    public RoleSkill(long roleId) {
        this.roleId = roleId;
        skillLevel = new HashMap<Integer, Integer>();
        useSkills = new HashMap<Byte, Integer>();
        pendingSkills = new HashSet<>();
    }

    //	TODO:异常这边后续处理
    @Override
    public String getChangeSql() {
        if ((skillLevel == null || skillLevel.size() == 0) && (useSkills == null || useSkills.size() == 0)) {
            return "";
        }
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleskill",
                " roleid=" + this.roleId);
    }

    @Override
    public String getDeleteSql() {
        return "";
    }

    public void setSkillLevelStr(String skillLevelStr) {

        if (skillLevelStr == null || skillLevelStr.equals("")) {
            return;
        }

        String[] sts1 = skillLevelStr.split("[&]");
        for (String string : sts1) {
            String[] sts2 = string.split("[=]");
            skillLevel.put(Integer.parseInt(sts2[0]), Integer.parseInt(sts2[1]));
        }
    }

    public void setSkillLevel(String str) {
    }

    public String getSkillLevel() {
        Set<Entry<Integer, Integer>> set = skillLevel.entrySet();
        StringBuffer buffer = new StringBuffer();
        for (Entry<Integer, Integer> entry : set) {
            if (buffer.length() > 0) {
                buffer.append("&");
            }
            buffer.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return buffer.toString();
    }

    //数据库需求，先临时处理
    public void setUseSkills(String str) {

    }

    public int getSkillLv(int skillId) {
        return skillLevel.containsKey(skillId) ? skillLevel.get(skillId) : 0;
    }

    public void upSkillLevel(int skillId) {
        if (skillLevel.containsKey(skillId)) {
            skillLevel.put(skillId, skillLevel.get(skillId) + 1);
        } else {
            skillLevel.put(skillId, 1);
        }
    }

    public void setPendingSkills(String pendingSkillStr) {
        if (pendingSkillStr != null && !pendingSkillStr.isEmpty()) {
            String[] strings = pendingSkillStr.split("\\+");
            for (String str : strings) {
                pendingSkills.add(Integer.parseInt(str));
            }
        }

    }

    public String getPendingSkills() {
        StringBuffer buffer = new StringBuffer();
        for (int skillId : pendingSkills) {
            buffer.append(skillId).append("+");
        }
        if (buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        return buffer.toString();
    }


    public String getUseSkills() {
        Set<Entry<Byte, Integer>> set = useSkills.entrySet();
        StringBuffer buffer = new StringBuffer();
        int index = 0;
        for (Entry<Byte, Integer> entry : set) {
            if (index > 0) {
                buffer.append("|");
            }
            buffer.append(entry.getKey());
            buffer.append("=");
            buffer.append(entry.getValue());
            index++;
        }
        return buffer.toString();
    }

    public void setUseSkill(String useSkillStr) {
        if (useSkillStr == null || useSkillStr.equals("")) {
            return;
        }
        String[] sts1 = useSkillStr.split("[|]");
        for (String string : sts1) {
            String[] sts2 = string.split("[=]");
            useSkills.put(Byte.parseByte(sts2[0]), Integer.parseInt(sts2[1]));
        }
    }

    public void putSkillLevel(int skillId, int level) {
        this.skillLevel.put(skillId, level);
    }

    public void putUseSkill(byte position, int SkillId) {
        this.useSkills.put(position, SkillId);
    }

    public void addPendingSkill(int skillId) {
        this.pendingSkills.add(skillId);
    }

    public void delPendingSkill(int skillId) {
        if (this.pendingSkills.contains(skillId)) {
            this.pendingSkills.remove(skillId);
        }
    }

    public int getSkillId(byte position) {
        return this.useSkills.containsKey(position) ? this.useSkills.get(position) : 0;
    }

    public byte getSkillPostion(int skillid) {
        Set<Entry<Byte, Integer>> set = this.useSkills.entrySet();
        for (Entry<Byte, Integer> entry : set) {
            if (entry.getValue() == skillid) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public void clearPosition(byte position) {
        this.useSkills.remove(position);
    }

    public String getUsePassSkillStr() {
        Set<Entry<Integer, Integer>> set = skillLevel.entrySet();
        StringBuffer buffer = new StringBuffer();
        SkillvupVo skillvo;
        int index = -1;
        for (Entry<Integer, Integer> entry : set) {
            skillvo = SkillManager.getSkillvupVo(entry.getKey(), entry.getValue());
            //此处判断已佩戴或符文或法宝的被动技能才生效，应该从skilltype去区分，现逻辑有点乱
            if ((!useSkills.containsValue(skillvo.getSkillId())
                    && NewEquipmentManager.getTokenSkillVoBySkillId(skillvo.getSkillId()) == null)
                    && skillvo.getSkillType() != SkillConstant.TRUMP_SKILLTYPE_PASS
                    && skillvo.getSkillType() != SkillConstant.FASHIONCARD_SKILLTYPE_PASS) continue;
            if (skillvo.getSkillType() == SkillConstant.LVUP_SKILLTYPE_PASS ||
                    skillvo.getSkillType() == SkillConstant.TRUMP_SKILLTYPE_PASS
                    || skillvo.getSkillType() == SkillConstant.FASHIONCARD_SKILLTYPE_PASS) { //被动技能类型
                index = getSkillPostion(skillvo.getSkillId());
                if (buffer.length() > 0) {
                    buffer.append("|");
                }
                //buffer.append(index).append("+").append(entry.getKey()).append("+").append(entry.getValue());
                buffer.append(entry.getKey()).append("+").append(entry.getValue());
            }
        }
        return buffer.toString();
    }

    public Map<Integer, Integer> getUsePassSkillList() {
        Map<Integer, Integer> useList = new HashMap<>();
        Set<Entry<Integer, Integer>> set = skillLevel.entrySet();
        SkillvupVo skillvo;
        for (Entry<Integer, Integer> entry : set) {
            skillvo = SkillManager.getSkillvupVo(entry.getKey(), entry.getValue());
            //此处判断已佩戴或符文或法宝的被动技能才生效，应该从skilltype去区分，现逻辑有点乱
            if ((!useSkills.containsValue(skillvo.getSkillId()))  //不在使用，而且不是符文技能
                    && NewEquipmentManager.getTokenSkillVoBySkillId(skillvo.getSkillId()) == null
                    && skillvo.getSkillType() != SkillConstant.TRUMP_SKILLTYPE_PASS
                    && skillvo.getSkillType() != SkillConstant.FASHIONCARD_SKILLTYPE_PASS)
                continue;
            if (skillvo.getSkillType() == SkillConstant.LVUP_SKILLTYPE_PASS ||
                    skillvo.getSkillType() == SkillConstant.TRUMP_SKILLTYPE_PASS
                    || skillvo.getSkillType() == SkillConstant.FASHIONCARD_SKILLTYPE_PASS) { //被动技能类型
                useList.put(entry.getKey(), entry.getValue());
            }
        }
        return useList;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public Map<Integer, Integer> getSkillLevelMap() {
        return this.skillLevel;
    }

    public Map<Byte, Integer> getUseSkillMap() {
        return this.useSkills;
    }

    public Set<Integer> getPendingSkillSet() {
        return this.pendingSkills;
    }

    public void setSkillLevel(Map<Integer, Integer> skillLevel) {
        this.skillLevel = skillLevel;
    }

    public void setUseSkills(Map<Byte, Integer> useSkills) {
        this.useSkills = useSkills;
    }

    public void setPendingSkills(Set<Integer> pendingSkills) {
        this.pendingSkills = pendingSkills;
    }
}
