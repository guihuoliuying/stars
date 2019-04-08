package com.stars.multiserver.camp;

import com.stars.modules.camp.event.CampCityChangeEvent;
import com.stars.modules.camp.event.RoleInfoChangeEvent;
import com.stars.multiserver.camp.pojo.CampPlayerImageData;
import com.stars.multiserver.camp.pojo.CampTypeScale;
import com.stars.services.Service;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/6/26.
 */
public interface CampRemoteMainService extends Service, ActorService {
    @AsyncInvocation
    void notifyAllServerUpdateCampData();

    @AsyncInvocation
    void caculateCampRoleNum();

    @AsyncInvocation
    void shareCampTypeRoleNum(int serverId, Map<Integer, Integer> campRoleNumMap, Integer fromServer);

    @AsyncInvocation
    void donateCampProsperous(Integer campServerId, Map<Integer, Integer> prosperousNumMap);

    CampTypeScale getCurrentScale();

    @AsyncInvocation
    void updateCampCityReputationRank(Integer campServerId, Map<Integer, List<AbstractRankPo>> map, Integer gameServerId);

    @AsyncInvocation
    void sort();

    @AsyncInvocation
    void notifyAllServerRareOfficer(boolean reset);

    @AsyncInvocation
    void grantRareOfficer();

    @AsyncInvocation
    void dailyProperousAdd();

    @AsyncInvocation
    void handleChangeCity(Integer campServerId, CampCityChangeEvent campCityChangeEvent);

    @AsyncInvocation
    void notityAllServerRank();

    @AsyncInvocation
    void addPlayerImageData(int serverId, CampPlayerImageData data);

    @AsyncInvocation
    void addPlayerImageData(int serverId, List<CampPlayerImageData> dataList);

    @AsyncInvocation
    void notifyAllServerCityData();


    @AsyncInvocation
    void remoteGm(int campServerId);

    @AsyncInvocation
    void handleRoleInfoChange(Integer campServerId, RoleInfoChangeEvent roleInfoChangeEvent, Integer gameServerId);

}
