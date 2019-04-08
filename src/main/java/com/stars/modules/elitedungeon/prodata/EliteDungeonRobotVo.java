package com.stars.modules.elitedungeon.prodata;

import com.stars.core.attr.Attribute;
import com.stars.modules.skill.SkillManager;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class EliteDungeonRobotVo {
	
	private int robotId;// '机器人唯一id'
    private int robotLevel;// '机器人等级'
    private String robotName;// '机器人名字'
    private int jobId;// '机器人职业'
    private int hp;// '生命'
    private int attack;// '攻击'
    private int defense;// '防御'
    private int hit;// '命中'
    private int avoid;// '闪避'
    private int crit;// '暴击'
    private int anticrit;// '抗暴'
    private String robotSkill;// '机器人技能'
    private int robotFightScore;// '机器人战力'
    private int buddyId;// '伙伴id'
    private int buddyLevel;// '伙伴等级'
    private int buddyStageLevel;// '伙伴阶级'
    private int buddyFightScore;// '伙伴战力'
    
    /* 内存数据 */
    private Attribute robotAttribute = new Attribute();// 机器人属性
    private Map<Integer, Integer> robotSkillMap = new HashMap<>();// 角色技能等级, <id, level>
    private Map<Integer, Integer> robotSkillDamage = new HashMap<>();// 技能附加伤害(装备附魔增加),<id, damage>

    public Attribute getRobotAttribute() {
        return robotAttribute;
    }

    public Map<Integer, Integer> getRobotSkillMap() {
        return robotSkillMap;
    }

    public Map<Integer, Integer> getRobotSkillDamage() {
        return robotSkillDamage;
    }

    public int getRobotId() {
        return robotId;
    }

    public void setRobotId(int robotId) {
        this.robotId = robotId;
    }

    public int getRobotLevel() {
        return robotLevel;
    }

    public void setRobotLevel(int robotLevel) {
        this.robotLevel = robotLevel;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
        robotAttribute.setHp(hp);
        robotAttribute.setMaxhp(hp);
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
        robotAttribute.setAttack(attack);
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
        robotAttribute.setDefense(defense);
    }

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
        robotAttribute.setHit(hit);
    }

    public int getAvoid() {
        return avoid;
    }

    public void setAvoid(int avoid) {
        this.avoid = avoid;
        robotAttribute.setAvoid(avoid);
    }

    public int getCrit() {
        return crit;
    }

    public void setCrit(int crit) {
        this.crit = crit;
        robotAttribute.setCrit(crit);
    }

    public int getAnticrit() {
        return anticrit;
    }

    public void setAnticrit(int anticrit) {
        this.anticrit = anticrit;
        robotAttribute.setAnticrit(anticrit);
    }

    public String getRobotSkill() {
        return robotSkill;
    }

    public void setRobotSkill(String robotSkill) {
        this.robotSkill = robotSkill;
        if (StringUtil.isEmpty(robotSkill) || "0".equals(robotSkill)) {
            return;
        }
        Map<Integer, Integer> skillMap = new HashMap<>();
        Map<Integer, Integer> damageMap = new HashMap<>();
        for (String temp : robotSkill.split(",")) {
            String[] delta = temp.split("\\+");
            int skillId = Integer.parseInt(delta[0]);
            int level = Integer.parseInt(delta[1]);
            int damage = Integer.parseInt(delta[2]);
            if (SkillManager.getSkillVo(skillId) == null) {
                com.stars.util.LogUtil.error("robot表数据错误,找不到skillId={}的skill数据", skillId);
                throw new IllegalArgumentException();
            }
            if (SkillManager.getSkillvupVo(skillId, level) == null) {
                LogUtil.error("robot表数据错误,找不到skillId={},level={}的skilllvup数据", skillId, level);
                throw new IllegalArgumentException();
            }
            skillMap.put(skillId, level);
            damageMap.put(skillId, damage);
        }
        robotSkillMap = skillMap;
        robotSkillDamage = damageMap;
    }

    public int getRobotFightScore() {
        return robotFightScore;
    }

    public void setRobotFightScore(int robotFightScore) {
        this.robotFightScore = robotFightScore;
    }

    public int getBuddyId() {
        return buddyId;
    }

    public void setBuddyId(int buddyId) {
        this.buddyId = buddyId;
    }

    public int getBuddyLevel() {
        return buddyLevel;
    }

    public void setBuddyLevel(int buddyLevel) {
        this.buddyLevel = buddyLevel;
    }

    public int getBuddyFightScore() {
        return buddyFightScore;
    }

    public void setBuddyFightScore(int buddyFightScore) {
        this.buddyFightScore = buddyFightScore;
    }

    public int getBuddyStageLevel() {
        return buddyStageLevel;
    }

    public void setBuddyStageLevel(int buddyStageLevel) {
        this.buddyStageLevel = buddyStageLevel;
    }

}
