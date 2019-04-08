package com.stars.modules.daily5v5.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.daily5v5.Daily5v5Module;
import com.stars.modules.gm.GmHandler;
import com.stars.multiserver.daily5v5.Daily5v5Manager;

import java.util.Map;

public class Daily5v5GmHandler implements GmHandler{

	@Override
	public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
		Daily5v5Manager.TEAM_MEMBER_NUM = Byte.parseByte(args[0]);
		Daily5v5Module module = (Daily5v5Module)moduleMap.get(MConst.Daily5v5);
		module.gmHandler(args);
	}

}
