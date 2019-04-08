package com.stars.modules.tool.func.impl;

import com.stars.core.attr.Attr;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.packet.ClientInFightChangeAttr;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * 更改血量道具;
 * Created by panzhenfeng on 2016/8/25.
 */
public class HpToolFunc extends ToolFunc {

    private float rate = 0.0f;
    private String hintStr;

    public HpToolFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        //格式9|百分比|文本索引;
        if (StringUtil.isNotEmpty(function)) {
            String[] itemArr = function.split(",");
            String[] args = itemArr[0].split("\\|");
            rate = Float.parseFloat(args[1]) / 100;
            if (args.length > 2) {
                parseNotice(args[2]);
            }
            if (itemArr.length > 1) {
                hintStr = itemArr[1];
                hintStr = String.format(DataManager.getGametext(hintStr), rate * 100);
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
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        ClientInFightChangeAttr clientInFightChangeAttr = new ClientInFightChangeAttr();
        int addedValue = (int) (Math.ceil(roleModule.getRoleRow().getTotalAttr().getHp() * rate));
        clientInFightChangeAttr.addAddedAttrValue((byte) Attr.HP.getIndexId(), addedValue);
        clientInFightChangeAttr.setHintStr(hintStr);
        roleModule.send(clientInFightChangeAttr);
        return null;
    }
}
