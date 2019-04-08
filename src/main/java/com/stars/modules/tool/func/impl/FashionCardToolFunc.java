package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.fashioncard.FashionCardModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-10-16.
 */
public class FashionCardToolFunc extends ToolFunc {

    private int fashionCardId;
    private int addUseHour;
    private int fashionItemId;

    public FashionCardToolFunc(ItemVo itemVo) {
        super(itemVo);
        fashionItemId = itemVo.getItemId();
    }

    @Override
    public void parseData(String function) {
        if (StringUtil.isEmpty(function)) return;
        String[] args = function.split("\\|");
        if (args.length == 2) {
            String[] args2 = args[1].split("\\+");
            if (args2.length != 2) return;
            fashionCardId = Integer.parseInt(args2[0]);
            addUseHour = Integer.parseInt(args2[1]) * 24;
        }
    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
        if (count <= 0) {
            return new ToolFuncResult(false, new ClientText("道具数量为零"));
        }
        ToolFuncResult tr = super.useCondition(moduleMap, args);
        if (tr == null) {
            tr = new ToolFuncResult(true, null);
        }
        return tr;
    }

    @Override
    public Map<Integer, Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        if (count <= 0) return null;
        FashionCardModule cardModule = (FashionCardModule) moduleMap.get(MConst.FashionCard);
        int real = addUseHour * count;
        cardModule.activeFashionCard(fashionCardId, real, fashionItemId);
        return null;
    }
}
