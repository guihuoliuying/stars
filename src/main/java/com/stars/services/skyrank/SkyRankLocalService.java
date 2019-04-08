package com.stars.services.skyrank;

import com.stars.modules.skyrank.SkyRankScoreHandle;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.List;
import java.util.Map;

/**
 * 天梯排行榜（本服）
 *
 * @author xieyuejun
 */
public interface SkyRankLocalService extends Service, ActorService {
    @AsyncInvocation
    public void receiveRankData(int serverId, List<SkyRankShowData> skyRankList, Map<Long, Integer> skyRankKfFrankMap);

    @AsyncInvocation
    public void reqRoleScoreMsg(long roleId, SkyRankShowData myDefalutRank);

    @AsyncInvocation
    public void reqRankMsg(long roleId, SkyRankShowData myDefalutRank);

    @AsyncInvocation
    public void handleScoreChange(SkyRankScoreHandle scoreHandle);

    @AsyncInvocation
    public void gmHandle(long roleId, String[] args);

    @AsyncInvocation
    public void receiveRankAward(int serverId, List<SkyRankShowData> rankAwardList);

    @AsyncInvocation
    public void updateLocalSkyRankData(SkyRankShowData srd);

    @AsyncInvocation
    public void runUpdate();

    @AsyncInvocation
    public void dailyReset();

    @AsyncInvocation
    public void handleScoreEvent(int serverId, long roleId, short fightEvent, byte isWin);

    @AsyncInvocation
    public void receiveRankData(int serverId, long roleId, SkyRankShowData myRank, SkyRankShowData myDefalutRank);

    public int getSkyScore(long roleId, String name, int fightScore);
    
    @AsyncInvocation
    public void RoledailyReset(long roleId, String name, int fightScore);
    
    public int getDailyAward(long roleId, String name, int fightScore);
    
    public Object[] getDailyAwardState(long roleId, String name, int fightScore);

    @AsyncInvocation
    public void static_log();

    @AsyncInvocation
    void updateRoleName(long id, String name, int fightScore);

    @AsyncInvocation
    void checkRankGradeWhileLogin(long roleId, String roleName, int fightScore);
}
