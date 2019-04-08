package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.packet.ClientClearCd;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.SkillModule;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * 清除CD道具;
 * Created by panzhenfeng on 2016/8/25.
 */
public class ClearCdToolFunc extends ToolFunc {
    private byte type;
    private String hintStr;

    public ClearCdToolFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        //格式11|类型|文本索引; (注意:这里的类型为保留字段, 暂时没用到)
        if (StringUtil.isNotEmpty(function)) {
            String[] itemArr = function.split(",");
            String[] args = itemArr[0].split("\\|");
            type = Byte.parseByte(args[1]);
            if (args.length > 2) {
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
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        SkillModule skillModule = (SkillModule) moduleMap.get(MConst.Skill);
        ClientClearCd clientClearCd = new ClientClearCd(type);
        sceneModule.send(clientClearCd);
        if (StringUtil.isNotEmpty(hintStr)) {
            int ultimateSkillId = skillModule.getUltimateSkillId();
            if (ultimateSkillId > 0) {
                SkillVo ultimateSkillVo = SkillManager.getSkillVo(ultimateSkillId);
                if (ultimateSkillVo != null) {
                    String tmp = String.format(DataManager.getGametext(hintStr), DataManager.getGametext(ultimateSkillVo.getName()));
                    sceneModule.warn(tmp);
                }
            }
        }
        return null;
    }
}

