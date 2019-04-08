package com.stars.modules.familyEscort;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.drop.DropModule;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.event.FamilyAuthUpdatedEvent;
import com.stars.modules.familyEscort.event.FamilyEscortDropEvent;
import com.stars.modules.familyEscort.event.FamilyEscortEnterPKEvent;
import com.stars.modules.familyEscort.event.FamilyEscortFlowEvent;
import com.stars.modules.familyEscort.prodata.FamilyEscortConfig;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.imp.fight.FamilyEscortPKScene;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.tool.ToolModule;
import com.stars.multiserver.MultiServerHelper;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;
import com.stars.services.familyEscort.FamilyEscortService;
import com.stars.services.familyEscort.FamilyEscortServiceActor;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class FamilyEscortModule extends AbstractModule {

	/**
	 * 用于获取arroundId，在参与家族运镖的时候，设置这个值，建议使用家族ID
	 */
	private String sceneKey;

	private String lootFamily;

	//当前的运镖场景，用于战斗场景切换回来用上
	private FamilyEscortScene currentEscortScene;

	public long startTimestamp = 0;    // 开始时间戳
	public long endTimestamp = 0;      // 结束时间戳

    public boolean isAlredayPrint;     //是否已经打印了结束日志

	public FamilyEscortModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("FamilyEscort", id, self, eventDispatcher, moduleMap);
    }

	public long getStartTimestamp() {
		return startTimestamp;
	}

	public long getEndTimestamp() {
		return endTimestamp;
	}

	public String getSceneKey() {
		return sceneKey;
	}

	public void setSceneKey(String sceneKey) {
		this.sceneKey = sceneKey;
	}

	public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
	}

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        signCalRedPoint(MConst.FamilyActEscort, RedPointConst.FAMILY_ESCORT_FLOW);
    }

	@Override
	public void onOffline() throws Throwable {
        RoleModule roleModule = module(MConst.Role);
        SceneModule sceneModule = module(MConst.Scene);
        FamilyModule fModule = module(MConst.Family);
        if (sceneModule.getScene() != null && (sceneModule.getScene() instanceof FamilyEscortScene
        		|| sceneModule.getScene() instanceof FamilyEscortPKScene)) {
//            sceneModule.backToCity();
            ServiceHelper.familyEscortService().offline(Long.parseLong(this.getSceneKey()),fModule.getAuth().getFamilyId(),roleModule.getRoleRow().getRoleId());
        }
//		if (FamilyEscortServiceActor.flowState == FamilyEscortService.FLOW_STATE_START) {
//
//		}
	}

	@Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        // 用红点控制主界面的入口显示
        if (redPointIds.contains(RedPointConst.FAMILY_ESCORT_FLOW)) {
			FamilyAuth auth = ((FamilyModule) module(MConst.Family)).getAuth();
			ForeShowModule foreShowModule = module(MConst.ForeShow);
			if (foreShowModule.isOpen(ForeShowConst.FAMILY_ESCORT) && auth.hasFamily()) { // 系统开启以及有家族的情况下才显示
				int flowState = FamilyEscortServiceActor.flowState;
				switch (flowState) {
					case FamilyEscortService.FLOW_STATE_PREPARE:
						redPointMap.put(RedPointConst.FAMILY_ESCORT_FLOW,
								"1+" + FamilyEscortServiceActor.startTimestamp); // 入口：准备，倒数
						break;
					case FamilyEscortService.FLOW_STATE_START:
						redPointMap.put(RedPointConst.FAMILY_ESCORT_FLOW, "2"); // 入口：开始
						break;
					case FamilyEscortService.FLOW_STATE_ESCORTEND:
						redPointMap.put(RedPointConst.FAMILY_ESCORT_FLOW, null); // 入口：关闭
						break;
				}
			} else {
				redPointMap.put(RedPointConst.FAMILY_ESCORT_FLOW, null); // 入口：关闭
			}
        }
    }

    public void onEvent(Event event) {
        if (event instanceof FamilyEscortFlowEvent
                || event instanceof FamilyAuthUpdatedEvent
				|| event instanceof ForeShowChangeEvent) {
            signCalRedPoint(MConst.FamilyActEscort, RedPointConst.FAMILY_ESCORT_FLOW);
        }else if (event instanceof FamilyEscortDropEvent) {
        	FamilyEscortDropEvent fDropEvent = (FamilyEscortDropEvent)event;
            boolean isWin = fDropEvent.isWin();
            int dropId = fDropEvent.getDropId();
        	DropModule dropModule = module(MConst.Drop);
            ToolModule toolModule = module(MConst.Tool);
            Map<Integer, Integer> toolMap = null;
            if (dropId > 0) {
                toolMap = dropModule.executeDrop(fDropEvent.getDropId(), 1, false);
				toolModule.addAndSend(toolMap, EventType.FAMILY_ESCORT_LOOT.getCode());
			}

            // 结算框
            if(!fDropEvent.isFinishEscort()){
	            ClientStageFinish packet = new ClientStageFinish(
	                    SceneManager.SCENETYPE_FAMILY_ESCORT_PVP_SCENE, (byte) (isWin ? 2 : 0));
	            packet.setIsHasCar(fDropEvent.getIsHasCar());
	            if (toolMap != null) {
	                packet.setItemMap(toolMap);
	            }
	            send(packet);
            }

            String escortStartAward = FamilyEscortConfig.config.getEscortStartAward();
            if(escortStartAward.contains(String.valueOf(dropId))){
                if(isWin && !isAlredayPrint){
                    isAlredayPrint = true;
                    endTimestamp = System.currentTimeMillis();
                    int spendTime = (int) ((endTimestamp - startTimestamp) / 1000);
                    ServerLogModule serverLogModule = module(MConst.ServerLog);
                    serverLogModule.logFamilyEscortFinish(spendTime, 1);
                }
            }

        }else if (event instanceof FamilyEscortEnterPKEvent) {
        	//进入pk场景
        	FamilyEscortPKScene eps = new FamilyEscortPKScene();
        	eps.setEnterFamliyId(((FamilyEscortEnterPKEvent) event).getFamliyId());
        	SceneModule dropModule = module(MConst.Scene);
        	if(dropModule.getScene() != null && dropModule.getScene() instanceof FamilyEscortScene){
        		this.currentEscortScene = (FamilyEscortScene) dropModule.getScene();
        	}

        	dropModule.enterScene(eps, SceneManager.SCENETYPE_FIGHTPK, -99, null);
		}
    }

	/**
	 * @param lootFamily 被劫的帮派
	 * 参与劫镖
	 */
	public void joinLootEscort(String lootFamily){
		sceneKey = lootFamily.trim();
		this.lootFamily = lootFamily.trim();
		SceneModule sModule = module(MConst.Scene);
		BuddyModule buddyModule = module(MConst.Buddy);
		sModule.enterScene(SceneManager.SCENETYPE_FAMILY_ESCORT_SAFE_SCENE, FamilyEscortConfig.config.getSafeStageId(), SceneManager.SCENETYPE_FAMILY_ESCORT_SAFE_SCENE);
//		enterEscortScene(lootFamily);
		ServiceHelper.familyEscortService().joinLootEscort(MultiServerHelper.getServerId(),
				lootFamily, FighterCreator.createSelf(moduleMap()),
                null);
//				FighterCreator.create(FighterEntity.TYPE_ROBOT, (byte) 1, buddyModule.getRoleBuddy(buddyModule.getFightBuddyId())));

	}

	/**
	 * 劫镖
	 */
	public void lootEscort(String id){
		ServiceHelper.familyEscortService().lootEscort(lootFamily, String.valueOf(id()), id);
	}

	/**
	 * 发起运镖
	 */
	public void actEscort(){
		FamilyModule fModule = module(MConst.Family);
		FighterEntity entity = FighterCreator.createSelf(moduleMap());
		//FIXME 是否需要判断玩家有没有家族
        if (fModule.getAuth().getFamilyId() == 0) {
            warn("没有家族");
            return;
        }
        if(fModule.getAuth().getFamilyId() != Long.parseLong(sceneKey)){
        	warn("不在本方家族中");
            return;
        }
		ServiceHelper.familyEscortService().actEscort(fModule.getAuth(), entity);

        isAlredayPrint = false;
		startTimestamp = System.currentTimeMillis();
		ServerLogModule serverLogModule = module(MConst.ServerLog);
		serverLogModule.logFamilyEscortBegin();
	}

	/**
	 * 参加运镖活动
	 */
	public void joinActEscort(){

		FamilyModule fModule = module(MConst.Family);
		sceneKey = String.valueOf(fModule.getAuth().getFamilyId());
		//进入场景
		SceneModule sModule = module(MConst.Scene);
		BuddyModule buddyModule = module(MConst.Buddy);
		sModule.enterScene(SceneManager.SCENETYPE_FAMILY_ESCORT_SAFE_SCENE, FamilyEscortConfig.config.getSafeStageId(), SceneManager.SCENETYPE_FAMILY_ESCORT_SAFE_SCENE);
//		enterEscortScene(fModule.getAuth().getFamilyId()+"");
		ServiceHelper.familyEscortService().joinActEscort(MultiServerHelper.getServerId(),
				fModule.getAuth(), FighterCreator.createSelf(moduleMap()),
                null);
//				FighterCreator.create(FighterEntity.TYPE_ROBOT, (byte) 2, buddyModule.getRoleBuddy(buddyModule.getFightBuddyId())));
	}

	/**
	 * 打开运镖界面
	 */
	public void openEscortUI(){
		FighterEntity entity = FighterCreator.createSelf(moduleMap());
		ServiceHelper.familyEscortService().openEscortUI(entity);
	}

	/**
	 * 展示劫镖列表
	 */
	public void showEscortList(){
		FamilyModule fModule = module(MConst.Family);
		FighterEntity entity = FighterCreator.createSelf(moduleMap());
		ServiceHelper.familyEscortService().showEscortList(fModule.getAuth().getFamilyId(),entity);
	}


	public void removeEscortBarrier(){
		FamilyModule fModule = module(MConst.Family);
		ServiceHelper.familyEscortService().removeBarrier(fModule.getAuth().getFamilyId(), id());
	}

	public void killRole(String roleId){
		FamilyModule fModule = module(MConst.Family);
		ServiceHelper.familyEscortService().killRole(Long.parseLong(sceneKey),id(), Long.parseLong(roleId));
	}

	public void confirmFightResult(String roleId){
		SceneModule sModule = module(MConst.Scene);
		sModule.enterScene(this.currentEscortScene,SceneManager.SCENETYPE_FAMILY_ESCORT_SAFE_SCENE, FamilyEscortConfig.config.getSafeStageId(), SceneManager.SCENETYPE_FAMILY_ESCORT_SAFE_SCENE);
	}

	public FamilyEscortScene getCurrentEscortScene() {
		return currentEscortScene;
	}

	public void setCurrentEscortScene(FamilyEscortScene currentEscortScene) {
		this.currentEscortScene = currentEscortScene;
	}

	/**
	 * 运镖过程中，断线重连的处理
	 */
	public void onReconnect() throws Throwable {
		RoleModule roleModule = module(MConst.Role);
		SceneModule sceneModule = module(MConst.Scene);
		FamilyModule fModule = module(MConst.Family);
		if (sceneModule.getScene() != null
				&& (sceneModule.getScene() instanceof FamilyEscortScene || sceneModule.getScene() instanceof FamilyEscortPKScene)) {
			ServiceHelper.familyEscortService().reconnect(Long.parseLong(this.getSceneKey()), fModule.getAuth().getFamilyId(),
					roleModule.getRoleRow().getRoleId());
		}
	}

}
