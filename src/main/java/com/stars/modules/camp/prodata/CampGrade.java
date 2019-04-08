package com.stars.modules.camp.prodata;

import com.stars.core.attr.Attribute;
import com.stars.modules.camp.CampManager;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/7/25.
 */
public class CampGrade implements Comparable<CampGrade> {
    private int id;
    private int level;// '等级',
    private int reqlevel;// '升级所需经验',
    private String attr;// '附加属性',
    private int killgrade;// '击杀获得积分',
    private Attribute attribute;
    private String buffId;
    private Map<Integer, Integer> buffMap = new HashMap<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getReqlevel() {
        return reqlevel;
    }

    public void setReqlevel(int reqlevel) {
        this.reqlevel = reqlevel;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
        attribute = new Attribute(this.attr);
    }

    public String getBuffId() {
        return buffId;
    }

    public void setBuffId(String buffId) {
        this.buffId = buffId;
        try {
            buffMap = StringUtil.toMap(buffId, Integer.class, Integer.class, '+', '|');
        } catch (Exception e) {
            throw new RuntimeException("策划数据配错了", e);
        }
    }

    public int getKillgrade() {
        return killgrade;
    }

    public void setKillgrade(int killgrade) {
        this.killgrade = killgrade;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public CampGrade getNextLevelCampGrade() {
        return CampManager.getCampGradeByLevel(level + 1);
    }

    public Map<Integer, Integer> getBuffMap() {
        return buffMap;
    }

    @Override
    public int compareTo(CampGrade o) {
        return this.getLevel() - o.getLevel();
    }
}
