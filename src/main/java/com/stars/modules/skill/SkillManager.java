package com.stars.modules.skill;

import com.stars.modules.skill.prodata.SkillPosition;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.modules.skill.prodata.SkillvupVo;

import java.util.Map;
import java.util.Set;

public class SkillManager {
	private static Map<String, SkillvupVo> skillVUPMap;
	
	private static Map<Integer, Integer> maxSkillLevel;

	private static Map<Integer, Set<Integer>> canLvUpSkill;//Map<jobId,Set<SkillId>>
	
	private static Map<Integer, SkillVo> skillVoMap = null;
	/**
	 * 《skillid,position》
	 */
	public static Map<Integer, Integer> skillPostionMap;
	/**
	 * 《jobid,《position,SkillPosition》》
	 */
	public static Map<Integer, Map<Integer, SkillPosition>> jobSkillPostionMap;

	public static Map<String, SkillvupVo> getSkillVUPMap() {
		return skillVUPMap;
	}

	public static void setSkillVUPMap(Map<String, SkillvupVo> skillVUPMap) {
		SkillManager.skillVUPMap = skillVUPMap;
	}
	
	public static SkillvupVo getSkillvupVo(int skillId,int level){
		return SkillManager.skillVUPMap.get(skillId+"_"+level);
	}
	
	public static int getMaxSkillLevel(int skillId){
		return SkillManager.maxSkillLevel.get(skillId);
	}

	public static Map<Integer, Integer> getMaxSkillLevel() {
		return maxSkillLevel;
	}

	public static void setMaxSkillLevel(Map<Integer, Integer> _maxSkillLevel) {
		SkillManager.maxSkillLevel = _maxSkillLevel;
	}
	
    public static Map<Integer, SkillVo> getSkillVoMap() {
        if (skillVoMap == null)
            throw new NullPointerException("skillVoMap");
        return skillVoMap;
    }

    public static void setSkillVoMap(Map<Integer, SkillVo> skillVoMap) {
        SkillManager.skillVoMap = skillVoMap;
    }
    
    public static SkillVo getSkillVo(int skillId) {
        return getSkillVoMap().get(skillId);
    }

	public static Map<Integer, Set<Integer>> getCanLvUpSkill() {
		return canLvUpSkill;
	}

	public static void setCanLvUpSkill(Map<Integer, Set<Integer>> canLvUpSkill) {
		SkillManager.canLvUpSkill = canLvUpSkill;
	}
}
