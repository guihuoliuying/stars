package com.stars.services.skyrank;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.List;

/**
 * 
 * 跨服天梯排行榜
 * @author xieyuejun
 *
 */
public interface SkyRankKFService extends Service, ActorService {
    @AsyncInvocation
    public void updateSkyRankData(int serverId,List<SkyRankShowData> skyRankDataList);
    
    @AsyncInvocation
    public void updateSkyRankData(int serverId,SkyRankShowData skyRankData);
    
    @AsyncInvocation
    public void reqSkyRankData(int serverId,int fromServerId);
    
    @AsyncInvocation
	public void updateRank();
    
    @AsyncInvocation
	public void runUpdate();
    
    @AsyncInvocation
    public void getMyRankData(int serverId,long roleId,SkyRankShowData myRank);
}
