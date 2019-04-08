package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.role.RoleModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;

import java.util.Map;

public class RoleExpToolFunc extends ToolFunc {
	
	private int exp = 0;
	
	public RoleExpToolFunc(ItemVo itemVo){
		super(itemVo);
	}

	@Override
	public void parseData(String function) {
		//格式类似：1|1000|text1，1000代表获取的经验数量，text1需要索引游戏文本（gametext）中key为text1的文字内容
		if (function == null || "0".equals(function.trim()) || function.equals("")) {
//            throw new IllegalArgumentException("配置为空");
			return;
        }
		String str[] = function.split("[|]");
		exp = Integer.parseInt(str[1]);
		parseNotice(str[2]);
	}

	@Override
	public ToolFuncResult check(Map<String, Module> moduleMap, int count,
                                Object... args) {
		RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
		
		ToolFuncResult tr = null;
		if (!roleModule.canAddExp(exp * count)) {
			tr = new ToolFuncResult(false, new ClientText("玩家经验已满"));
		}else {
			tr = super.useCondition(moduleMap, args);
		}
		if (tr == null) {
			tr =  new ToolFuncResult(true, null);
		}
		return tr;
	}

	@Override
	public Map<Integer,Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
		// TODO Auto-generated method stub
		RoleModule rm = (RoleModule)moduleMap.get(MConst.Role);
		rm.addExp(exp*count);
		rm.warn(getUseNotice());
		return null;
	}
}
