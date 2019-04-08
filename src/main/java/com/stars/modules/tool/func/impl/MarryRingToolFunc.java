package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.marry.MarryModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhoujin on 2017/5/12.
 */
public class MarryRingToolFunc extends ToolFunc {
    private int marryId = -1;

    public MarryRingToolFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        if (StringUtil.isEmpty(function)) return;
        String[] args = function.split("\\|");
        
        if (args.length >= 2) {
            marryId = Integer.parseInt(args[1]);
		}
        
        if (args.length >= 3) {
        	parseNotice(args[2]);
		}

    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
    	if (count <= 0) {
            return new ToolFuncResult(false, new ClientText("道具数量为零"));
        }
    	
    	ToolFuncResult tr = super.useCondition(moduleMap, args);
        if (tr == null) {
            tr = new ToolFuncResult(true, null);
        }
        if (canAutoUse(moduleMap,count) == 0) {
            tr.setSuccess(false);
            tr.setMessage(new ClientText("婚戒已激活"));
        }
            
        return tr;
    }

    @Override
    public int canAutoUse(Map<String, Module> moduleMap, int count) {
        if (count <= 0) return 0;
        MarryModule marryModule = (MarryModule) moduleMap.get(MConst.Marry);
        if (!marryModule.isCanActiveMarryRing(marryId)) {
			return 0;
		}else{
			return 1;
		}
    }

    @Override
    public Map<Integer,Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        if (count <= 0) return null;
        MarryModule marryModule = (MarryModule) moduleMap.get(MConst.Marry);
        marryModule.activeMarryRing(marryId);
        return null;
    }
}
