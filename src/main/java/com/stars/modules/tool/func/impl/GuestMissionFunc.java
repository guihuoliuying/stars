package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.guest.GuestModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.I18n;

import java.util.Map;

/**
 * Created by zhouyaohui on 2017/1/18.
 */
public class GuestMissionFunc extends ToolFunc {

    private int missionId;

    public GuestMissionFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        String[] funcs = function.split("[|]");
        missionId = Integer.valueOf(funcs[1]);
    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
        if (count != 1) {
            return new ToolFuncResult(false, new ClientText(I18n.get("guest.tool.error")));
        }
        GuestModule guestModule = (GuestModule) moduleMap.get(MConst.Guest);
        boolean b = guestModule.canUseTool(missionId);
        return new ToolFuncResult(b, null);
    }

    @Override
    public Map<Integer,Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        GuestModule guestModule = (GuestModule) moduleMap.get(MConst.Guest);
        for (int i = 0; i < count; i++) {
            guestModule.addMissionByTool(missionId);
        }
        return null;
    }
}
