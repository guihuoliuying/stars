package com.stars.modules.opactkickback.userdata;

import com.stars.modules.data.DataManager;

import java.util.HashMap;
import java.util.Map;

public class ConsumGateDefineCatalog {
	public static final ConsumGateDefineCatalog instance = new ConsumGateDefineCatalog();

	private Map<Integer, ConsumeGateDefine> gateMap = new HashMap<Integer, ConsumeGateDefine>();

	public void reload() {
		String rewardConf = DataManager.getCommConfig("moneyback_reward");
		String[] ss = rewardConf.split("\\,");
		Map<Integer, ConsumeGateDefine> temp = new HashMap<Integer, ConsumeGateDefine>();
		int way = 0;
		for (String reward : ss) {
			ConsumeGateDefine define = ConsumeGateDefine.parse(way, reward);
			temp.put(define.getId(), define);
			way++;
		}
		this.gateMap = temp;

	}

	public ConsumeGateDefine[] getGateDefines() {
		return gateMap.values().toArray(new ConsumeGateDefine[gateMap.values().size()]);
	}

}
