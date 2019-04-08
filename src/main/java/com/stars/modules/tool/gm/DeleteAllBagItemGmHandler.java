package com.stars.modules.tool.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;

import java.util.Map;

/**
 * Created by chenkeyu on 2016/11/3.
 */
public class DeleteAllBagItemGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        if(args[0].equals("all")){
            ToolModule module = (ToolModule)moduleMap.get(MConst.Tool);
            module.deleteAndSend(EventType.GM.getCode());
        }
        PlayerUtil.send(roleId, new ClientText("执行成功,additem " + args[0]));
    }
}
