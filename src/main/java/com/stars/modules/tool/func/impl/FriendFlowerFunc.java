package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.services.ServiceHelper;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/17.
 */
public class FriendFlowerFunc extends ToolFunc {
    private int intimacy;   //亲密度
    private int count;      //鲜花数量

    public FriendFlowerFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        if (StringUtil.isEmpty(function) || "0".equals(function)) {
            return;
        }
        String[] args = function.split("\\|");
        if(args == null || args.length != 2) return;
        String[] array = args[1].split("\\+");
        count = Integer.parseInt(array[0]);
        intimacy = Integer.parseInt(array[1]);
    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
        ToolFuncResult tr = super.useCondition(moduleMap, args);
        if (tr == null) {
            FriendModule friendModule = (FriendModule) moduleMap.get(MConst.Friend);
            ServiceHelper.friendService().sendFriendList(friendModule.id());
            tr = new ToolFuncResult(false, null);
        }
        return tr;
    }

    @Override
    public Map<Integer,Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        return null;
    }

    public int getIntimacy() {
        return intimacy;
    }

    public int getCount() {
        return count;
    }
}
