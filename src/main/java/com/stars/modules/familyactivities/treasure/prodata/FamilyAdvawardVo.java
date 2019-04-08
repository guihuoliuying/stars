package com.stars.modules.familyactivities.treasure.prodata;

/**
 * 2.3.	家族探宝伤害奖励表
 * Created by chenkeyu on 2017/2/10 11:34
 */
public class FamilyAdvawardVo {
    private int group;      //伤害组        填正整数，相同表示同一组
    private String damage;  //伤害值        表示伤害量分段大值，左开右闭分段
    private int dropId;     //掉落奖励      填drop表groupid，表示对应的掉落奖励

    //内存数据
    private int minDamage;
    private int maxDamage;

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public String getDamage() {
        return damage;
    }

    public void setDamage(String damage) {
        this.damage = damage;
        String[] damageStr = damage.split("\\+");
        this.minDamage = Integer.parseInt(damageStr[0]);
        this.maxDamage = Integer.parseInt(damageStr[1]);
    }

    public int getDropId() {
        return dropId;
    }

    public void setDropId(int dropId) {
        this.dropId = dropId;
    }

    public int getMinDamage() {
        return minDamage;
    }

    public int getMaxDamage() {
        return maxDamage;
    }
}
