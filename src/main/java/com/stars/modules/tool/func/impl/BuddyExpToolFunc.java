package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/9.
 */
public class BuddyExpToolFunc extends ToolFunc {
    public BuddyExpToolFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {

    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
        ToolFuncResult tr = super.useCondition(moduleMap, args);
        BuddyModule buddyModule = (BuddyModule) moduleMap.get(MConst.Buddy);
        if (buddyModule.getFightBuddyId() == 0) {
            tr = new ToolFuncResult(false, new ClientText("没有出战伙伴"));
        }
        if (tr == null) {
            tr = new ToolFuncResult(true, null);
        }
        return tr;
    }

    @Override
    public Map<Integer,Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        /**
         * 增加伙伴经验入口
         * 1.战斗中增加,无需传入伙伴Id,取当前出战伙伴Id
         * */
        BuddyModule buddyModule = (BuddyModule) moduleMap.get(MConst.Buddy);
        int buddyId = args.length == 0 ? buddyModule.getFightBuddyId() : (int) args[0];
        if (buddyId == 0) {// 增加目标伙伴Id为0
            return null;
        }
        buddyModule.addExp(buddyId, count);
        return null;
    }
}
