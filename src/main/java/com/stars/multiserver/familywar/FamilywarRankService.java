package com.stars.multiserver.familywar;

import com.stars.services.Service;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.List;

/**
 * Created by chenkeyu on 2017-06-28.
 */
public interface FamilywarRankService extends ActorService, Service {

    @AsyncInvocation
    void connectServer(int serverId, int fromServerId);

    @AsyncInvocation
    void mainServerFamilyData(int serverId, int fromServerId, List<AbstractRankPo> familyList);

    @AsyncInvocation
    void updateTitle(int serverId, List<Long> familyIds, String title, int type);

    @AsyncInvocation
    void resetTitle(int serverId, boolean loadData);

    @AsyncInvocation
    void view(int serverId, int fromServerId, long familyId, long roleId);

    @AsyncInvocation
    void delete(int serverId, long familyId);

    @AsyncInvocation
    void debug(int serverId, int period, int size);
}
