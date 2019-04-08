package com.stars.multiserver.daily5v5;

import com.stars.multiserver.daily5v5.data.MatchingInfo;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.HashMap;
import java.util.Map;

public interface Daily5v5MatchService extends Service, ActorService{
	@AsyncInvocation
	public void matching(int serverId, MatchingInfo info);

	@AsyncInvocation
	public void cancelMatching(int serverId, int mainServerId, long roleId, boolean isOffline);
	
	@AsyncInvocation
	public void continueFighting(int serverId,  int mainServerId, long roleId);
	@AsyncInvocation
	public void checkContinue(int serverId,  int mainServerId, long roleId);
	
	public void checkMatching();
	
	public void checkTeamMatching();
	
	@AsyncInvocation
	public void gmHandler(int serverId, long roleId, String[] args);
	
	@AsyncInvocation
	public void checkFighitEndTimeOut();
	
	@AsyncInvocation
	public void endRemoveFight(String fightId);
	@AsyncInvocation
	public void activityStart();
	@AsyncInvocation
	public void activityEnd();
	
	/**
	 * 战斗RPC回调
	 */
	@AsyncInvocation
	public void rpcOnFightCreated(int serverId, String fightId, boolean isOk);
	
	@AsyncInvocation
	public void handleFightDamage(int serverId, String battleId, String fightId, Map<String, HashMap<String, Integer>> damageMap);

    @AsyncInvocation
    public void handleFightDead(int serverId, String battleId, String fightId, Map<String, String> deadMap);
    
    @AsyncInvocation
    public void handleFighterQuit(int serverId, String fightId, long roleId);
    
    @AsyncInvocation
    public void handleRevive(int serverId, String battleId, String fightId, String fighterUid);
    
    @AsyncInvocation
    public void handChangeConn(int serverId, String battleId, String fightId, String fighterUid);
    
    @AsyncInvocation
    public void handleTimeOut(int serverId, String fightId, HashMap<String, String> hpInfo);
    
    @AsyncInvocation
    public void removeFromFightingMap(long roleId);
    
    @AsyncInvocation
    public void onFighterAddingSucceeded(int serverId, int fightServerId, String fightId, long roleId);
    
    @AsyncInvocation
    public void handleClientPreloadFinished(int serverId, String fightId, long roleId);

    @AsyncInvocation
    public void handleUseBuff(int serverId, String fightId, long roleId, int effectId);

}
