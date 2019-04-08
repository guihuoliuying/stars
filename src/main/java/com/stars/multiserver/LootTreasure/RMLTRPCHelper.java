package com.stars.multiserver.LootTreasure;

import com.stars.multiserver.fightManager.RMFSManagerService;
import com.stars.services.fightbase.FightBaseService;
import com.stars.services.loottreasure.LootTreasureService;

public class RMLTRPCHelper {
	static FightBaseService fightBaseService;
	static LootTreasureService lootTreasureService;
	static RMFSManagerService rmfsManagerService;
	public static FightBaseService fightBaseService() {
        return fightBaseService;
    }
	public static LootTreasureService lootTreasureService(){
		return lootTreasureService;
	}
	
	public static RMFSManagerService rmfsManagerService(){
		return rmfsManagerService;
	}
}
