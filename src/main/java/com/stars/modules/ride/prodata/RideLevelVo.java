package com.stars.modules.ride.prodata;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/18.
 */
public class RideLevelVo {
    /* Db Data */
	private int id;//流水号
    private int stagelevel; // 坐骑阶级
    private int level; // 坐骑等级
    private int hp; // 坐骑属性：生命
    private int attack; // 坐骑属性：攻击
    private int defense; // 坐骑属性：防御
    private int hit; // 坐骑属性：命中
    private int avoid; // 坐骑属性：闪避
    private int crit; // 坐骑属性：暴击
    private int anticrit; // 坐骑属性：抗暴
    private String reqItem; // 培养所需道具
    private int reqRoleLevel; // 需要人物等级

    /* Mem Data */
    private Map<Integer, Integer> reqItemMap;
    private int fightScore;

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(stagelevel);
        buff.writeInt(level);
        buff.writeInt(hp);
        buff.writeInt(attack);
        buff.writeInt(defense);
        buff.writeInt(hit);
        buff.writeInt(avoid);
        buff.writeInt(crit);
        buff.writeInt(anticrit);
//        buff.writeInt(reqRoleLevel);
//        buff.writeByte((byte) reqItemMap.size());
//        for (Map.Entry<Integer, Integer> entry : reqItemMap.entrySet()) {
//            buff.writeInt(entry.getKey());
//            buff.writeInt(entry.getValue());
//        }
//        // 计算的数据
//        buff.writeInt(fightScore);
    }

    public void calcFightScore() {
        Attribute attr = new Attribute();
        attr.setHp(hp); //
        attr.setAttack(attack);
        attr.setDefense(defense);
        attr.setHit(hit);
        attr.setAvoid(avoid);
        attr.setCrit(crit);
        attr.setAnticrit(anticrit);
        fightScore = FormularUtils.calFightScore(attr);
    }

    /* Mem Data Getter/Setter */
    public Map<Integer, Integer> getReqItemMap() {
        return reqItemMap;
    }

    public void setReqItemMap(Map<Integer, Integer> reqItemMap) {
        this.reqItemMap = reqItemMap;
    }

    /* Db Data Getter/Setter */
    public int getStagelevel() {
		return stagelevel;
	}

	public void setStagelevel(int stagelevel) {
		this.stagelevel = stagelevel;
	}

    public int getLevel() {
        return level;
    }

	public void setLevel(int level) {
        this.level = level;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }

    public int getAvoid() {
        return avoid;
    }

    public void setAvoid(int avoid) {
        this.avoid = avoid;
    }

    public int getCrit() {
        return crit;
    }

    public void setCrit(int crit) {
        this.crit = crit;
    }

    public int getAnticrit() {
        return anticrit;
    }

    public void setAnticrit(int anticrit) {
        this.anticrit = anticrit;
    }

    public String getReqItem() {
        return reqItem;
    }

    public void setReqItem(String reqItem) throws Exception {
        this.reqItem = reqItem;
        this.reqItemMap = StringUtil.toMap(reqItem, Integer.class, Integer.class, '+', '|');
    }

    public int getReqRoleLevel() {
        return reqRoleLevel;
    }

    public void setReqRoleLevel(int reqRoleLevel) {
        this.reqRoleLevel = reqRoleLevel;
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
