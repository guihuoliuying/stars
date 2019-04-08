package com.stars.modules.familyEscort;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.arroundPlayer.ArroundPlayerModule;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.familyEscort.prodata.FamilyEscortConfig;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.ArroundScene;
import com.stars.modules.scene.Scene;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.imp.fight.FamilyEscortPKScene;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.multiserver.MultiServerHelper;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;
import com.stars.util.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author dengzhou 家族运镖的场景定义
 */
public class FamilyEscortScene extends ArroundScene {

	private String arroundId = null;
	
	private long familyId;
	
	private int [] randomPos = null;
	
    public boolean isCanRepeatEnter(Scene newScene,byte newSceneType,int newSceneId, Object extend){
		return true;
    }

	public Object getSceneMsg(){
		return familyId;
	}
    
	@Override
	public String getArroundId(Map<String, Module> moduleMap) {
		if (arroundId != null) {
			return arroundId;
		}
		String key = ((FamilyEscortModule) moduleMap.get(MConst.FamilyActEscort)).getSceneKey();
		arroundId = new StringBuilder("familyEscort_").append(key).toString();
		return arroundId;
	}

	public void enterAndUpdatePosition(RoleModule roleModule,int sceneId,Map<String, Module> moduleMap){
		String posStr = getPosition(moduleMap);
		roleModule.getRoleRow().setPositionStr(posStr);
//		roleModule.updateSafeStageId(sceneId);
		roleModule.updateArroundId(getArroundId(moduleMap));
		ArroundPlayerModule am = (ArroundPlayerModule) moduleMap.get(MConst.ArroundPlayer);
		am.setPosition(posStr);
	}
	
	
	public int[] getEscortRandomBornPostion(){
		List<int[]> bornList = FamilyEscortConfig.config.getEscortBornPosList();
		if( bornList== null|| bornList.size() <=0){
			return null;
		}
		Random random = new Random(System.currentTimeMillis());
		return bornList.get(random.nextInt(bornList.size()));
	}
	
	public int[] getLootRandomBornPostion(){
		List<int[]> bornList = FamilyEscortConfig.config.getRobBornPosList();
		if( bornList== null|| bornList.size() <=0){
			return null;
		}
		Random random = new Random(System.currentTimeMillis());
		return bornList.get(random.nextInt(bornList.size()));
	}
	
	public String getPosition(Map<String, Module> moduleMap) {
		if(this.getNowPosition() !=null){
			return StringUtil.toPositionByArray(this.getNowPosition());
		}
		FamilyModule fModule = (FamilyModule) moduleMap.get(MConst.Family);
		FamilyEscortModule escortModule = (FamilyEscortModule) moduleMap.get(MConst.FamilyActEscort);
		familyId = Long.parseLong(escortModule.getSceneKey());
		
		FamilyAuth fa = fModule.getAuth();
		long roleFamilyId = 0;

		if (fa != null) {
			roleFamilyId = fa.getFamilyId();
		}
		int [] randomPos = null;
		if(this.familyId == roleFamilyId){
			randomPos = getEscortRandomBornPostion();
		}else{
			randomPos = getLootRandomBornPostion();
		}
		this.randomPos = randomPos;
		return StringUtil.toPositionByArray(randomPos);
	}

	@Override
	public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
		return true;
	}

	@Override
	public void exit(Map<String, Module> moduleMap) {
		
	}
	
    public void login(Map<String, Module> moduleMap, Object obj){
    	
    	
    }
	
	/**
	 * 进入场景的处理
	 */
	public void extendEnter(Map<String, Module> moduleMap, Object obj) {
		FamilyModule fModule = (FamilyModule) moduleMap.get(MConst.Family);

		FamilyEscortModule escortModule = (FamilyEscortModule) moduleMap.get(MConst.FamilyActEscort);
		familyId = Long.parseLong(escortModule.getSceneKey());
		
		arroundId = new StringBuilder("familyEscort_").append(familyId).toString();
		
		FamilyAuth fa = fModule.getAuth();
		long roleFamilyId = 0;

		if (fa != null) {
			roleFamilyId = fa.getFamilyId();
		}
		//传参生成
		FamilyEscrotRoleParameter erp = new FamilyEscrotRoleParameter(MultiServerHelper.getServerId(),Long.parseLong(escortModule.getSceneKey()), roleFamilyId, FighterCreator.createSelf(moduleMap), this.getNowPosition(),
				fa,this.randomPos);
		
		ServiceHelper.familyEscortService().extendEnterScene(erp);
	};
	
	/**
	 * 场景退出时的处理
	 */
    public void extendExit(Map<String, Module> moduleMap, Scene  newScene,Object obj){
    	//进入战斗场景则不退出
    	//FIXME 是否需要判断是对应家族的战斗场景
    	if(newScene != null && newScene instanceof FamilyEscortPKScene){
    		return ;
    	}
    	
    	FamilyModule fModule = (FamilyModule) moduleMap.get(MConst.Family);
    	RoleModule rModule = (RoleModule) moduleMap.get(MConst.Role);
		FamilyAuth fa = fModule.getAuth();
		long roleFamilyId = 0;

		if (fa != null) {
			roleFamilyId = fa.getFamilyId();
		}

        if (ServiceHelper.familyEscortService().getFamilyEscortMap(this.familyId).getEscortCar(rModule.getRoleRow().getRoleId()) != null) {
            FamilyEscortModule familyEscortModule = (FamilyEscortModule) moduleMap.get(MConst.FamilyActEscort);
            ServerLogModule serverLogModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
            int spendTime = (int) ((System.currentTimeMillis() - familyEscortModule.getStartTimestamp()) / 1000);
            serverLogModule.logFamilyEscortFinish(spendTime, 0);
        }

        ServiceHelper.familyEscortService().extendExistScene(this.familyId, roleFamilyId,
    			rModule.getRoleRow().getRoleId());

    }
	
	@Override
	public boolean isEnd() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void enter(Map<String, Module> moduleMap, Object obj) {
		
	}

	public long getFamilyId() {
		return familyId;
	}

	public void setFamilyId(long familyId) {
		this.familyId = familyId;
	}

	public int [] getRandomPos() {
		return randomPos;
	}

	public void setRandomPos(int [] randomPos) {
		this.randomPos = randomPos;
	}

	@Override
	public String getPosition() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
