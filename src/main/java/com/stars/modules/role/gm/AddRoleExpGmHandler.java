package com.stars.modules.role.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.role.RoleModule;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/5/19.
 */
public class AddRoleExpGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            int val = Integer.parseInt(args[0]);
            RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
            if (val > 0) {
                roleModule.addExp(val);
                PlayerUtil.send(roleId, new ClientText("执行成功, addRoleExp " + args[0]));
            } else {
                PlayerUtil.send(roleId, new ClientText("执行失败, addRoleExp " + args[0]));
            }
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行失败, addRoleExp " + args[0]));
        }
    }
}
