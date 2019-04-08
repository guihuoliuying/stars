package com.stars.modules.scene.imp.city;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gamecave.GameCaveManager;
import com.stars.modules.gamecave.packet.ClientEnterGameScene;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.ArroundScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.prodata.SafeinfoVo;

import java.util.Map;

/**
 * Created by panzhenfeng on 2016/9/6.
 */
public class GameCaveScene extends ArroundScene{
	private String position;

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object layerId) {
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
    	 RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
    	 int safeId = GameCaveManager.gameCaveSafeId;
         SafeinfoVo safeInfoVo = SceneManager.getSafeVo(safeId);
         ClientEnterGameScene clientEnterGameScene = new ClientEnterGameScene();
         clientEnterGameScene.setSafeId(safeId);
         if (roleModule.getSafeStageId() != safeId) {
             /** 上一次不是家族场景，初始化出生位置 */
             position = safeInfoVo.getCharPosition();
             clientEnterGameScene.setPostionStr(safeInfoVo.getCharPosition());
         } else {
             position = roleModule.getRoleRow().getPositionStr();
             clientEnterGameScene.setPostionStr(roleModule.getRoleRow().getPositionStr());
         }
  
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, getSceneId());
        sceneModule.send(clientEnterGameScene);
    }

    @Override
    public void exit(Map<String, Module> moduleMap) {

    }

    @Override
    public boolean isEnd() {
        return false;
    }

	@Override
	public String getArroundId(Map<String, Module> moduleMap) {
		return "";
	}

	@Override
	public String getPosition() {
		return position;
	}
}
