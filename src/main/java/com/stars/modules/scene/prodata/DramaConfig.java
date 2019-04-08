package com.stars.modules.scene.prodata;

import com.stars.modules.skill.prodata.SkillVo;

import java.util.HashMap;
import java.util.Map;

/**
 * 解析后剧情内存数据
 * Created by liuyuheng on 2016/7/28.
 */
public class DramaConfig {
    private String dramaId;
    private Map<Integer, MonsterVo> monsterVoMap = new HashMap<>();
    private Map<Integer, SkillVo> skillVoMap = new HashMap<>();

    public DramaConfig(String dramaId) {
        this.dramaId = dramaId;
    }

    public String getDramaId() {
        return dramaId;
    }

    public Map<Integer, MonsterVo> getMonsterVoMap() {
        return monsterVoMap;
    }

    public void setMonsterVoMap(Map<Integer, MonsterVo> monsterVoMap) {
        this.monsterVoMap = monsterVoMap;
    }

    public Map<Integer, SkillVo> getSkillVoMap() {
        return skillVoMap;
    }

    public void setSkillVoMap(Map<Integer, SkillVo> skillVoMap) {
        this.skillVoMap = skillVoMap;
    }
}
