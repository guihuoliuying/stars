package com.stars.modules.newofflinepvp.prodata;

import com.stars.core.attr.Attribute;
import com.stars.modules.skill.SkillManager;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-03-12 16:50
 */
public class OfflineInitializeVo {
    private long initializeId;      //填流水号，表示机器人，填1到1000，不多不少，用于初始化
    private int robotLevel;         //填整数，表示机器人等级
    private String robotName;       //直接填中文，表示名字
    private int jobId;              //填1、2、3、4，表示1剑尊2墨客3魅影1弓箭手
    private int hp;                 //
    private int attack;             //
    private int defense;            //
    private int hit;                //
    private int avoid;              //
    private int crit;               //
    private int antiCrit;           //
    private String robotSkill;      //技能id+技能等级+附魔额外伤害, 技能id+技能等级+附魔额外伤害, 普攻技能不配置
    private int robotFightScore;    //填整数，表示战力
    private int buddyId;            //填伙伴id, 填0代表无宠物
    private int buddyLevel;         //伙伴等级
    private int buddyStageLevel;    //关联到buddystage表，根据monsterid到怪物表找到配置的技能
    private int buddyFightScore;    //

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

    public long getInitializeId() {
        return initializeId;
    }

    public void setInitializeId(long initializeId) {
        this.initializeId = initializeId;
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

    public int getAntiCrit() {
        return antiCrit;
    }

    public void setAntiCrit(int antiCrit) {
        this.antiCrit = antiCrit;
        robotAttribute.setAnticrit(antiCrit);
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

    public int getBuddyStageLevel() {
        return buddyStageLevel;
    }

    public void setBuddyStageLevel(int buddyStageLevel) {
        this.buddyStageLevel = buddyStageLevel;
    }

    public int getBuddyFightScore() {
        return buddyFightScore;
    }

    public void setBuddyFightScore(int buddyFightScore) {
        this.buddyFightScore = buddyFightScore;
    }
}
