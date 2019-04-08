package com.stars.services.family.main.prodata;

/**
 * Created by zhaowenshuo on 2016/8/25.
 */
public class FamilyLevelVo {

    private int level;
    private int requiredMoney;
    private int memberLimit; // 成员上限
    private int skillLimit; // 家族心法上限

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRequiredMoney() {
        return requiredMoney;
    }

    public void setRequiredMoney(int requiredMoney) {
        this.requiredMoney = requiredMoney;
    }

    public int getMemberLimit() {
        return memberLimit;
    }

    public void setMemberLimit(int memberLimit) {
        this.memberLimit = memberLimit;
    }

    public int getSkillLimit() {
        return skillLimit;
    }

    public void setSkillLimit(int skillLimit) {
        this.skillLimit = skillLimit;
    }
}
