package com.stars.modules.truename.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.truename.TrueNameModule;

import java.util.Map;

public class TrueNameGmHandler implements GmHandler{

	@Override
	public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
		TrueNameModule module = (TrueNameModule)moduleMap.get(MConst.TrueName);
		module.gmHandle(args);
	}

}
