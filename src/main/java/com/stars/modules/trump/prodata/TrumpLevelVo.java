package com.stars.modules.trump.prodata;

import com.stars.core.attr.Attribute;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouyaohui on 2016/9/23.
 */
public class TrumpLevelVo {

    private int trumpId;            // 法宝id
    private short level;              // 法宝等级
    private byte stage;              // 法宝阶位
    private byte displayLevel;      // 客户端显示等级
    private String display;         // 法宝外观
    private String smallScale;      // 佩带界面,模型缩放千分比与旋转角度
    private String mediumScale;     // 炼化界面,模型缩放千分比与旋转角度
    private String largeScale;      // 法宝界面,模型缩放千分比与旋转角度
    private String skill;           // 技能
    private int fightScore;         // 战力值
    private String material;        // 由前一级升级到当前等级需要的材料
    private int triggerRate;
    private String attr;            // 属性
    private Attribute attribute;

    private Map<Integer, Integer> materialMap = new HashMap<>();
    private Map<Integer, Integer> skillMap = new HashMap<>();

    public Attribute getAttribute() {
        return attribute;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
        this.attribute = new Attribute(attr);
    }

    public byte getDisplayLevel() {
        return displayLevel;
    }

    public void setDisplayLevel(byte displayLevel) {
        this.displayLevel = displayLevel;
    }

    public int getTriggerRate() {
        return triggerRate;
    }

    public void setTriggerRate(int triggerRate) {
        this.triggerRate = triggerRate;
    }

    public int getTrumpId() {
        return trumpId;
    }

    public void setTrumpId(int trumpId) {
        this.trumpId = trumpId;
    }

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public byte getStage() {
        return stage;
    }

    public void setStage(byte stage) {
        this.stage = stage;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getSmallScale() {
        return smallScale;
    }

    public void setSmallScale(String smallScale) {
        this.smallScale = smallScale;
    }

    public String getMediumScale() {
        return mediumScale;
    }

    public void setMediumScale(String mediumScale) {
        this.mediumScale = mediumScale;
    }

    public String getLargeScale() {
        return largeScale;
    }

    public void setLargeScale(String largeScale) {
        this.largeScale = largeScale;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
        String[] arr = skill.trim().split("\\|");
        for (String s : arr) {
            String[] ss = s.split("\\+");
            if (ss.length != 2) {
                continue;
            }
            skillMap.put(Integer.valueOf(ss[0]), Integer.valueOf(ss[1]));
        }
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
        String[] arr = material.trim().split("\\|");
        for (String m : arr) {
            String[] item = m.split("\\+");
            materialMap.put(Integer.parseInt(item[0]), Integer.parseInt(item[1]));
        }
    }

    /** 便利接口 */

    /**
     * 升级材料,存在并发，拷贝副本
     * @return
     */
    public Map<Integer, Integer> getMaterialMap() {
        Map<Integer, Integer> map = new HashMap<>();
        map.putAll(materialMap);
        return map;
    }

    /**
     * 技能列表
     * @return
     */
    public Map<Integer, Integer> getSkillMap() {
        Map<Integer, Integer> map = new HashMap<>();
        map.putAll(skillMap);
        return map;
    }
}
