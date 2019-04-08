package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class OptionalBoxFunc extends ToolFunc {
    private int group;

    public OptionalBoxFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        try {
            int[] params = StringUtil.toArray(function, int[].class, '|');
            group = params[1];
        } catch (Exception e) {
            LogUtil.error("可选宝箱解析数据错误", e);
        }

    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
        return new ToolFuncResult(true, null);
    }

    @Override
    public Map<Integer, Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        return null;
    }

    public int getGroup() {
        return group;
    }
}
