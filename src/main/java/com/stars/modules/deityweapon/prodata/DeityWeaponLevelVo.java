package com.stars.modules.deityweapon.prodata;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 神兵升级表;
 * Created by panzhenfeng on 2016/12/14.
 */
public class DeityWeaponLevelVo {
    private byte type; //整值，神兵类型;
    private int deityweaponlvl; //	整值, 神兵等级,从0开始
    private String cost; //	字符串, 升级花费, 格式为 itemid+数量
    private int levellimit; //	升级到当前级需要的角色等级
    private int hp; //	整值, 神兵属性
    private int attack; //	整值, 神兵属性
    private int defense; //	整值, 神兵属性
    private int hit; //	整值, 神兵属性
    private int avoid; //	整值, 神兵属性
    private int crit; //	整值, 神兵属性
    private int anticrit; // 整值, 神兵属性
    //内存数据;
    private Attribute attribute = new Attribute();
    private Map<Integer, Integer> costMap = new HashMap<>();

    private Map<Integer, Map<Integer, Integer>> job2CostMap = new HashMap<>();

    public void writeBuff(NewByteBuffer buff) {
        buff.writeByte(type);
        buff.writeInt(deityweaponlvl);
        buff.writeString(cost);
        buff.writeInt(levellimit);
        buff.writeInt(FormularUtils.calFightScore(attribute));
        attribute.writeToBuffer(buff);
    }

    public Attribute getAttribute(){
        return attribute;
    }

    public Map<Integer, Integer> getCostMap(){
        return this.costMap;
    }

    public int getDeityweaponlvl() {
        return deityweaponlvl;
    }

    public void setDeityweaponlvl(int deityweaponlvl) {
        this.deityweaponlvl = deityweaponlvl;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) throws Exception {
        this.cost = cost;
//        costMap.clear();
//        String[] costIdCount = this.cost.split("[+]");
//        costMap.put(Integer.parseInt(costIdCount[0]), Integer.parseInt(costIdCount[1]));

        job2CostMap = new HashMap<>();
        String[] jobCostArray = StringUtil.toArray(cost, String[].class, '|');
        for (String str : jobCostArray) {
            String[] keyValPair = StringUtil.toArray(str, String[].class, ',');
            job2CostMap.put(Integer.parseInt(keyValPair[0]),
                    StringUtil.toMap(keyValPair[1], Integer.class, Integer.class, '+', ','));
        }
    }

    public int getLevellimit() {
        return levellimit;
    }

    public void setLevellimit(int levellimit) {
        this.levellimit = levellimit;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
        attribute.setHp(hp);
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
        attribute.setAttack(attack);
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
        attribute.setDefense(defense);
    }

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
        attribute.setHit(hit);
    }

    public int getAvoid() {
        return avoid;
    }

    public void setAvoid(int avoid) {
        this.avoid = avoid;
        attribute.setAvoid(avoid);
    }

    public int getAnticrit() {
        return anticrit;
    }

    public void setAnticrit(int anticrit) {
        this.anticrit = anticrit;
        attribute.setAnticrit(anticrit);
    }

    public int getCrit() {
        return crit;
    }

    public void setCrit(int crit) {
        this.crit = crit;
        attribute.setCrit(crit);
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public Map<Integer, Integer> getCostByJobId(int jobId) {
        if (job2CostMap.containsKey(jobId)) {
            return job2CostMap.get(jobId);
        }
        return new HashMap<>();
    }

}
