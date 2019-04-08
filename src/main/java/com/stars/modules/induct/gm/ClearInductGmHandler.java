package com.stars.modules.induct.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.induct.InductModule;
import com.stars.modules.induct.userdata.RoleInduct;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/18.
 */
public class ClearInductGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            InductModule inductModule = (InductModule) moduleMap.get(MConst.Induct);
            if ("all".equals(args[0])) {
                for (RoleInduct roleInduct : inductModule.getRoleInductMap().values()) {
                    inductModule.context().delete(roleInduct);
                }
            } else {
                int inductId = Integer.parseInt(args[0]);
                RoleInduct roleInduct = inductModule.getRoleInduct(inductId);
                if (roleInduct == null)
                    throw new IllegalArgumentException();
                inductModule.context().delete(roleInduct);
            }
            PlayerUtil.send(roleId, new ClientText("执行成功, clearinduct" + args[0]));
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行失败, clearinduct" + args[0]));
            LogUtil.error("", e);
        }
    }
}
