package com.stars.services.loottreasure;

import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.multiserver.LootTreasure.LTDamageRank;
import com.stars.multiserver.LootTreasure.LTDamageRankVo;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.List;
import java.util.Map;

/**
 * Created by panzhenfeng on 2016/10/10.
 */
public interface LootTreasureService extends Service, ActorService {
	@AsyncInvocation
    void requestAttend(FighterEntity fEntity, int jobId);
    

    @AsyncInvocation
    void setLootTreasureActivityState(boolean isStarting);

    List<LTDamageRankVo> getLtDamageRankVoLists(String lootSectionId);

    @AsyncInvocation
    void checkLootTreasureActivity(long roleId);

    long getStartActivityTimeStamp();

    long getEndActivityTimeStamp();

    boolean isActivityFlowValid();
    
    @AsyncInvocation
    void attendLTBack(int serverId,long roleId,byte flag);
    
    @AsyncInvocation
    void existFight(int serverId,long roleId);

    @AsyncInvocation
    void addClientLootTreasureRankList(int serverId, String lootSectionId, LTDamageRank ltDamageRank);

    @AsyncInvocation
    void setRoleAtLootSection(long roleId, String lootSectionId);

    String getLootSectionIdByRoleId(long roleId);

    @AsyncInvocation
    void sendAwardEmail(int serverId, int customType, long receiveRoleId, int templateId, long sendId, String sendName, Map<Integer, Integer> affixMap);
}
