package com.stars.modules.dragonboat.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.dragonboat.DragonBoatConst;
import com.stars.modules.gm.GmHandler;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by zhangjiahua on 2016/2/25.
 */
public class DragonBoatGmHandler implements GmHandler {


    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) {
        try {
            String cmd0 = args[0];
            String[] cmds = cmd0.split("=");
            String cmd = cmds[0];
            String step = cmds[1];
            switch (cmd) {
                case "step": {
                    switch (Integer.parseInt(step)) {
                        case DragonBoatConst.BET_ON: {
                            ServiceHelper.opDragonBoatService().startBetOn();
                        }
                        break;
                        case DragonBoatConst.RACING: {
                            ServiceHelper.opDragonBoatService().startRacing();
                        }
                        break;
                        case DragonBoatConst.WAITING: {
                            ServiceHelper.opDragonBoatService().startWaiting();
                        }
                        break;
                        case DragonBoatConst.SHOW: {
                            ServiceHelper.opDragonBoatService().startShow();
                        }
                        break;
                        case DragonBoatConst.FINISH: {
                            ServiceHelper.opDragonBoatService().finishRound();
                        }
                    }
                }
                break;
                case "updatesteptime": {
                    ServiceHelper.opDragonBoatService().updateStepTime();
                }
                break;
            }

        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            PlayerUtil.send(roleId, new ClientText("执行失败,additem " + args[0]));
        }
    }
}
