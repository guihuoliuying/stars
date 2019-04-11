package com.stars.modules.vip.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/12/8.
 */
public class ResetFirstChargeGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            PlayerUtil.send(roleId, new ClientText("执行成功,resetfirstcharge "));
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行失败,resetfirstcharge "));
        }
    }
}
