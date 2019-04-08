package com.stars.modules.family.prodata;

/**
 * Created by zhaowenshuo on 2016/9/8.
 */
public class FamilySkillVo {
    /* Db Data */
    private String attribute;
    private int level;
    private int value;
    private int reqRoleLevel;
    private int reqContribution;

    /* Db Data Getter/Setter */
    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getReqRoleLevel() {
        return reqRoleLevel;
    }

    public void setReqRoleLevel(int reqRoleLevel) {
        this.reqRoleLevel = reqRoleLevel;
    }

    public int getReqContribution() {
        return reqContribution;
    }

    public void setReqContribution(int reqContribution) {
        this.reqContribution = reqContribution;
    }
}
