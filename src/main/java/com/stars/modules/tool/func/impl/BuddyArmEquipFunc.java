package com.stars.modules.tool.func.impl;

import com.stars.core.attr.Attribute;
import com.stars.core.module.Module;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/11/22.
 */
public class BuddyArmEquipFunc extends ToolFunc {
    private Attribute attribute = new Attribute();

    public BuddyArmEquipFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        // 格式为:18|hp=100,attack=10
        if (StringUtil.isEmpty(function) || "0".equals(function.trim())) {
            return;
        }
        String[] temp = function.split("\\|");
        attribute.strToAttribute(temp[1]);
    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
        return null;
    }

    @Override
    public Map<Integer,Integer> use(Map<String, Module> moduleMap, int count, Object... args) {

        return null;
    }

    public Attribute getAttribute() {
        return attribute;
    }
}
