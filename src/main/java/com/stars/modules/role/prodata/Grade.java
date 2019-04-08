package com.stars.modules.role.prodata;

import com.stars.core.attr.Attribute;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 角色等级表;
 * Created by panzhenfeng on 2016/6/22.
 */
public class Grade {
    private int id ;
    private int job;
    private int level;
    private int reqexp;
    private int hp;
    private int mp;
    private int attack;
    private int defense;
    private int hit;
    private int avoid;
    private int crit;
    private int anticrit;
    private int crithurtadd;
    private int crithurtreduce;
    private int vigorMax;
    private String buyMoney;// 购买金币消耗和获得
    private byte freeCount;// 免费购买金币次数
    private byte payCount;// 付费购买金币次数
    private short odds;// 购买金币翻倍几率
    private String multiple;// 购买金币倍率

    // 属性对象
    private Attribute attribute = new Attribute();
    // 购买金币消耗
    private Map<Integer, Integer> buyMoneyCost = new HashMap<>();
    // 购买金币获得
    private Map<Integer, Integer> buyMoneyAward = new HashMap<>();
    // 购买金币倍率随机范围
    private int[] multipleArray;

    public Attribute getAttribute() {
        return attribute;
    }

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(this.getReqexp());
        buff.writeInt(this.getHp());
        buff.writeInt(this.getMp());
        buff.writeInt(this.getAttack());
        buff.writeInt(this.getDefense());
        buff.writeInt(this.getHit());
        buff.writeInt(this.getAvoid());
        buff.writeInt(this.getCrit());
        buff.writeInt(this.getAnticrit());
        buff.writeInt(this.getCrithurtadd());
        buff.writeInt(this.getCrithurtreduce());
    }

    /**
     * 获得购买金币随机倍率
     *
     * @return
     */
    public int getBuyMoneyMulti() {
        return new Random().nextInt(multipleArray[1] - multipleArray[0]) + multipleArray[0];
    }

    public Map<Integer, Integer> getBuyMoneyCost() {
        return buyMoneyCost;
    }

    public Map<Integer, Integer> getBuyMoneyAward() {
        return buyMoneyAward;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJob() {
        return job;
    }

    public void setJob(int job) {
        this.job = job;
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
        attribute.setHp(hp);
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
        attribute.setMp(mp);
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

    public int getCrit() {
        return crit;
    }

    public void setCrit(int crit) {
        this.crit = crit;
        attribute.setCrit(crit);
    }

    public int getAnticrit() {
        return anticrit;
    }

    public void setAnticrit(int anticrit) {
        this.anticrit = anticrit;
        attribute.setAnticrit(anticrit);
    }

    public int getCrithurtadd() {
        return crithurtadd;
    }

    public void setCrithurtadd(int crithurtadd) {
        this.crithurtadd = crithurtadd;
        attribute.setCrithurtadd(crithurtadd);
    }

    public int getCrithurtreduce() {
        return crithurtreduce;
    }

    public void setCrithurtreduce(int crithurtreduce) {
        this.crithurtreduce = crithurtreduce;
        attribute.setCrithurtreduce(crithurtreduce);
    }

    public int getReqexp() {
        return reqexp;
    }

    public void setReqexp(int reqexp) {
        this.reqexp = reqexp;
    }

    public int getVigorMax() {
        return vigorMax;
    }

    public void setVigorMax(int vigorMax) {
        this.vigorMax = vigorMax;
    }

    public String getBuyMoney() {
        return buyMoney;
    }

    public void setBuyMoney(String buyMoney) {
        this.buyMoney = buyMoney;
        if (StringUtil.isEmpty(buyMoney) || "0".equals(buyMoney)) {
            return;
        }
        String[] temp = buyMoney.split("\\|");
        buyMoneyCost = StringUtil.toMap(temp[0], Integer.class, Integer.class, '+', ',');
        buyMoneyAward = StringUtil.toMap(temp[1], Integer.class, Integer.class, '+', ',');
    }

    public byte getFreeCount() {
        return freeCount;
    }

    public void setFreeCount(byte freeCount) {
        this.freeCount = freeCount;
    }

    public byte getPayCount() {
        return payCount;
    }

    public void setPayCount(byte payCount) {
        this.payCount = payCount;
    }

    public short getOdds() {
        return odds;
    }

    public void setOdds(short odds) {
        this.odds = odds;
    }

    public String getMultiple() {
        return multiple;
    }

    public void setMultiple(String multiple) throws Exception {
        this.multiple = multiple;
        if (StringUtil.isEmpty(multiple) || "0".equals(multiple)) {
            throw new IllegalArgumentException("grade表multiple字段配置错误");
        }
        multipleArray = StringUtil.toArray(multiple, int[].class, '+');
    }
}
