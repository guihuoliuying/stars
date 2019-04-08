package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.familyEscort.FamilyEscortScene;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.Scene;
import com.stars.modules.scene.SceneModule;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;

import java.util.Map;

/**
 * 
 * 家族运镖战斗场景，表示该玩家正在战斗中
 * @author xieyuejun
 *
 */
public class FamilyEscortPKScene extends Scene {
	
	private boolean isEnd = false;
	private long enterFamliyId;

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
	
	public void extendExit(Map<String, Module> moduleMap, Scene newScene, Object obj) {
		if (isEnd) {
			return;
		}
		isEnd = true;
		SceneModule roleModule = (SceneModule) moduleMap.get(MConst.Scene);

		RoleModule roleM = (RoleModule) moduleMap.get(MConst.Role);
		// 进入战斗场景则不退出
		// FIXME 是否需要判断是对应家族的战斗场景
		ServiceHelper.familyEscortService().extendExistSceneFromFight(enterFamliyId, roleM.getRoleRow().getRoleId());

		if (newScene != null && newScene instanceof FamilyEscortScene) {
			return;
		}
		FamilyModule fModule = (FamilyModule) moduleMap.get(MConst.Family);
		FamilyAuth fa = fModule.getAuth();
		long roleFamilyId = 0;

		if (fa != null) {
			roleFamilyId = fa.getFamilyId();
		}
		ServiceHelper.familyEscortService().extendExistScene(this.enterFamliyId, roleFamilyId, roleM.getRoleRow().getRoleId());

		// 通知到运镖场景战斗结束
		// 进入场景
		// SceneModule sModule = (SceneModule) moduleMap.get(MConst.Scene);
		// sModule.enterScene(SceneManager.SCENETYPE_FAMILY_ESCORT_SAFE_SCENE,
		// FamilyEscortConfig.config.getSafeStageId(),
		// SceneManager.SCENETYPE_FAMILY_ESCORT_SAFE_SCENE);
	}

	@Override
	public boolean isEnd() {
		return isEnd ;
	}

	public long getEnterFamliyId() {
		return enterFamliyId;
	}

	public void setEnterFamliyId(long enterFamliyId) {
		this.enterFamliyId = enterFamliyId;
	}

}
