package com.stars.modules.familyactivities.treasure;

import com.stars.modules.familyactivities.treasure.prodata.FTBuffVo;
import com.stars.modules.familyactivities.treasure.prodata.FamilyAdvawardVo;
import com.stars.modules.familyactivities.treasure.prodata.FamilyAdvendVo;
import com.stars.modules.familyactivities.treasure.prodata.FamilyAdventureVo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenkeyu on 2017/2/10 11:47
 */
public class FamilyTreasureManager {
    public static Map<Integer, Map<Integer, FamilyAdventureVo>> familyAdventureVoMap = new HashMap<>();//level,--step,vo

    public static Map<String, FamilyAdvendVo> familyAdvendVoMap = new HashMap<>();//endcondition,--Vo

    public static Map<Integer, Map<FamilyAdvawardVo, Integer>> familyAdvawardVoMap = new HashMap<>();//group,--Vo,dropId

    public static Map<Integer, FTBuffVo> buffVoMap = new HashMap<>();//day,--Vo

    public static int familyadventure_count;
    public static int familyadventure_sundaycount;

    public static Map<Integer, Map<Integer, FamilyAdventureVo>> getFamilyAdventureVoMap() {
        return familyAdventureVoMap;
    }

    public static int getMaxLevel() {
        Set<Integer> levels = familyAdventureVoMap.keySet();
        return Collections.max(levels);
    }

    public static int getMinLevel() {
        Set<Integer> levels = familyAdventureVoMap.keySet();
        return Collections.min(levels);
    }

    public static int getMaxStepByLevel(int level) {
        Set<Integer> steps = familyAdventureVoMap.get(level).keySet();
        return Collections.max(steps);
    }

    public static int getMinStepByLevel(int level) {
        Set<Integer> steps = familyAdventureVoMap.get(level).keySet();
        return Collections.min(steps);
    }

    public static int getDropByGroupAndDamage(int group, long damage) {
        for (Map.Entry<FamilyAdvawardVo, Integer> entry : familyAdvawardVoMap.get(group).entrySet()) {
            if (damage >= entry.getKey().getMinDamage() && damage <= entry.getKey().getMaxDamage()) {
                return entry.getValue();
            }
        }
        return -1;
    }
}
