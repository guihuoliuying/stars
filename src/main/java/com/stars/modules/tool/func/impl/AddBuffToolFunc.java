package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.packet.ClientAddBuff;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * 添加Buff道具;
 * Created by panzhenfeng on 2016/8/25.
 */
public class AddBuffToolFunc extends ToolFunc {
    private int buffId;
    private int buffLevel = 0;
    private String hintStr;
    public AddBuffToolFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        //格式11|buffId+buffLevel|文本索引; (注意:这里的类型为保留字段, 暂时没用到)
        if(StringUtil.isNotEmpty(function) && !function.equals("0")){
            String[] itemArr = function.split(",");
            String[] args = itemArr[0].split("\\|");
            String[] valueArr = args[1].split("\\+");
            buffId = Integer.parseInt(valueArr[0]);
            if(valueArr.length>1){
                buffLevel = Integer.parseInt(valueArr[1]);
            }
            if(args.length>2){
                parseNotice(args[2]);
            }
            if (itemArr.length > 1) {
                hintStr = itemArr[1];
            }
        }
    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
        ToolFuncResult tr = super.useCondition(moduleMap, args);
        if(tr == null){
            tr = new ToolFuncResult(true, null);
        }
        //判断当前是否在战斗场景中;
        SceneModule sceneModule = (SceneModule)moduleMap.get(MConst.Scene);
        if(!sceneModule.getScene().isInFightScene()){
            return new ToolFuncResult(false, new ClientText("不能在非战斗场景中使用该道具"));
        }
        return tr;
    }

    @Override
    public Map<Integer,Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        SceneModule sceneModule = (SceneModule)moduleMap.get(MConst.Scene);
        ClientAddBuff clientAddBuff = new ClientAddBuff(buffId, buffLevel);
        sceneModule.send(clientAddBuff);
        if(StringUtil.isNotEmpty(hintStr)){
            sceneModule.warn(hintStr);
        }
        return null;
    }
}
