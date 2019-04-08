package com.stars.multiserver.camp;

import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.imp.fight.CampCityFightScene;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

public interface CampCityFightService extends Service, ActorService{
	/**
	 * 添加 fightScene
	 * @param teamId
	 * @param scene
	 */
	@AsyncInvocation
	void addToSceneMap(int teamId, CampCityFightScene scene);
	
	/**
	 * 移除 fightScene
	 * @param teamId
	 */
	@AsyncInvocation
	void removeFightScene(int teamId);
	
	void removeMember(long roleId);
	
	/**
     * 副本中收到战斗相关包
     *
     * @param packet
     */
    @AsyncInvocation
	void receiveFightPacket(PlayerPacket packet);
	
	/**
     * 副本开始战斗
     *
     * @param roleId
     */
    @AsyncInvocation
    public void startFightTime(long roleId);
    
    /**
     * 发送可邀请好友信息
     * @param initiator
     * @param campType
     * @param scene
     */
    public void sendCanInviteList(long initiator, int campType, String scene);

}
