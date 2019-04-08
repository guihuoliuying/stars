package com.stars.modules.family.summary;

import com.stars.services.summary.SummaryComponent;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/20.
 */
public interface FamilySummaryComponent extends SummaryComponent {

    public long getFamilyId();

    public String getFamilyName();

    public byte getPostId();

    public Map<String, Integer> getSkillLevelMap();

}
