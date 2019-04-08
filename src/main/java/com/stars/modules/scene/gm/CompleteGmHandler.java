package com.stars.modules.scene.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/7/15.
 */
public class CompleteGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
            FightScene fs = (FightScene)sceneModule.getScene();
            if (args != null && args.length == 1 && args[0].equals("0")) {
                fs.stageStatus = SceneManager.STAGE_FAIL;
                fs.finishDeal(moduleMap, SceneManager.STAGE_FAIL);
            } else {
                fs.stageStatus = SceneManager.STAGE_VICTORY;
                fs.finishDeal(moduleMap, SceneManager.STAGE_VICTORY);
            }
            PlayerUtil.send(roleId, new ClientText("执行成功, stagecomplete"));
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行失败, stagecomplete"));
        }
    }
}
