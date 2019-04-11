package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/24.
 */
public class BuddyExpBoxFunc extends ToolFunc {
    private int addNum;

    public BuddyExpBoxFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        //格式为8|数量|提示文本索引
        if (function == null || "".equals(function.trim())) {
            return;
        }
        String[] args = function.split("\\|");
        addNum = Integer.parseInt(args[1]);
        parseNotice(args[2]);
    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
        ToolFuncResult tr;
        int buddyId;
        if (args.length > 1) {
            Object[] temp = (Object[]) args[1];
            buddyId = Integer.parseInt((String) temp[0]);

        } else {
            buddyId = Integer.parseInt((String) args[0]);
        }

        return null;
    }

    @Override
    public Map<Integer, Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        /**
         * 增加伙伴经验入口
         * 2.使用道具增加,需要传入伙伴Id
         */
        int buddyId = Integer.parseInt((String) args[0]);
        return null;
    }
}
