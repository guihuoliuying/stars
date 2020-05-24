package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.MapUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/5/16.
 */
public class BoxToolFunc extends ToolFunc {

    private Map<Integer, Integer> tools;
    private byte showType = 0;

    public BoxToolFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        //格式为2|物品id+数量，物品id+数量|提示文本索引
        if (function == null || "".equals(function.trim())) {
            return;
        }
        tools = new HashMap<Integer, Integer>();
        String[] args = function.split("\\|");
        String[] toolStr = args[1].split(",");
        for (String s:toolStr) {
            String[] tp = s.split("[+]");
            tools.put(Integer.parseInt(tp[0]), Integer.parseInt(tp[1]));
        }
        parseNotice(args.length > 2 ? args[2] : null);
        if(args.length >= 4){
            showType = Byte.parseByte(args[3]); //0默认为提示，1为不提示 2为弹窗展示奖励
        }
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
    public Map<Integer,Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
        Map<Integer, Integer> tempMap = new HashMap<>(tools);
        MapUtil.multiply(tempMap, count);
        Map<Integer,Integer> map = toolModule.addAndSend(tempMap);
        ClientAward clientAward = new ClientAward(removeAutoUseTool(map));
        if(showType == 0) {            	//默认飘字提示奖励
            toolModule.sendPacket(clientAward);
        }else if(showType == 1){		//1为不提示

        }else if(showType == 2){		//2为弹窗展示奖励
            clientAward.setType((byte) 1);
            toolModule.sendPacket(clientAward);
        }
        toolModule.warn(getUseNotice());
        return map;
    }

    public Map<Integer, Integer> getTools() {
        return tools;
    }

}
