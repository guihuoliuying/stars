package com.stars.multiserver.daily5v5.data;

import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class MatchFloat {
	
	private int floatId;
	
	private byte type;
	
	private String scoreRange;
	
	private int[] range;
	
	private String timeFloat;
	
    private List<int[]> timeFloatList;

	public int getFloatId() {
		return floatId;
	}

	public void setFloatId(int floatId) {
		this.floatId = floatId;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public String getScoreRange() {
		return scoreRange;
	}

	public void setScoreRange(String scoreRange) throws Exception {
		this.scoreRange = scoreRange;
//		String[] arr = scoreRange.split("\\+");
		range = StringUtil.toArray(scoreRange, int[].class, '+');
	}

	public int[] getRange() {
		return range;
	}

	public void setRange(int[] range) {
		this.range = range;
	}

	public String getTimeFloat() {
		return timeFloat;
	}

	public void setTimeFloat(String timeFloat) throws Exception {
		this.timeFloat = timeFloat;
		String[] timeInfos = timeFloat.split(",");
		List<int[]> list = new ArrayList<>();
		for(String timeInfo : timeInfos){
//			String[] arr = timeInfo.split("\\+");
			list.add(StringUtil.toArray(timeInfo, int[].class, '+'));
		}
		timeFloatList = list;
	}

	public List<int[]> getTimeFloatList() {
		return timeFloatList;
	}

	public void setTimeFloatList(List<int[]> timeFloatList) {
		this.timeFloatList = timeFloatList;
	}

}
