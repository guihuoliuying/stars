package com.stars.modules.foreshow.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.foreshow.ForeShowManager;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by chenkeyu on 2016/11/10.
 */
public class ForeShowGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        ForeShowModule foreShowModule = (ForeShowModule) moduleMap.get(MConst.ForeShow);
        if (args[0].equals("all")) {
            foreShowModule.openAll();
            PlayerUtil.send(roleId, new ClientText("已开启所有功能"));
        }
        if (args[0].equals("0")) {
            ForeShowManager.loginCheck = false;
            PlayerUtil.send(roleId, new ClientText("已关闭登陆检查(默认开启)"));
        }
        if (args[0].equals("1")) {
            ForeShowManager.loginCheck = true;
            PlayerUtil.send(roleId, new ClientText("已开启登陆检查(默认开启)"));
        }
        if (args[0].equalsIgnoreCase("DailyWindow")) {
            PlayerUtil.send(roleId, new ClientText(foreShowModule.isOpen(args[0]) + ""));
        }
    }
}
