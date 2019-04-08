package com.stars.modules.poemdungeon.prodata;

import com.stars.core.attr.Attribute;
import com.stars.modules.skill.SkillManager;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2017/5/13.
 */
public class PoemRobotVo {
    private int poemrobotid;
    private int robotlevel;
    private String robotname;
    private int jobid;
    private int hp;
    private int attack;
    private int defense;
    private int hit;
    private int avoid;
    private int crit;
    private int anticrit;
    private String robotskill;
    private int buddyid;
    private int buddylevel;
    private int buddystagelevel;
    private int robotfightscore;
    private int dungeonid;
    
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
    
    public int getPoemRobotId() {
        return poemrobotid;
    }

    public void setPoemRobotId(int value) {
        this.poemrobotid = value;
    }
    
    public int getRobotLevel() {
        return robotlevel;
    }

    public void setRobotLevel(int value) {
        this.robotlevel = value;
    }

    public String getRobotName() {
        return robotname;
    }

    public void setRobotName(String value) {
        this.robotname = value;       	
    }
    
    public int getJobId() {
        return jobid;
    }

    public void setJobId(int value) {
        this.jobid = value;
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
        return robotskill;
    }

    public void setRobotSkill(String robotSkill) {
        this.robotskill = robotSkill;
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
    
    public int getBuddyId() {
        return buddyid;
    }

    public void setBuddyId(int value) {
        this.buddyid = value;
    }
    
    public int getBuddyLevel() {
        return buddylevel;
    }

    public void setBuddyLevel(int value) {
        this.buddylevel = value;
    }
    
    public int getBuddyStageLevel() {
        return buddystagelevel;
    }

    public void setBuddyStageLevel(int value) {
        this.buddystagelevel = value;
    }
    
    public int getRobotFightScore() {
        return robotfightscore;
    }

    public void setRobotFightScore(int value) {
        this.robotfightscore = value;
    }
    
    public int getDungeonId() {
        return dungeonid;
    }

    public void setDungeonId(int value) {
        this.dungeonid = value;
    }
}
