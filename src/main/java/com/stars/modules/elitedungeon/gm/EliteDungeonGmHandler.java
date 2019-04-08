package com.stars.modules.elitedungeon.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.elitedungeon.EliteDungeonModule;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

public class EliteDungeonGmHandler implements GmHandler{

	@Override
	public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
		EliteDungeonModule module = (EliteDungeonModule)moduleMap.get(MConst.EliteDungeon);
		module.gmHandler(args);
	}

}
