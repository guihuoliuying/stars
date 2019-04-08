package com.stars.modules.scene.imp.city;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.event.FamilyWarEnterSafeSceneEvent;
import com.stars.modules.familyactivities.war.packet.ui.ClientFamilyWarSafeScene;
import com.stars.modules.scene.ArroundScene;
import com.stars.modules.scene.Scene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.prodata.SafeinfoVo;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-04-11 18:16
 */
public class FamilyWarSafeScene extends ArroundScene {
    private String position;

    @Override
    public String getArroundId(Map<String, Module> moduleMap) {
        FamilyModule familyModule = (FamilyModule) moduleMap.get(MConst.Family);
        return String.valueOf(familyModule.getAuth().getFamilyId());
    }

    @Override
    public String getPosition() {
        return position;
    }

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
        if ("".equals(obj)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isCanRepeatEnter(Scene newScene, byte newSceneType, int newSceneId, Object extend) {
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
        LogUtil.info("familywar|进入家族战备战场景");
        SafeinfoVo curSafeinfoVo = SceneManager.getSafeVo(FamilyActWarManager.stageIdOfSafe);
        position = curSafeinfoVo.getCharPosition();
        FamilyWarEnterSafeSceneEvent safeSceneEvent = (FamilyWarEnterSafeSceneEvent) obj;
        ClientFamilyWarSafeScene safeScene = new ClientFamilyWarSafeScene();
        safeScene.setPostionStr(position);
        safeScene.setType(safeSceneEvent.getType());
        safeScene.setSafeId(FamilyActWarManager.stageIdOfSafe);
        safeScene.setMemberType(safeSceneEvent.getMemberType());
        safeScene.setCamp1FamilyPoints(safeSceneEvent.getCamp1FamilyPoints());
        safeScene.setCamp2FamilyPoints(safeSceneEvent.getCamp2FamilyPoints());
        safeScene.setRemainTime(safeSceneEvent.getRemainTime());
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        sceneModule.send(safeScene);
    }

    @Override
    public void exit(Map<String, Module> moduleMap) {

    }

    @Override
    public boolean isEnd() {
        return false;
    }
}
