package com.stars.services.marry;

import com.stars.services.Service;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.services.marry.userdata.Marry;
import com.stars.services.marry.userdata.MarryRole;
import com.stars.services.marry.userdata.MarryWedding;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhouyaohui on 2016/12/1.
 */
public interface MarryService extends ActorService, Service {

    @AsyncInvocation
    void playerOnline(MarryRole role);

    @AsyncInvocation
    void playerOffline(String key, long roleId);

    @AsyncInvocation
    void claim(long roleId, String claim, int reqLevel);

    @AsyncInvocation
    void claimList(long roleId, int index, int endIndex);

    @AsyncInvocation
    void profress(long roleId, long profressTarget, byte way);

    @AsyncInvocation
    void profressResponse(long roleId, long profressor, byte profressType);

    @AsyncInvocation
    void profressList(long roleId);

    @AsyncInvocation
    void appointment(long roleId, byte gender, byte reqType);

    @AsyncInvocation
    void appointmentResPonse(long roleId, byte b);

    @AsyncInvocation
    void check();

    @AsyncInvocation
    void appointmentLuxurious(long roleId, byte gender);

    @AsyncInvocation
    void getState(long roleId);

    @AsyncInvocation
    void save();

    @AsyncInvocation
    void breakMarry(long roleId, byte breakType);

    @AsyncInvocation
    void appointmentInfo(long roleId);

    @AsyncInvocation
    void toolSubCallback(long roleId, byte type, Object arg);

    @AsyncInvocation
    void marryInfo(long roleId);

    @AsyncInvocation
    void createTeam(long roleId, BaseTeamMember creator, int target);

    @AsyncInvocation
    void SyncMarryOther(long roleId);

    @AsyncInvocation
    void addDungeon(List<Long> roleId);

    @AsyncInvocation
    void SyncMarryScore(long roleId, int score);

    // 同步获取结婚实体,只做读（可能会有状态不一致，慎重）
    Marry getMarrySync(long roleId);

    MarryWedding getWeddingSync(String key);

    public ConcurrentMap<String, MarryWedding> getCurrentWeddingMapSync();

    int getRemainTeamDungeon(long roleId);

    @AsyncInvocation
    void sendCanInviteList(long roleId);

    @AsyncInvocation
    void weddingInfo(long roleId);

    @AsyncInvocation
    void enterWeddingScene(String key, long roleId, boolean login);

    @AsyncInvocation
    void exitWeddingScene(String key, long roleId);

    @AsyncInvocation
    void openCandy(long roleId, String position, int candyStamp, String key);

    @AsyncInvocation
    void fireworks(long roleId, String key);

    @AsyncInvocation
    void sendRedbag(long roleId, String key);

    @AsyncInvocation
    void getRedbag(long roleId, long senderId, String key);

    @AsyncInvocation
    void shipInfo(long roleId);

    @AsyncInvocation
    void sendShipDungeon(long roleId, int level, Map<Integer, Integer> awardMap);

    @AsyncInvocation
    void fight(long roleId);

    @AsyncInvocation
    void loginBreakCheck(long roleId);

    @AsyncInvocation
    void weddingActivityInfo(long roleId, String key);

    @AsyncInvocation
    void search(long roleId, String searchName);

    @AsyncInvocation
    void onDailyReset(long id);

    @AsyncInvocation
    void appointSceneCheckBack(long roleId, byte gender, byte reqType, boolean result);

    @AsyncInvocation
    void weddingList(long roleId, int startIndex, int endIndex);

    @AsyncInvocation
    void appointmentCheck(long roleId);

    void updateRoleName(Long id, String newName);
}
