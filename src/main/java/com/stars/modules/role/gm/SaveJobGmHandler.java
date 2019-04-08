package com.stars.modules.role.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.core.persist.SaveDBManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by wuyuxing on 2017/3/6.
 */
public class SaveJobGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            int flag = Integer.parseInt(args[0]);
            SaveDBManager.enableSaving = flag == 1;
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行失败, setsave param:" + args[0]));
        }
    }
}
