package com.stars.modules.tool.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;

import java.util.Map;

/**
 * Created by zhangjiahua on 2016/2/29.
 */
public class DeleteItemGmHandler  implements GmHandler  {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) {
        String toolInfo = args[0];          //grid=count
        String[] infoStr = toolInfo.split("=");
        int itemId = Integer.parseInt(infoStr[0]);
        int count = Integer.parseInt(infoStr[1]);
        if(count <= 0 ){
            return;
        }
        ToolModule module = (ToolModule)moduleMap.get(MConst.Tool);
       module.deleteAndSend(itemId, count, EventType.GM.getCode());
    }
}
