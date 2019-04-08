package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * 击杀怪物道具, 目前为主动击杀当前出现的全部怪物;
 * Created by panzhenfeng on 2016/8/25.
 */
public class KillMonsterToolFunc extends ToolFunc {
    private byte type;
    private String hintStr;

    public KillMonsterToolFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        //格式10|类型|文本索引; (注意:这里的类型为保留字段, 暂时没用到)
        if (StringUtil.isNotEmpty(function)) {
            String[] itemArr = function.split(",");
            String[] args = itemArr[0].split("\\|");
            type = Byte.parseByte(args[1]);
            if (args.length > 2) {
                parseNotice(args[2]);
            }
            if (itemArr.length > 1) {
                hintStr = itemArr[1];
                hintStr = DataManager.getGametext(hintStr);
            }
        }
    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
        ToolFuncResult tr = super.useCondition(moduleMap, args);
        if (tr == null) {
            tr = new ToolFuncResult(true, null);
        }
        //判断当前是否在战斗场景中;
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        if (!sceneModule.getScene().isInFightScene()) {
            return new ToolFuncResult(false, new ClientText("不能在非战斗场景中使用该道具"));
        }
        return tr;
    }

    @Override
    public Map<Integer, Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        //判断当前是否在战斗场景中;
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        ((FightScene) sceneModule.getScene()).serverKillMonster(moduleMap, type);
        if(StringUtil.isNotEmpty(hintStr)){
            sceneModule.warn(hintStr);
        }
        return null;
    }
}