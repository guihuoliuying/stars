package com.stars.multiserver.daily5v5;

import com.stars.modules.daily5v5.packet.ClientDaily5v5;
import com.stars.multiserver.daily5v5.data.Daily5v5MatchingVo;
import com.stars.multiserver.daily5v5.data.MatchingInfo;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.List;

public interface Daily5v5Service extends Service, ActorService{
	
	@AsyncInvocation
	public void actOpen();
	
	@AsyncInvocation
	public void actEnt();
	
	@AsyncInvocation
	public void startMatching(MatchingInfo info);
	@AsyncInvocation
	public void cancelMatching(long roleId, boolean isOffline);
	
	@AsyncInvocation
	public void cancelMatchingResult(int serverId, long roleId);

	@AsyncInvocation
	public void continueFighting(long roleId);
	@AsyncInvocation
	public void checkContinue(long roleId);
	
	@AsyncInvocation
	public void finishMatching(int serverId, long roleId, List<Daily5v5MatchingVo> memberList,
			List<Daily5v5MatchingVo> enermyMemberList, int fightServerId, int matchingSuccessTime);

	@AsyncInvocation
	public void announce(int serverId, long roleId, String name, byte enermy);
	
	@AsyncInvocation
	public void finishFight(int serverId, long roleId, byte result, int time, ClientDaily5v5 packet);

	@AsyncInvocation
	public void announceTips();

	//控制匹配的gm
	public void gmHandler(long roleId, String[] args);

}
