package com.stars.modules.scene;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;

import java.util.Map;

/**
 * 刷新周围玩家场景接口 Created by zhouyaohui on 2016/10/10.
 */
public abstract class ArroundScene extends Scene{
	
	private int[] nowPosition;
	
	/**
	 * 客户端心跳包更新位置
	 * @param pos
	 * @param sceneId
	 * @param sceneObjectId
	 */
	public void updatePositionByClient(int [] pos,int sceneId,int sceneObjectId){
		if(this.getSceneId() == sceneId){
			this.nowPosition = pos;
		}
	}
	
	public void cannotEnterNewSceneDo(Map<String, Module> moduleMap, Object obj) {
		RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
		roleModule.initSafeStage();
		SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
		sceneModule.enterScene(SceneManager.SCENETYPE_CITY, roleModule.getSafeStageId(), "");
	}
	
	public void enterAndUpdatePosition(RoleModule roleModule,int sceneId,Map<String, Module> moduleMap){
		roleModule.getRoleRow().setPositionStr(getPosition());
		roleModule.updateSafeStageId(sceneId);
		roleModule.updateArroundId(getArroundId(moduleMap));
	}

	public abstract String getArroundId(Map<String, Module> moduleMap);

	/**
	 * 位置可以是出生位置，也可以是保存下来的位置，具体看需求
	 * 
	 * @return
	 */
	public abstract String getPosition();

	public int[] getNowPosition() {
		return nowPosition;
	}

	public void setNowPosition(int[] nowPosition) {
		this.nowPosition = nowPosition;
	}
}
