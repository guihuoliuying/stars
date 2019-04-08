package com.stars.services.familyEscort;

import com.stars.modules.familyEscort.FamilyEscrotRoleParameter;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.services.Service;
import com.stars.services.family.FamilyAuth;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.List;

public interface FamilyEscortService extends Service, ActorService {

    public static final int FLOW_STATE_PREPARE = 1;
    public static final int FLOW_STATE_START = 2;
    public static final int FLOW_STATE_ESCORTEND = 3;
    public static final int FLOW_STATE_END = 4;

	FamilyEscortMap getFamilyEscortMap(long familyId);

	@AsyncInvocation
	void offline(long enterFamilyId,long roleFamilyId,long roleId);
	
	public int getEscount(long roleId);
	
	@AsyncInvocation
	public void reconnect(long enterFamilyId, long roleFamilyId,long roleId);
	
	/**
	 * @param fAuth
	 * @param role
	 * 发起运镖
	 */
	@AsyncInvocation
	void actEscort(FamilyAuth fAuth,FighterEntity role);
	
	/**
	 * @param fAuth
	 * @param role
	 * 参加运镖
	 */
	@AsyncInvocation
	void joinActEscort(int serverId,FamilyAuth fAuth,FighterEntity entity, FighterEntity buddyEntity);
	
	/**
	 * @param familyId
	 * @param id
	 * 劫镖
	 */
	@AsyncInvocation
	void lootEscort(String familyId,String self, String aim);
	
	
	/**
	 * 进入运镖场景下发相关信息
	 */
	@AsyncInvocation	   
	public void extendEnterScene(FamilyEscrotRoleParameter erp);
	
	/**
	 * 退出运镖场景的处理
	 */
	@AsyncInvocation	   
	public void extendExistScene(long enterFamilyId, long roleFamilyId, long roleId);
	
	/**
	 * 从战斗场景退出的处理
	 * @param enterFamilyId
	 * @param roleFamilyId
	 * @param role
	 */
	@AsyncInvocation	   
	public void extendExistSceneFromFight(long enterFamily,long roleId);
	
	/**
	 * @param familyId
	 * @param role
	 * 参加劫镖活动
	 */
	@AsyncInvocation
	void joinLootEscort(int serverId,String familyId, FighterEntity entity, FighterEntity buddyEntity);
	
	/**
	 * 周围玩家列表更新
	 * @param roleId
	 * @param familyId
	 * @param list
	 */
	@AsyncInvocation
	public void updateFlushArroundPlayerList(long roleId,Object sceneMsg,List<Long> list);
	
	/**
	 * 
	 * 显示拦截运镖界面
	 * @param role
	 */
	@AsyncInvocation
	void showEscortList(long famliyId,FighterEntity role);
	
	
	/**
	 * 打开运镖入口界面
	 * @param role
	 */
	@AsyncInvocation
	 void openEscortUI(FighterEntity role);
	
	/**
	 * @param familyId
	 * @param roleId
	 * 移除阻碍物
	 */
	@AsyncInvocation
	void removeBarrier(long familyId,long roleId);

	@AsyncInvocation
	void runUpdate();
	
	@AsyncInvocation
	void saveUserData();

    /*
     * 活动流程
     */
    /**
     * 流程：准备
     */
    @AsyncInvocation
    void prepare();

    /**
     * 流程：开始
     */
    @AsyncInvocation
    void start();
    
    /**
     * 流程：押镖结束
     */
	void escortEnd();

    /**
     * 流程：结束
     */
    @AsyncInvocation
    void end();

    /**
     * 流程：清场
     */
    @AsyncInvocation
    void clearup();
    
    /**
     * 杀人，反劫镖
	 * @param familyId
	 * @param selfRoleId
	 * @param aimRoleId
	 */
    @AsyncInvocation
    void killRole(long familyId, long selfRoleId, long aimRoleId);
    
    /**
     * 每日重置
     */
    @AsyncInvocation
    public void dailyReset();

    /*
     * 战斗：Rpc回调部分
     */

    void rpcOnFightCreated(int mainServerId, String fightId, boolean isOk);

	void rpcOnFightEnd(int mainServerId, int fightServerId, String fightId, long loserId);

	/*
	 * 测试：
	 */
//	void createFight(FighterEntity attacker, FighterEntity defender, long attackerFamilyId, long defenderFamilyId);

}
