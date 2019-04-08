package com.stars.multiserver.camp;

import com.stars.modules.camp.event.CampCityChangeEvent;
import com.stars.modules.camp.event.CampLevelUpEvent;
import com.stars.modules.camp.event.RoleInfoChangeEvent;
import com.stars.multiserver.camp.pojo.CampPlayerImageData;
import com.stars.multiserver.camp.pojo.CampTypeScale;
import com.stars.multiserver.camp.usrdata.AllServerCampPo;
import com.stars.multiserver.camp.usrdata.RareOfficerRolePo;
import com.stars.services.Service;
import com.stars.services.rank.userdata.CampRoleReputationRankPo;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by huwenjun on 2017/6/26.
 */
public interface CampLocalMainService extends Service, ActorService {
    @AsyncInvocation
    void connectAndShareRoleNum();

    @AsyncInvocation
    void joinCamp(Long roleId, Integer campType);


    Integer getRandomCampType();


    @AsyncInvocation
    void save();

    @AsyncInvocation
    void pushAllServerCampRoleNum(int serverId, Map<Integer, AllServerCampPo> allServerCampMap, CampTypeScale campTypeScale);


    @AsyncInvocation
    void donateCampProsperous(long roleId, int campType, int prosperousNum);

    @AsyncInvocation
    void handleCampLevelUp(int serverId, CampLevelUpEvent campLevelUpEvent);


    AllServerCampPo getAllServerCampByCampType(int campType);

    @AsyncInvocation
    void updateReputation(CampRoleReputationRankPo newRoleReputationRankPo);

    @AsyncInvocation
    void shareLocalReputationRank();

    @AsyncInvocation
    void pushAllServerRareOfficerRoles(int serverId, Map<Integer, Map<Long, RareOfficerRolePo>> currentRoundroleRareOfficerMap, Map<Integer, List<CampRoleReputationRankPo>> lastRoleReputationRankMap, boolean reset);


    CampTypeScale getCampTypeScale();

    int getAllCampRoleNum();

    RareOfficerRolePo getAllCityRareOfficerByRoleId(long roleId);


    List<RareOfficerRolePo> getRareOfficerListByCityId(int cityId);

    int getHighestRareOfficerCityId(int campType);

    @AsyncInvocation
    void handleChangeCity(CampCityChangeEvent campCityChangeEvent);

    @AsyncInvocation
    void pushAllServerRank(int serverId, Map<Integer, Map<Long, CampRoleReputationRankPo>> sortedCityRankMap);

    @AsyncInvocation
    void pushCampCityFightMap(int serverId, Map<Integer, Map<Long, CampPlayerImageData>> campCityFightMap,
                              Map<Integer, Integer> cityPlayerNumMap);

    @AsyncInvocation
    public void getPlayerImageData(int serverId, Set<Long> roleSet);

    @AsyncInvocation
    void getPlayerImageData(int serverId, long roleId);


    CampRoleReputationRankPo getRankByCityId(int cityId, long roleId);

    List<CampRoleReputationRankPo> getRankListByCityId(int cityId);

    Map<Integer, List<CampRoleReputationRankPo>> getCityRankListMap();

    Map<Integer, Integer> getCityPlayerNumMap();

    Map<Long, CampPlayerImageData> getCampCityFightMap(int campType);

    List<RareOfficerRolePo> getCurrentRoundRoleRareOfficerListByCityId(int cityId);

    Map<Integer, AllServerCampPo> getAllServerCampMap();



    @AsyncInvocation
    void handleRoleInfoChange(RoleInfoChangeEvent roleInfoChangeEvent);
}
