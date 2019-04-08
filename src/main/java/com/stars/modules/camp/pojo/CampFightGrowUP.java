package com.stars.modules.camp.pojo;

import com.stars.core.attr.Attribute;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.prodata.CampGrade;
import com.stars.modules.data.DataManager;
import com.stars.modules.offlinepvp.OfflinePvpManager;
import com.stars.modules.offlinepvp.prodata.OPRobotVo;
import com.stars.modules.scene.fightdata.FighterEntity;

import java.util.Map;
import java.util.Set;

/**
 * Created by huwenjun on 2017/7/25.
 */
public class CampFightGrowUP implements Cloneable, Comparable<CampFightGrowUP> {
    private String fightUid;
    private String name;
    private int level = 1;
    private int score;
    private int exp;
    private int vipLevel;
    private int commonOfficerId;
    private int rareOfficerId;
    private int designateOfficerId;
    private String serverName;
    private Set<Integer> skillIds;

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            throw new RuntimeException(exception);
        }
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }


    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public Set<Integer> getSkillIds() {
        return skillIds;
    }

    public void setSkillIds(Set<Integer> skillIds) {
        this.skillIds = skillIds;
    }

    public String getFightUid() {
        return fightUid;
    }

    public void setFightUid(String fightUid) {
        this.fightUid = fightUid;
    }

    public static CampFightGrowUP getNewInstance(FighterEntity fighterEntity) {
        OPRobotVo opRobotVo = OfflinePvpManager.robotVoMap.get(Integer.parseInt(fighterEntity.getUniqueId()));
        CampFightGrowUP campFightGrowUP = new CampFightGrowUP();
        campFightGrowUP.setVipLevel(0);
        campFightGrowUP.setLevel(1);
        campFightGrowUP.setFightUid(fighterEntity.getUniqueId());
        campFightGrowUP.setCommonOfficerId(1);
        campFightGrowUP.setRareOfficerId(0);
        campFightGrowUP.setName(fighterEntity.getName());
        campFightGrowUP.setServerName(DataManager.getGametext(opRobotVo.getServerName()));
        return campFightGrowUP;
    }

    public int getCommonOfficerId() {
        return commonOfficerId;
    }

    public void setCommonOfficerId(int commonOfficerId) {
        this.commonOfficerId = commonOfficerId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean addScore(int addscore) {
        this.score += addscore;
        this.exp += addscore;
        boolean canLevelUp = false;
        CampGrade campGradeByJobLevel = CampManager.getCampGradeByLevel(level);
        CampGrade nextLevelCampGrade = campGradeByJobLevel;
        while (nextLevelCampGrade != null && exp >= nextLevelCampGrade.getReqlevel()) {
            exp -= nextLevelCampGrade.getReqlevel();
            nextLevelCampGrade = nextLevelCampGrade.getNextLevelCampGrade();
            if (nextLevelCampGrade == null) {
                return canLevelUp;
            }
            level = nextLevelCampGrade.getLevel();
            canLevelUp = true;
        }
        return canLevelUp;
    }


    public int getRareOfficerId() {
        return rareOfficerId;
    }

    public void setRareOfficerId(int rareOfficerId) {
        this.rareOfficerId = rareOfficerId;
    }

    public Attribute getAttribute() {
        CampFightOfficerAttr campFightOfficerAttr = CampManager.getCampFightOfficerAttr(rareOfficerId);
        Attribute attribute = new Attribute();
        CampGrade campGrade = CampManager.getCampGradeByLevel(1);
        if (campFightOfficerAttr != null) {
            attribute.addAttribute(campGrade.getAttribute(), 1000 + campFightOfficerAttr.getScale(), 1000);
        } else {
            attribute.addAttribute(campGrade.getAttribute());
        }
        attribute.setMaxhp(attribute.getHp());
        return attribute;
    }

    public Map<Integer, Integer> getBuffs() {
        CampGrade campGrade = CampManager.getCampGradeByLevel(level);
        return campGrade.getBuffMap();
    }

    @Override
    public int compareTo(CampFightGrowUP o) {
        return o.getScore() - this.getScore();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDesignateOfficerId() {
        return designateOfficerId;
    }

    public void setDesignateOfficerId(int designateOfficerId) {
        this.designateOfficerId = designateOfficerId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void reset() {
        score = 0;
        level = 1;
        exp=0;
    }
}
