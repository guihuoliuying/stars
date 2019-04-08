package com.stars.modules.skill.prodata;

import com.stars.core.attr.Attr;
import com.stars.core.attr.Attribute;
import com.stars.modules.skill.SkillConstant;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;
import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dengzhou
 *         <p/>
 *         技能升级对象
 */
public class SkillvupVo implements Comparable<SkillvupVo> {
    /**
     * 技能id	填skill表skillid，对应指定技能，可重复
     */
    private int skillId;

    /**
     * 技能类型 主动技能|被动技能
     */
    private byte skillType;
    /**
     * 技能等级	填整数，表示对应技能等级，同一个技能skillid，等级不能重复
     */
    private int level;
    /**
     * 技能系数	填整数，表示技能的系数，除以1000后用于计算技能伤害，详见技能计算公式
     */
    private int coefficient;
    /**
     * 技能伤害值	填整数，表示技能伤害值，用于计算技能伤害，详见技能计算公式。
     */
    private int damage;
    /**
     * 冷却时间	填整数，表示毫秒，技能的冷却时间，详见技能冷却。
     */
    private int cooldown;
    /**
     * 所需材料	填itemid=数量|itemid=数量，表示提升到当前等级所需材料及数量，如果当前level为1，表示激活该技能需要的材料。填0表示无需求。
     */
    private Map<Integer, Integer> reqitem;
    /**
     * 所需等级	填整数，表示角色等级，提升到当前技能等级需要满足角色等级条件。填0表示无条件。
     */
    private int reqlv;

    /**
     * 填dungeonid，表示主线关卡中打败对应关卡boss时，开启技能，无条件填0。
     */
    private int reqdungeon;
    /**
     * 技能战力	填整数，表示此技能给角色附加的战力
     */
    private int battlepower;

    /**
     * 触发条件	类型|参数。类型0:无条件触发，参数，无；类型1：命中附加，参数，无；类型2：技能附加，参数，无；类型3：血量条件，参数，千分比
     */
    private String conditions;

    /**
     * 触发效果	类型|参数，类型1，代表增加了属性，参数为，属性ID+千分比+数值。类型2，代表buff，参数为，target+buff，其中target，0代表敌人，1代表自己。
     */
    private String effectinfo;

    private String skilldesc;

    private String describ;

    private int reqbook;

    /**
     * 技能伤害值	填整数，表示技能伤害值，用于计算技能伤害，详见技能计算公式。
     */
    private String damagedesc;

    private String reqskilllevel;

    private int calcdamagedesc;
    private int nextCalcDamegeDesc;

    private String reqItemStr;

    private int dragonBallid; //技能外显iD

