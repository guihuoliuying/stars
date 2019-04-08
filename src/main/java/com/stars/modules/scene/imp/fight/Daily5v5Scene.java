package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.scene.Scene;

import java.util.Map;

public class Daily5v5Scene extends Scene{
	
	private boolean isEnd = false;

	@Override
	public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
		return true;
	}

	@Override
	public void enter(Map<String, Module> moduleMap, Object obj) {
	}

	@Override
	public void exit(Map<String, Module> moduleMap) {
		
	}
	
	 public void extendExit(Map<String, Module> moduleMap, Object obj){
		 if(isEnd){
			 return;
		 }
		 isEnd = true;
		 //通知到运镖场景战斗结束
			//进入场景
//		SceneModule sModule = (SceneModule) moduleMap.get(MConst.Scene);
//		sModule.enterScene(SceneManager.SCENETYPE_FAMILY_ESCORT_SAFE_SCENE, FamilyEscortConfig.config.getSafeStageId(), SceneManager.SCENETYPE_FAMILY_ESCORT_SAFE_SCENE);
	 }

	@Override
	public boolean isEnd() {
		return isEnd ;
	}

}
