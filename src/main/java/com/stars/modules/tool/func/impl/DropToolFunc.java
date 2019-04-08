package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.drop.DropModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.tool.productdata.ItemVo;

import java.util.Map;

public class DropToolFunc extends ToolFunc {

	private int dropGroup;
	private byte showType = 0;

	public DropToolFunc(ItemVo itemVo){
		super(itemVo);
	}

	@Override
	public void parseData(String function) {
		// TODO Auto-generated method stub
		if (function == null || function.equals("") || function.equals("0")) {
			return;
		}
		String[] sts = function.split("\\|");
		dropGroup = Integer.parseInt(sts[1]);
		parseNotice(sts[2]);
		if(sts.length >= 4){
			showType = Byte.parseByte(sts[3]); //0默认为提示，1为不提示 2为弹窗展示奖励
		}
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
		DropModule dm = (DropModule)moduleMap.get(MConst.Drop);
		Map<Integer, Integer> drops = dm.executeDrop(dropGroup, count,true);
		ToolModule tm = (ToolModule)moduleMap.get(MConst.Tool);
		Map<Integer,Integer> map = tm.addAndSend(drops, EventType.USETOOL.getCode());
		if(showType == 0) {            	//默认飘字提示奖励
			tm.sendPacket(new ClientAward(map));
		}else if(showType == 1){		//1为不提示

		}else if(showType == 2){		//2为弹窗展示奖励
			ClientAward clientAward = new ClientAward(map);
			clientAward.setType((byte)1);
			tm.sendPacket(clientAward);
		}
		return map;
	}

}
