package com.stars.modules.tool.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by zhangjiahua on 2016/2/25.
 */
public class AddItemGmHandler implements GmHandler {


    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) {
        try {
            String toolInfo = args[0];          //itemid=count
            String[] infoStr = toolInfo.split("=");
            int itemId = Integer.parseInt(infoStr[0]);
            int count = Integer.parseInt(infoStr[1]);
            if (count <= 0) {
                return;
            }
            //要区别存放道具奖励;
            ItemVo itemVo = ToolManager.getItemVo(itemId);
            if (itemVo.getType() == ToolManager.TYPE_SEARCHTREASURE) {

            } else {
                ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
                toolModule.addAndSend(itemId, count, EventType.GM.getCode());
            }
            PlayerUtil.send(roleId, new ClientText("执行成功,additem " + args[0]));
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            PlayerUtil.send(roleId, new ClientText("执行失败,additem " + args[0]));
        }
    }
}
