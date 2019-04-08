package com.stars.modules.gm.gmhandler;

import com.stars.core.module.Module;
import com.stars.core.module.ModuleManager;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/1/14.
 */
public class ReloadGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        // fixme: 对全服玩家作用的GM应该独立于Player执行
        String param = args[0];
        if (param.trim().equalsIgnoreCase("all")) {
            ModuleManager.loadProductData();
        } else {
            ModuleManager.loadProductData(param);
        }
        LogUtil.info("游戏服重载数据成功");
        PlayerUtil.send(roleId, new ClientText("success"));
    }
}

