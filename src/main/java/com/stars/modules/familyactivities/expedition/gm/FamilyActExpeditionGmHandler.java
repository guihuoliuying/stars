package com.stars.modules.familyactivities.expedition.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.familyactivities.expedition.FamilyActExpeditionModule;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.imp.fight.FamilyActExpeditionScene;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/10/12.
 */
public class FamilyActExpeditionGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        FamilyActExpeditionModule module = (FamilyActExpeditionModule) moduleMap.get(MConst.FamilyActExpe);
        switch (args[0]) {
            case "fight":
                module.fight(Integer.parseInt(args[1]));
                break;
            case "addbuff":
                module.addBuff(Integer.parseInt(args[1]));
                break;
            case "passStep":
                SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
                FamilyActExpeditionScene scene = (FamilyActExpeditionScene) sceneModule.getScene();
                scene.finishDeal(moduleMap, SceneManager.STAGE_VICTORY);
                break;
            case "start":
                FamilyActExpeditionModule.start();
                break;
            case "end":
                FamilyActExpeditionModule.end();
                break;
            case "openall":
                module.gmOpenAll();
                break;
        }
    }
}
