package com.stars.modules.tool.func.impl;

import com.stars.core.event.Event;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.event.UseUnlockEquipToolEvent;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;

import java.util.Map;

public class UnlockEquipFunc extends ToolFunc {
	
	public UnlockEquipFunc(ItemVo itemVo) {
		super(itemVo);
	}

	@Override
	public void parseData(String function) {
		// TODO Auto-generated method stub

	}

	@Override
	public ToolFuncResult check(Map<String, Module> moduleMap, int count,
                                Object... args) {
		ToolFuncResult tr = super.useCondition(moduleMap, args);
    	if (tr == null) {
			tr = new ToolFuncResult(true, null);
		}
        return tr;
	}

	@Override
	public Map<Integer,Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
		// TODO Auto-generated method stub
		Event e = new UseUnlockEquipToolEvent(itemVo().getItemId());
		ToolModule tm = (ToolModule)moduleMap.get(MConst.Tool);
		tm.dispathEvent(e);
		return null;
	}

}
