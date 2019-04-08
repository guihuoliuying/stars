package com.stars.multiserver.daily5v5;

import java.util.Map;

public class Daily5v5FightArgs {
	
	private long createTimestamp;
	
	private Map<String, Integer> fighterSeverIdMap;

	public long getCreateTimestamp() {
		return createTimestamp;
	}

	public void setCreateTimestamp(long createTimestamp) {
		this.createTimestamp = createTimestamp;
	}

	public Map<String, Integer> getFighterSeverIdMap() {
		return fighterSeverIdMap;
	}

	public void setFighterSeverIdMap(Map<String, Integer> fighterSeverIdMap) {
		this.fighterSeverIdMap = fighterSeverIdMap;
	}

}
