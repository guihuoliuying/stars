package com.stars.modules.buddy.userdata;import com.stars.core.attr.Attribute;import com.stars.core.attr.FormularUtils;import com.stars.core.db.DBUtil;import com.stars.core.db.DbRow;import com.stars.core.db.SqlUtil;import com.stars.modules.buddy.BuddyManager;import com.stars.modules.buddy.prodata.BuddyArmsVo;import com.stars.modules.buddy.prodata.BuddyLevelVo;import com.stars.modules.buddy.prodata.BuddyStageVo;import com.stars.modules.buddy.prodata.BuddyinfoVo;import com.stars.modules.scene.SceneManager;import com.stars.modules.scene.prodata.MonsterVo;import com.stars.modules.skill.SkillManager;import com.stars.modules.skill.prodata.SkillvupVo;import com.stars.network.server.buffer.NewByteBuffer;import com.stars.util.StringUtil;import java.util.HashMap;import java.util.List;import java.util.Map;/** * Created by liuyuheng on 2016/8/8. */public class RoleBuddy extends DbRow {    private long roleId;// '角色Id'    private int buddyId;// '伙伴id'    private int level;// '当前等级'    private int exp;// '当前经验'    private int stageLevel;// '阶级'    private byte isFollow;// '是否跟随'    private byte isFight;// '是否出战'    private int armLevel;// '武装等级'    private String equip;// '装备,格式:位置=1(已装备)/0(未装备);'    /* 内存数据 */    private Attribute attribute = new Attribute();    private int fightScore;    private int nextStageLvFS;// 下一阶战力    private Map<Byte, Byte> equipMap = new HashMap<>();    public RoleBuddy() {    }    public RoleBuddy(long roleId, int buddyId) {        this.roleId = roleId;        this.buddyId = buddyId;        this.level = BuddyManager.BUDDY_INIT_LEVEL;        this.exp = 0;        this.stageLevel = BuddyManager.BUDDY_INIT_STAGELV;        this.isFollow = BuddyManager.BUDDY_NOT_FOLLOW;        this.isFight = BuddyManager.BUDDY_NOT_FIGHT;        this.armLevel = BuddyManager.BUDDY_INIT_ARMLV;        this.equip = "";        resetEquip();    }    public void writeToBuff(com.stars.network.server.buffer.NewByteBuffer buff) {        buff.writeInt(buddyId);        buff.writeInt(level);        buff.writeInt(exp);        buff.writeInt(stageLevel);        buff.writeInt(armLevel);        buff.writeByte(isFollow);        buff.writeByte(isFight);        buff.writeInt(fightScore);        buff.writeInt(nextStageLvFS);        // 装备穿戴状态        writeEquipStatus(buff);        attribute.writeToBuffer(buff);    }    public void writeEquipStatus(com.stars.network.server.buffer.NewByteBuffer buff) {        byte size = (byte) equipMap.size();        buff.writeByte(size);        if (size != 0) {            for (Map.Entry<Byte, Byte> entry : equipMap.entrySet()) {                buff.writeByte(entry.getKey());// 装备位置Id                buff.writeByte(entry.getValue());// 状态,1(已装备)/0(未装备)            }        }    }    public void writeFollowBuddy(NewByteBuffer buff) {        BuddyinfoVo buddyinfoVo = BuddyManager.getBuddyinfoVo(buddyId);        BuddyStageVo stageVo = BuddyManager.getBuddyStageVo(buddyId, stageLevel);        buff.writeInt(buddyId);        buff.writeString(buddyinfoVo.getName());        buff.writeString(buddyinfoVo.getFollow());        buff.writeString(stageVo.getUseMonsterVo().getHeadIcon());        buff.writeString(stageVo.getUseMonsterVo().getModel());        buff.writeInt(stageVo.getUseMonsterVo().getMoveSpeed());        buff.writeInt(stageVo.getUseMonsterVo().getUiPosition());        buff.writeInt(stageVo.getSceneScale());    }    @Override    public String getChangeSql() {        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolebuddy", " roleid=" + roleId +                " and buddyid=" + buddyId);    }    @Override    public String getDeleteSql() {        return SqlUtil.getDeleteSql("rolebuddy", " roleid=" + roleId + " and buddyid=" + buddyId);    }    /**     * 计算属性     */    public void calAttribute() {        Attribute calAttr = new Attribute();        Attribute nextStageAttr = new Attribute();        // 等级属性        BuddyLevelVo buddyLevelVo = BuddyManager.getBuddyLevelVo(buddyId, level);        if (buddyLevelVo != null) {            calAttr.addAttribute(buddyLevelVo.getAttribute());            nextStageAttr.addAttribute(buddyLevelVo.getAttribute());        }        // 阶级属性        BuddyStageVo buddyStageVo = BuddyManager.getBuddyStageVo(buddyId, stageLevel);        if (buddyStageVo != null) {            calAttr.addAttribute(buddyStageVo.getAttribute());        }        // 下一阶级属性        BuddyStageVo nextStageVo = BuddyManager.getBuddyStageVo(buddyId, stageLevel + 1);        if (nextStageVo != null) {            nextStageAttr.addAttribute(nextStageVo.getAttribute());            nextStageLvFS = FormularUtils.calFightScore(nextStageAttr);        }        // 计算武装属性        BuddyArmsVo armsVo = BuddyManager.getBuddyArmVo(buddyId, armLevel);        if (armsVo != null) {            // 武装等级增加属性            calAttr.addAttribute(armsVo.getArmLevelAttr());            // 穿上的装备增加属性            for (Map.Entry<Byte, Byte> entry : equipMap.entrySet()) {                // 没有装备                if (entry.getValue() != BuddyManager.BUDDY_EQUIP_PUTON)                    continue;                calAttr.addAttribute(armsVo.getEquipAttr(entry.getKey()));            }        }        //根据伙伴调用的怪物的被动技能，计算属性加成        int monsterId = buddyStageVo.getMonsterId();        MonsterVo monsterVo = SceneManager.getMonsterVo(monsterId);        if (monsterVo != null) {            List<Integer> passSkill = monsterVo.getPassSkillList();            for (Integer skillId : passSkill) {                //怪物默认skillLevel是1级的，这里skillId填的必须正好是1级的skillId，否则会拿不到skillvup                SkillvupVo skillvupVo = SkillManager.getSkillvupVo(skillId, 1);                if (skillvupVo != null) {                    skillvupVo.addAttrByEffectInfo(calAttr);                }            }        }        attribute = calAttr;        attribute.setMaxhp(attribute.getHp());        // 根据属性计算战力        fightScore = FormularUtils.calFightScore(attribute);    }    public Map<Integer, Integer> getUseSkill() {        Map<Integer, Integer> skillMap = new HashMap<>();        BuddyStageVo stageVo = BuddyManager.getBuddyStageVo(buddyId, stageLevel);        BuddyArmsVo armsVo = BuddyManager.getBuddyArmVo(buddyId, armLevel);        if (armsVo != null) {            for (Map.Entry<Integer, Integer> entry : stageVo.getUseMonsterVo().getSkillMap().entrySet()) {                if (armsVo.getSkillLevelMap().containsKey(entry.getKey())) {                    skillMap.put(entry.getKey(), armsVo.getSkillLevelMap().get(entry.getKey()));                } else {                    skillMap.put(entry.getKey(), entry.getValue());                }            }        }        return skillMap;    }    public Map<Byte, Byte> getEquipMap() {        return equipMap;    }    public boolean equipAllPutOn() {        for (byte status : equipMap.values()) {            if (status != BuddyManager.BUDDY_EQUIP_PUTON) {                return false;            }        }        return true;    }    public void putOnEquip(byte partId) {        equipMap.put(partId, BuddyManager.BUDDY_EQUIP_PUTON);        buildEquipStr();    }    public void resetEquip() {        BuddyArmsVo buddyArmsVo = BuddyManager.getBuddyArmVo(buddyId, armLevel);        for (byte partId : buddyArmsVo.getEquipMap().keySet()) {            equipMap.put(partId, BuddyManager.BUDDY_EQUIP_NOT_PUTON);        }        buildEquipStr();    }    private void buildEquipStr() {        StringBuilder builder = new StringBuilder("");        if (!equipMap.isEmpty()) {            for (Map.Entry<Byte, Byte> entry : equipMap.entrySet()) {                builder.append(entry.getKey())                        .append("=")                        .append(entry.getValue())                        .append(";");            }            builder.deleteCharAt(builder.lastIndexOf(";"));        }        this.equip = builder.toString();    }    public Attribute getAttribute() {        return attribute;    }    public long getRoleId() {        return roleId;    }    public void setRoleId(long roleId) {        this.roleId = roleId;    }    public int getBuddyId() {        return buddyId;    }    public void setBuddyId(int buddyId) {        this.buddyId = buddyId;    }    public int getLevel() {        return level;    }    public void setLevel(int level) {        this.level = level;    }    public int getExp() {        return exp;    }    public void setExp(int exp) {        this.exp = exp;    }    public int getStageLevel() {        return stageLevel;    }    public void setStageLevel(int stageLevel) {        this.stageLevel = stageLevel;    }    public byte getIsFollow() {        return isFollow;    }    public void setIsFollow(byte isFollow) {        this.isFollow = isFollow;    }    public byte getIsFight() {        return isFight;    }    public void setIsFight(byte isFight) {        this.isFight = isFight;    }    public int getFightScore() {        return fightScore;    }    public int getArmLevel() {        return armLevel;    }    public void setArmLevel(int armLevel) {        this.armLevel = armLevel;    }    public String getEquip() {        return equip;    }    public void setEquip(String equip) throws Exception {        this.equip = equip;        if (StringUtil.isEmpty(equip)) {            return;        }        this.equipMap = StringUtil.toMap(equip, Byte.class, Byte.class, '=', ';');    }    public int getNextStageLvFS() {        return nextStageLvFS;    }}