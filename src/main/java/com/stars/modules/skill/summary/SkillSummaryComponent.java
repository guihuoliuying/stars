package com.stars.modules.skill.summary;

import com.stars.services.summary.SummaryComponent;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/10.
 */
public interface SkillSummaryComponent extends SummaryComponent {
    public Map<Integer, Integer> getSkillLevel();

    public Map<Integer, Integer> getSkillDamage();

    public Map<Byte, Integer> getSkillPositionMap();
}
