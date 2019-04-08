package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.modules.trump.TrumpModule;
import com.stars.modules.trump.userdata.RoleTrumpRow;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhouyaohui on 2016/9/18.
 */
public class TrumpFunc extends ToolFunc {

    private int trumpId;

    public TrumpFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        /** 格式：13|trumpId|notice */
        if (StringUtil.isEmpty(function)) return;
        String[] args = function.split("\\|");
        trumpId = Integer.parseInt(args[1]);
        parseNotice(args[2]);
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
    public int canAutoUse(Map<String, Module> moduleMap, int count) {
        if (count <= 0) return 0;
        TrumpModule trumpModule = (TrumpModule) moduleMap.get(MConst.Trump);
        RoleTrumpRow row = trumpModule.getRoleTrumpRowById(trumpId);
        if (row == null) return 1;
        return 0;
    }

    @Override
    public Map<Integer,Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        if (count <= 0) return null;
        TrumpModule trumpModule = (TrumpModule) moduleMap.get(MConst.Trump);
        RoleTrumpRow row = trumpModule.getRoleTrumpRowById(trumpId);
        if (trumpId == 0) {
            LogUtil.error("roleId:{}|法宝id配置错误", trumpModule.id());
            return null;
        }
        if (row == null) {  // 解封
            trumpModule.unblock(trumpId);
        } else {    // 分解
            trumpModule.resolve(trumpId, count);
        }
        return null;
    }
}
