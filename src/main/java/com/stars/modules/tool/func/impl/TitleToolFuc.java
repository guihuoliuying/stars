package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.title.TitleModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/7/25.
 */
public class TitleToolFuc extends ToolFunc {
    private int titleId;

    public TitleToolFuc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        // 格式:5|titleId
        if (StringUtil.isEmpty(function) || "0".equals(function)) {
            return;
        }
        String[] args = function.split("\\|");
        this.titleId = Integer.parseInt(args[1].trim());
//        parseNotice(args[2]);
    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
    	ToolFuncResult tr = super.useCondition(moduleMap, args);
    	if (tr == null) {
			tr = new ToolFuncResult(true, null);
		}
        return tr;
    }

    @Override
    public Map<Integer,Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        TitleModule titleModule = (TitleModule) moduleMap.get(MConst.Title);
        titleModule.activeTitle(titleId,count);
        return null;
    }
}
