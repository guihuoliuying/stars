package com.stars.modules.familyEscort.gm;

import com.stars.core.module.Module;
import com.stars.modules.gm.GmHandler;
import com.stars.services.ServiceHelper;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/4/19.
 */
public class FamilyEscortGmHandler implements GmHandler {

    @Override
	public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
		switch (args[0]) {
		case "prepare":
			ServiceHelper.familyEscortService().prepare();
			break;
		case "start":
			ServiceHelper.familyEscortService().start();
			break;
		case "end":
			ServiceHelper.familyEscortService().escortEnd();
			break;
		case "clearup":
			ServiceHelper.familyEscortService().clearup();
			break;
		case "reset":
			ServiceHelper.familyEscortService().dailyReset();
			break;

		/* 测试接口 */
		// case "testfight": //
		// Summary summary =
		// ServiceHelper.summaryService().getSummary(4194927L);
		// FighterEntity attacker = FighterCreator.createSelf(moduleMap);
		// attacker.setFighterType(FighterEntity.TYPE_PLAYER);
		// FighterEntity defender = null;
		// for (FighterEntity entity : FighterCreator.createBySummary((byte) 1,
		// summary).values()) {
		// if (!entity.getUniqueId().startsWith("b")) {
		// defender = entity;
		// }
		// }
		// defender.setFighterType(FighterEntity.TYPE_ROBOT);
		//
		// ServiceHelper.familyEscortService().createFight(
		// attacker, defender, 0L, 0L);
		// break;
		}
	}

}
