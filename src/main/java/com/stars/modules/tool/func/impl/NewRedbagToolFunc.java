package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.newredbag.NewRedbagModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;

import java.util.Map;

/**
 * Created by zhouyaohui on 2017/2/14.
 */
public class NewRedbagToolFunc extends ToolFunc {

    private int redbagId;

    public NewRedbagToolFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        // 22|redid
        String[] func = function.split("[|]");
        redbagId = Integer.valueOf(func[1]);
    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
        return null;
    }

    @Override
    public Map<Integer, Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        NewRedbagModule module = (NewRedbagModule) moduleMap.get(MConst.NewRedbag);
        module.add(redbagId, count);
        return null;
    }
}
