package com.stars.modules.camp.prodata;

/**
 * Created by huwenjun on 2017/7/25.
 */
public class CampSkill {
    private int id;
    private String skillid;// 技能id
    private int type;//技能类型
    private String desc;// '技能获得时描述',
    private String job;// 职业
    private int odds;
    private String opencondition;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSkillid() {
        return skillid;
    }

    public void setSkillid(String skillid) {
        this.skillid = skillid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public int getOdds() {
        return odds;
    }

    public void setOdds(int odds) {
        this.odds = odds;
    }

    public String getOpencondition() {
        return opencondition;
    }

    public void setOpencondition(String opencondition) {
        this.opencondition = opencondition;
    }
}
