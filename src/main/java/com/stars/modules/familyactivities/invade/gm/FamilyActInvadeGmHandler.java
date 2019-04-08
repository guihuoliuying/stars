package com.stars.modules.familyactivities.invade.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;
import com.stars.services.family.activities.invade.FamilyActInvadeFlow;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/25.
 */
public class FamilyActInvadeGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            switch (args[0]) {
                case "start":
                    FamilyActInvadeFlow.start();
                    break;
                case "end":
                    FamilyActInvadeFlow.end();
                    break;
            }
            PlayerUtil.send(roleId, new ClientText("执行成功,family.act.invade " + args[0]));
        } catch (Exception e) {
            LogUtil.error("", e);
            PlayerUtil.send(roleId, new ClientText("执行失败,family.act.invade " + args[0]));
        }
    }
}
