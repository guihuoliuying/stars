package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.MapUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017/1/3 11:05
 */
public class LevelToolFunc extends ToolFunc {
    private Map<String, Map<Integer, Integer>> lvTools;
    private Map<Integer, Integer> tools;
    private byte showType = 0;

    public LevelToolFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        //格式为20|等级上限1,itemid+数量,itemid+数量=等级上限2,itemid+数量, itemid+数量=等级上限3,itemid+数量, itemid+数量|使用成功文本索引+使用失败文本索引
        if (function == null || "".equals(function.trim())) {
            return;
        }
        String[] args = function.split("\\|");
        String[] lvLimitStrs = args[1].split("=");
        lvTools = new HashMap<>();
        int preLevel = 0;
        for (String str : lvLimitStrs) {
            String[] lvItemCount = str.split(",");
            if (lvItemCount.length <= 1) {
                throw new IllegalArgumentException("数值配置有误->" + lvItemCount);
            }
            tools = new HashMap<>();
            for (String licStr : lvItemCount) {
                String[] tmpStr = licStr.split("\\+");
                if (tmpStr.length == 1) {
                    continue;
                }
                if (tmpStr.length == 2) {
                    tools.put(Integer.parseInt(tmpStr[0]), Integer.parseInt(tmpStr[1]));
                } else {
                    throw new IllegalArgumentException("数值配置有误->" + tmpStr);
                }
            }
            lvTools.put(preLevel + "+" + Integer.parseInt(lvItemCount[0]), tools);
            preLevel = Integer.parseInt(lvItemCount[0]);
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

    /**
     * 等级段在(0,等级上限1]的等级区间内, 获得对应的物品奖励; (等级上限1, 等级上限2]的,获得对应的物品奖励, (等级上限2,等级上限3]以及(等级上限3,无穷), 获得对应的物品奖励
     *
     * @param moduleMap 模块映射表
     * @param count     使用数量
     * @param args      其他参数 fixme: 如果道具有状态的话，估计增加RoleToolRow
     */
    @Override
    public Map<Integer,Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        Map<Integer, Integer> itemMap = getItemMap(roleModule.getLevel());
        if (itemMap != null) {
            Map<Integer, Integer> tempMap = new HashMap<>();
            tempMap.putAll(itemMap);
            MapUtil.multiply(tempMap, count);
            Map<Integer, Integer> result = toolModule.addAndSend(tempMap, EventType.USETOOL.getCode());
            if(showType == 0) {            	//默认飘字提示奖励
                ClientAward clientAward = new ClientAward(result);
                toolModule.sendPacket(clientAward);
            }else if(showType == 1){		//1为不提示

            }else if(showType == 2){		//2为弹窗展示奖励
                ClientAward clientAward = new ClientAward(result);
                clientAward.setType((byte) 1);
                toolModule.sendPacket(clientAward);
            }
            return result;
        }else{
            throw new IllegalArgumentException("数值配置有误");
        }
    }

    private Map<Integer, Integer> getItemMap(int roleLevel) {
        for (String levelBlock : lvTools.keySet()) {
            String[] args = levelBlock.split("\\+");
            if (roleLevel > Integer.parseInt(args[0]) && roleLevel <= Integer.parseInt(args[1])) {
                return lvTools.get(levelBlock);
            }
        }
        return null;
    }
}
