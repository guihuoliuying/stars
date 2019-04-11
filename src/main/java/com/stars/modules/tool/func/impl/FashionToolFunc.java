package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.StringUtil;

import java.util.Map;


/**
 * Created by gaopeidian on 2016/10/08.
 */
public class FashionToolFunc extends ToolFunc {
    private int fashionId = -1;
    private int addUseHour = 0;
    private int fashionItemId = 0;

    public FashionToolFunc(ItemVo itemVo) {
        super(itemVo);
        fashionItemId = itemVo.getItemId();
    }

    @Override
    public void parseData(String function) {
        /** 格式：15|fashionid+X，表示获得后增加某个id的时装X天的使用时间。*/
        if (StringUtil.isEmpty(function)) return;
        String[] args = function.split("\\|");

        if (args.length == 2) {
            String[] args2 = args[1].split("\\+");
            if (args2.length != 2) return;
            fashionId = Integer.parseInt(args2[0]);
            addUseHour = Integer.parseInt(args2[1])*24; //因为一开始做成了小时
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
    public int canAutoUse(Map<String, Module> moduleMap, int count) {
        if (count <= 0) return 0;
        return count;
        //        FashionModule fashionModule = (FashionModule) moduleMap.get(MConst.Fashion);
        //        if (fashionModule.getIsActive(fashionId)&&!FashionManager.isTimeLimitedFashion(fashionId)) {
        //            return 0;
        //        }else{
        //            return 1;
        //        }
    }

    @Override
    public Map<Integer, Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        if (count <= 0) return null;
        return null;
    }

    public int getAddUseHour() {
        return addUseHour;
    }
    public int getFashionId() {
        return fashionId;
    }

}

