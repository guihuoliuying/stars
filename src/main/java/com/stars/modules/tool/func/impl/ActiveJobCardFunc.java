package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.changejob.ChangeJobModule;
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
public class ActiveJobCardFunc extends ToolFunc {
    private Integer jobId;
    private Map<Integer, Integer> itemDic;

    public ActiveJobCardFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        String[] args = function.split("\\|");
        jobId = Integer.parseInt(args[1]);
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
        ChangeJobModule changeJobModule = (ChangeJobModule) moduleMap.get(MConst.ChangeJob);
        ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
        boolean actived = changeJobModule.isActivedJob(jobId);
        if (actived) {
            toolModule.addAndSend(itemDic, EventType.JOB_ACTIVE_CARD_REPEAT_RESOLVE.getCode());
        } else {
            changeJobModule.gotoActiveView(jobId);
        }
        return null;
    }
}
