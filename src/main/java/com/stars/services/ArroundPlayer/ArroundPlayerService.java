package com.stars.services.ArroundPlayer;

import com.stars.modules.arroundPlayer.ArroundPlayer;
import com.stars.services.Service;
import com.stars.services.family.FamilyAuth;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ArroundPlayerService extends Service, ActorService {
    @AsyncInvocation
    void addArroundPlayer(ArroundPlayer p);

    @AsyncInvocation
    void removeArroundPlayer(String scenceId, long roleId);

    @AsyncInvocation
    void flushArroundPlayers(String scenceId, long roleId, Object sceneMsg);

    @AsyncInvocation
    void flush();

    @AsyncInvocation
    void updateBabyCurFashionId(String scenceId, long roleId, int curFashionId);

    @AsyncInvocation
    void updateDragonBallList(String scenceId, long roleId, List<String> dragonBallList);

    @AsyncInvocation
    void updateLevel(String scenceId, long roleId, short level);

    @AsyncInvocation
    void updatePosition(String scenceId, long roleId, int[] p);

    @AsyncInvocation
    void updateActiveRideId(String scenceId, long roleId, int activeRideId);

    @AsyncInvocation
    void updateCurFashionId(String scenceId, long roleId, int curFashionId);

    @AsyncInvocation
    public void updateCurTitleId(String sceneId, long roleId, int curTitleId);

    @AsyncInvocation
    public void updateCurVipLevel(String sceneId, long roleId, int curVipLevel);

    @AsyncInvocation
    void updateBabyFollow(String scenceId, long roleId, byte follow);

    @AsyncInvocation
    void updateCurDeityWeaponType(String sceneId, long roleId, byte deityweaponType);

    @AsyncInvocation
    void addFriendId(long roleId, Collection<Long> friendIdSet);

    @AsyncInvocation
    void delFriendId(long roleId, Collection<Long> friendIdSet);

    @AsyncInvocation
    void updateCoupleId(long roleId, long coupleId);

    @AsyncInvocation
    void updateFamilyAuth(long roleId, FamilyAuth auth);

    @AsyncInvocation
    void updateCurFashionCardId(String sceneId, long roleId, int curFashionCardId);

    /**
     * 根据场景ID返回周围玩家列表
     *
     * @param sceneId
     * @return
     */
    Map<Long, ArroundPlayer> getArroundPlayersBySceneId(String sceneId);

    @AsyncInvocation
    void updateRoleRename(long id, String safeStageId, String newName);
}
