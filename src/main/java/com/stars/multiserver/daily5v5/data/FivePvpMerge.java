package com.stars.multiserver.daily5v5.data;

import com.stars.core.attr.Attribute;

import java.util.LinkedHashMap;

public class FivePvpMerge {
	
	private int jobid;
	
	private String attr;
	
	private String skill;
	
	private Attribute attribute;
	
	private LinkedHashMap<Integer, Integer> skillMap;

	public int getJobid() {
		return jobid;
	}

	public void setJobid(int jobid) {
		this.jobid = jobid;
	}

	public String getAttr() {
		return attr;
	}

	public void setAttr(String attr) {
		this.attr = attr;
		this.attribute = new Attribute(attr);
	}

	public String getSkill() {
		return skill;
	}

	public void setSkill(String skill) {
		this.skill = skill;
		LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>();
		String[] infos = skill.split(",");
		for(String info : infos){
			String[] arr = info.split("\\+");
			map.put(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
		}
		skillMap = map;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public LinkedHashMap<Integer, Integer> getSkillMap() {
		return skillMap;
	}

	public void setSkillMap(LinkedHashMap<Integer, Integer> skillMap) {
		this.skillMap = skillMap;
	}
}
