package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.baby.BabyModule;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by huwenjun on 2017/5/27.
 */
public class BabyFashionFunc extends ToolFunc {
    private Integer fashionId;
    private Map<Integer, Integer> itemDic;

    public BabyFashionFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        String[] args = function.split("\\|");
        fashionId = Integer.parseInt(args[1]);
        itemDic = StringUtil.toMap(args[2], Integer.class, Integer.class, '+', ',');
    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
        if (count <= 0) {
            return new ToolFuncResult(false, new ClientText("道具数量为零"));
        }
        return new ToolFuncResult(true, null);
    }

    @Override
    public Map<Integer, Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        BabyModule babyModule = (BabyModule) moduleMap.get(MConst.Baby);
        ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
        boolean actived = babyModule.isActivedFashion(fashionId);
        if (actived) {
            toolModule.addAndSend(itemDic, EventType.BABY_FASHION_REPEAT_RESOLVE.getCode());
        } else {
            babyModule.activeFashion(fashionId);
        }
        return null;
    }
}
