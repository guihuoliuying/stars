package com.stars.modules.familyactivities.bonfire.prodata;

/**
 * Created by wuyuxing on 2017/3/7.
 */
public class FamilyFireVo {
    private int level;                  //等级
    private int reqExp;                 //升级所需经验
    private int subExp;                 //当前篝火等级下每秒减少经验值，填0表示不降低
    private int dropGroupId;
    private String fireEffect;          //填effictid，表示对应篝火等级，篝火npc上播放的燃烧特效

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getReqExp() {
        return reqExp;
    }

    public void setReqExp(int reqExp) {
        this.reqExp = reqExp;
    }

    public int getSubExp() {
        return subExp;
    }

    public void setSubExp(int subExp) {
        this.subExp = subExp;
    }

    public String getFireEffect() {
        return fireEffect;
    }

    public void setFireEffect(String fireEffect) {
        this.fireEffect = fireEffect;
    }

    public int getDropGroupId() {
        return dropGroupId;
    }

    public void setDropGroupId(int dropGroupId) {
        this.dropGroupId = dropGroupId;
    }
}
