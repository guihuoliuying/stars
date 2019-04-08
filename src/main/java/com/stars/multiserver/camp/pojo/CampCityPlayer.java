package com.stars.multiserver.camp.pojo;

import com.stars.util.StringUtil;

import java.util.Map;

public class CampCityPlayer {
	
	private int cityId;
	
	private String playerInfo;
	
	private Map<Long, Integer> playerMap;
	
	private int totalNum;

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public String getPlayerInfo() {
		this.playerInfo = StringUtil.makeString(playerMap, '+', '|');
		return playerInfo;
	}

	public void setPlayerInfo(String playerInfo) {
		this.playerInfo = playerInfo;
		this.playerMap = StringUtil.toMap(playerInfo, Long.class, Integer.class, '+', '|');
	}

	public Map<Long, Integer> getPlayerMap() {
		return playerMap;
	}

	public void setPlayerMap(Map<Long, Integer> playerMap) {
		this.playerMap = playerMap;
	}

	public int getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}

}
