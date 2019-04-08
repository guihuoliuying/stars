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
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/10.
 */
public class JobBoxToolFunc extends ToolFunc {
    private Map<Integer,Map<Integer, Integer>> jobToolMap;
    private byte showType = 0;
    public JobBoxToolFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        if (StringUtil.isEmpty(function) || "0".equals(function)) {
            return;
        }
        String[] args = function.split("\\|");
        if(args == null || (args.length != 2 && args.length != 3)) return;
        if(args.length >= 3){
            showType = Byte.parseByte(args[2]); //0默认为提示，1为不提示 2为弹窗展示奖励
        }
        String[] array = args[1].split(",");
        jobToolMap = new HashMap<>();
        String[] arr;
        Integer job,itemId,count;
        Map<Integer, Integer> map;
        for(String strData:array){
            arr = strData.split("\\+");
            if(arr == null || arr.length != 3) continue;;

            job = Integer.parseInt(arr[0]);
            itemId = Integer.parseInt(arr[1]);
            count = Integer.parseInt(arr[2]);

            map = jobToolMap.get(job);
            if(map == null){
                map = new HashMap<>();
                jobToolMap.put(job,map);
            }

            map.put(itemId,count);
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
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);

        int jobId = roleModule.getRoleRow().getJobId();
        Map<Integer, Integer> toolMap = jobToolMap.get(jobId);
        Map<Integer, Integer> resultMap = null;
        if(StringUtil.isNotEmpty(toolMap)) {
            Map<Integer, Integer> map = new HashMap<>(toolMap);
            for (Integer itemId : toolMap.keySet()) {
                map.put(itemId, map.get(itemId) * count);
            }
            resultMap = toolModule.addAndSend(map, EventType.USETOOL.getCode());

            if(showType == 0) {            	//默认飘字提示奖励
                toolModule.sendPacket(new ClientAward(removeAutoUseTool(resultMap)));
            }else if(showType == 1){		//1为不提示

            }else if(showType == 2){		//2为弹窗展示奖励
                ClientAward clientAward = new ClientAward(removeAutoUseTool(resultMap));
                clientAward.setType((byte)1);
                toolModule.sendPacket(clientAward);
            }
        }
        return resultMap;
    }

    public Map<Integer, Map<Integer, Integer>> getJobToolMap() {
        return jobToolMap;
    }


}
