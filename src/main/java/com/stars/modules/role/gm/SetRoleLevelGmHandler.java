package com.stars.modules.role.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.userdata.Role;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/4.
 */
public class SetRoleLevelGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            int level = Integer.parseInt(args[0]);
            RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
            Role role = roleModule.getRoleRow();
            if (level <= 0 || level > RoleManager.getMaxlvlByJobId(role.getJobId())) {
                throw new IllegalArgumentException("level不能小于1并且不能大于最高等级配置");
            }
            role.setLevel(level);
            roleModule.context().update(role);
            PlayerUtil.send(roleId, new ClientText("执行成功, setrolelevel " + args[0]));
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行失败, setrolelevel " + args[0]));
        }
    }
}
