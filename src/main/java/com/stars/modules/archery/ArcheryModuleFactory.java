package com.stars.modules.archery;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.data.DataManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huzhipeng
 * 2017-06-08
 */
public class ArcheryModuleFactory extends AbstractModuleFactory<ArcheryModule> {

	public ArcheryModuleFactory() {
		super(new ArcheryPacketSet());
	}
	
	@Override
	public ArcheryModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
		// TODO Auto-generated method stub
		return new ArcheryModule(id, self, eventDispatcher, map);
	}
	
	@Override
	public void loadProductData() throws Exception {
		String awardStr = DataManager.getCommConfig("game_arrow_award");
		Map<int[], Integer> integralAwardMap = new HashMap<>();
		String[] partAwardStrs = awardStr.split("\\|");
		int maxIntegral = 0;
		for(String partAwardStr : partAwardStrs){
			String[] awardInfo = partAwardStr.split(",");
			String[] integralArr = awardInfo[0].split("\\+");
			int[] arr = new int[]{Integer.parseInt(integralArr[0]), Integer.parseInt(integralArr[1])};
			int dropId = Integer.parseInt(awardInfo[1]);
			integralAwardMap.put(arr, dropId);
			if(arr[1]>maxIntegral){
				maxIntegral = arr[1];
			}
		}
		ArcheryManager.integralAwardMap = integralAwardMap;
		ArcheryManager.MaxIntegral = maxIntegral;
		ArcheryManager.TotalPlayNum = DataManager.getCommConfig("game_arrow_wholetimes", (byte)2);
	}

}