    private Map<Integer, Integer> reqskilllevelMap;

    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(this.skillId);
        buff.writeInt(this.level);
        buff.writeString(this.conditions);
        buff.writeString(this.effectinfo);
        buff.writeInt(this.coefficient);
        buff.writeInt(this.damage);
        buff.writeInt(this.cooldown);
        buff.writeByte(this.skillType);
        buff.writeInt(this.reqlv);
    }

    public int getCalcdamagedesc() {
        return calcdamagedesc;
    }

    public void setCalcdamagedesc(int calcdamagedesc) {
        this.calcdamagedesc = calcdamagedesc;
    }

    public String getDamagedesc() {
        return damagedesc;
    }

    public void setDamagedesc(String damagedesc) {
        this.damagedesc = damagedesc;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(int coefficient) {
        this.coefficient = coefficient;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public Map<Integer, Integer> getReqitem() {
        return reqitem;
    }

    public void setReqitem(String reqItem) {
        if (reqItem == null || reqItem.equals("") || reqItem.equals("0")) {
            setReqItemStr("0");
            return;
        }
        setReqItemStr(reqItem);
        this.reqitem = new HashMap<>();
        String[] sts1 = reqItem.split("\\|");
        for (String str : sts1) {
            String[] sts2 = str.split("[=]");
            this.reqitem.put(Integer.parseInt(sts2[0]), Integer.parseInt(sts2[1]));
        }
    }

    public int getReqlv() {
        return reqlv;
    }

    public void setReqlv(int reqlv) {
        this.reqlv = reqlv;
    }

    public int getReqdungeon() {
        return reqdungeon;
    }

    public void setReqdungeon(int reqdungeon) {
        this.reqdungeon = reqdungeon;
    }

    public String getReqskilllevel() {
        return reqskilllevel;
    }

    public void setReqskilllevel(String reqskilllevel) {
        if (reqskilllevel == null || reqskilllevel.equals("") || reqskilllevel.equals("0")) {
            this.reqskilllevelMap = new HashMap<>();
            return;
        }
        this.reqskilllevel = reqskilllevel;
        if (reqskilllevel.equals("0") || reqskilllevel == null) return;
        this.reqskilllevelMap = new HashMap<>();
        this.reqskilllevelMap = StringUtil.toMap(reqskilllevel, Integer.class, Integer.class, '+', '|');
    }

    public Map<Integer, Integer> getReqskilllevelMap() {
        return reqskilllevelMap;
    }

    public String getSkilldesc() {
        return skilldesc;
    }

    public void setSkilldesc(String skilldesc) {
        this.skilldesc = skilldesc;
    }

    public int getBattlepower() {
        return battlepower;
    }

    public void setBattlepower(int battlepower) {
        this.battlepower = battlepower;
    }

    public String getReqItemStr() {
        return reqItemStr;
    }

    public void setReqItemStr(String reqItemStr) {
        this.reqItemStr = reqItemStr;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String condition) {
        this.conditions = condition;
    }

    public String getEffectinfo() {
        return effectinfo;
    }

    public void setEffectinfo(String effectinfo) {
        this.effectinfo = effectinfo;
    }

    public int getNextCalcDamegeDesc() {
        return nextCalcDamegeDesc;
    }

    public void setNextCalcDamegeDesc(int nextCalcDamegeDesc) {
        this.nextCalcDamegeDesc = nextCalcDamegeDesc;
    }

    public int getBuffId() {
        if (this.effectinfo != "0") {
            String[] tempStr = this.effectinfo.split("\\|");
            if (tempStr[0].equals("2")) { //效果是加buff的类型
                String[] paramStr = tempStr[1].split("\\+");
                return Integer.parseInt(paramStr[1]);
            }
        }
        return 0;
    }

    public String getDescrib() {
        return describ;
    }

    public void setDescrib(String describ) {
        this.describ = describ;
    }

    public byte getSkillType() {
        return skillType;
    }

    public void setSkillType(byte skillType) {
        this.skillType = skillType;
    }

    public int getReqbook() {
        return reqbook;
    }

    public void setReqbook(int reqbook) {
        this.reqbook = reqbook;
    }

    public int getDragonBallid() {
        return dragonBallid;
    }

    public void setDragonBallid(int dragonBallid) {
        this.dragonBallid = dragonBallid;
    }

    public int compareTo(SkillvupVo o) {
        return o.getBattlepower() - this.battlepower;
    }

    /**
     * 通过effectinfo字段计算出被动技能带来的属性加成
     * baseAttr：计算时的基础属性
     */
    public void addAttrByEffectInfo(Attribute baseAttr) {
        if (getSkillType() == SkillConstant.LVUP_SKILLTYPE_PASS ||
                getSkillType() == SkillConstant.TRUMP_SKILLTYPE_PASS) {
            String[] tempStr = getEffectinfo().split("\\|");
            if (Integer.parseInt(tempStr[0]) == SkillConstant.PASS_EFFECT_ATTRIBUTE) {
                String[] sts = tempStr[1].split("\\+");
                int value = baseAttr.getAttributes()[Attr.getIndexByteEn(sts[0])];
                int addValue = (int) (value * Integer.parseInt(sts[1]) * 0.001 + Integer.parseInt(sts[2]));
                value += addValue;
                baseAttr.setSingleAttr(Attr.getIndexByteEn(sts[0]), value);
            }
        }
    }

    public static void writeDamageDescDataToBuff(com.stars.network.server.buffer.NewByteBuffer buffer, int roleAttack, String damageDesc, int coefficient, int damage) {
        byte size = 0;
        if (damageDesc == null || damageDesc.equals("") || damageDesc.equals("0")) {
            buffer.writeByte(size);
            return;
        }

        String sts[] = damageDesc.split("\\|");
        size = (byte) sts.length;
        buffer.writeByte(size);

        for (String unit : sts) {
            int type = 0;
            int paramInt = 0;

            String unitSts[] = unit.split("\\+");
            if (unitSts.length == 2) {
                type = Integer.parseInt(unitSts[0]);
                float param = Float.parseFloat(unitSts[1]);
                paramInt = (int) (param * 1000);
            } else if (unitSts.length == 3) {
                type = Integer.parseInt(unitSts[0]);
                int a = Integer.parseInt(unitSts[1]);
                int b = Integer.parseInt(unitSts[2]);
                paramInt = (int) Math.ceil(((roleAttack * ((float) coefficient / 1000) + damage) * a + b));
            }

            buffer.writeByte((byte) type);
            buffer.writeInt(paramInt);
        }
    }

//    public static LinkedList<int[]> getDamageDescData(int roleAttack , String damageDesc , int coefficient , int damage){
//    	LinkedList<int[]> data = new LinkedList<int[]>();
//    	if (damageDesc == null || damageDesc.equals("") || damageDesc.equals("0")) {
//			return data;
//		}
//    	
//    	String sts[] = damageDesc.split("\\|");
//    	for (String unit : sts) {
//			String unitSts[] = unit.split("\\+");
//			int[] unitData = new int[2];
//			if (unitSts.length == 2) {
//				int type = Integer.parseInt(unitSts[0]);
//				float param = Float.parseFloat(unitSts[1]);
//				int paramInt = (int)(param * 1000);
//				unitData[0] = type;
//				unitData[1] = paramInt;
//				data.add(unitData);
//			}else if (unitSts.length == 3) {
//				int type = Integer.parseInt(unitSts[0]);
//				int a = Integer.parseInt(unitSts[1]);
//                int b = Integer.parseInt(unitSts[2]);
//                int paramInt = (roleAttack * coefficient / 1000 + damage) * a + b;
//                unitData[0] = type;
//				unitData[1] = paramInt;
//				data.add(unitData);
//			}
//		}
//    	
//    	return data;
//    }

    public static void main(String args[]) {
        com.stars.network.server.buffer.NewByteBuffer buffer = new NewByteBuffer(Unpooled.buffer());
        writeDamageDescDataToBuff(buffer, 15565, "1+8+0", 167, 15);
    }
}
