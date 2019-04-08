package com.stars.modules.skill.summary;

import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillvupVo;
import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/10.
 */
public class SkillSummaryComponentImpl extends AbstractSummaryComponent implements SkillSummaryComponent {
    private Map<Integer, Integer> skillLevelMap;
    private Map<Integer, Integer> skillDamageMap;
    private Map<Byte, Integer> skillPositionMap;

    public SkillSummaryComponentImpl() {
    }

    public SkillSummaryComponentImpl(Map<Integer, Integer> skillLevelMap, Map<Integer, Integer> skillDamageMap,Map<Byte, Integer> skillPositionMap) {
        this.skillLevelMap = skillLevelMap;
        this.skillDamageMap = skillDamageMap;
        this.skillPositionMap = new HashMap<>(skillPositionMap);
    }

    @Override
    public String getName() {
        return "skill";
    }

    @Override
    public int getLatestVersion() {
        return 1;
    }

    @Override
    public void fromString(int version, String str) {
        try {
            switch (version) {
                case 1:
                    parseVer1(str);
                    break;
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    @Override
    public String makeString() {
        Map<String, String> map = new HashMap<>();
        com.stars.util.MapUtil.setString(map, "skillLevel", StringUtil.makeString2(skillLevelMap, '=', ','));
        com.stars.util.MapUtil.setString(map, "skillDamage", StringUtil.makeString2(skillDamageMap, '=', ','));
        com.stars.util.MapUtil.setString(map, "skillPosition", StringUtil.makeString2(skillPositionMap, '=', ','));
        return StringUtil.makeString2(map, '=', ',');
    }

    private void parseVer1(String str) throws Exception {
        Map<String, String> map = StringUtil.toMap(str, String.class, String.class, '=', ',');
        this.skillLevelMap = StringUtil.toMap(com.stars.util.MapUtil.getString(map, "skillLevel", ""), Integer.class, Integer.class, '=', ',');
        SkillvupVo svv;
        for (Map.Entry<Integer, Integer> entry : this.skillLevelMap.entrySet()) {
            svv = SkillManager.getSkillvupVo(entry.getKey(), entry.getValue());
            if (svv == null) {
                this.skillLevelMap.put(entry.getKey(), 1);
            }
        }
        this.skillDamageMap = StringUtil.toMap(com.stars.util.MapUtil.getString(map, "skillDamage", ""), Integer.class, Integer.class, '=', ',');
        this.skillPositionMap = StringUtil.toMap(MapUtil.getString(map, "skillPositionMap", ""), Byte.class, Integer.class, '=', ',');
    }

    @Override
    public Map<Integer, Integer> getSkillLevel() {
        return skillLevelMap;
    }

    @Override
    public Map<Integer, Integer> getSkillDamage() {
        return skillDamageMap;
    }

    @Override
    public Map<Byte, Integer> getSkillPositionMap() {  return skillPositionMap; }
}
