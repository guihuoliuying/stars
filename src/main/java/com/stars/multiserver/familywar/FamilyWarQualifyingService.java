package com.stars.multiserver.familywar;

import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.multiserver.familywar.knockout.KnockoutFamilyInfo;
import com.stars.multiserver.familywar.remote.FamilyWarRemoteFamily;
import com.stars.services.Service;
import com.stars.services.family.main.userdata.FamilyMemberPo;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenkeyu on 2017-05-18.
 */
public interface FamilyWarQualifyingService extends ActorService, Service {

    @AsyncInvocation
    void registerFamilyWarServer(int serverId, int comFrom);

    @AsyncInvocation
    void qualifyFamilyWarServer(int serverId, int comFrom);

    @AsyncInvocation
    void containFamily(int serverId, int fromServerId, long familyId, long roleId);

    @AsyncInvocation
    void generateFamilyData(int serverId, int fromServerId, List<AbstractRankPo> familyList);

    @AsyncInvocation
    void onRankFamilyData(int serverId, int fromServerId, List<KnockoutFamilyInfo> infoList);

    @AsyncInvocation
    void startBattle(int serverId, int battleType);

    @AsyncInvocation
    void endBattle(int serverId, int battleType);

    @AsyncInvocation
    void match(int serverId);

    @AsyncInvocation
    void updateIconText(int serverId, String text);

    @AsyncInvocation
    void startQualifying(int serverId);

    @AsyncInvocation
    void enterSafeScene(int serverId, int fromServerId, long roleId);

    @AsyncInvocation
    void enterSafeScene(int serverId, int fromServerId, long familyId, long roleId);

    @AsyncInvocation
    void enterFight(int serverId, int fromServerId, long familyId, long roleId, FighterEntity fighterEntity);

    @AsyncInvocation
    void cancelFight(int serverId, int fromServerId, long familyId, long roleId);

    @AsyncInvocation
    void sendMainIcon2All(int serverId, long countdown);

    @AsyncInvocation
    void sendMainIcon2Role(int serverId, int fromServerId, long roleId, boolean isMaster, long familyId);

    @AsyncInvocation
    void onPremittedToEnter(int mainServerId, int fightServerId, String fightId, byte camp, long roleId);

    @AsyncInvocation
    void onFighterAddingSucceeded(int serverId, int mainServerId, int fightServerId, String battleId, String fightId, byte camp, long roleId);

    @AsyncInvocation
    void onNormalFighterAddingSucceeded(int serverId, int mainServerId, int fightServerId, String battleId, String fightId, byte camp, long roleId);

    @AsyncInvocation
    void modifyConnectorRoute(int serverId, int mainServerId, long roleId, int fightServerId);

    @AsyncInvocation
    void handleFighterQuit(int serverId, int mainServerId, int fightServerId, String fightId, String battleId, long roleId, short type);

    @AsyncInvocation
    void onClientPreloadFinished(int serverId, int mainServerId, int fightServerId, String battleId, String fightId, long roleId);

    @AsyncInvocation
    void handleEliteFightDamage(int serverId, int mainServerId, String battleId, String fightId, Map<String, HashMap<String, Integer>> damageMap);

    @AsyncInvocation
    void handleEliteFightDead(int serverId, int mainServerId, String battleId, String fightId, Map<String, String> deadMap);

    @AsyncInvocation
    void handleNormalFightDamage(int serverId, int mainServerId, String battleId, String fightId, Map<String, HashMap<String, Integer>> damageMap);

    @AsyncInvocation
    void handleNormalFightDead(int serverId, int mainServerId, String battleId, String fightId, Map<String, String> deadMap);

    @AsyncInvocation
    void handleStageFightDead(int serverId, int mainServerId, String battleId, String fightId, Map<String, String> deadMap);

    @AsyncInvocation
    void revive(int serverId, int mainServerId, int fightServerId, String battleId, String fightId, long roleId, byte reqType);

    @AsyncInvocation
    void handleRevive(int serverId, String battleId, String fightId, String fighterUid);

    @AsyncInvocation
    void onStageFightCreationSucceeded(int serverId, int mainServerId, int fightServerId, String battleId, String fightId);

    @AsyncInvocation
    void onStageFighterAddingSucceeded(int serverId, int mainServerId, int fightServerId, String battleId, String fightId, Set<Long> entitySet);

    @AsyncInvocation
    void onNormalFightCreationSucceeded(int serverId, int mainServerId, int fightServerId, String battleId, String fightId);

    // 通知修改creationTime
    @AsyncInvocation
    void onNormalFightStarted(int serverId, int mainServerId, int fightServerId, String battleId, String fightId);

    @AsyncInvocation
    void checkAndEndTimeout();

    @AsyncInvocation
    void sendAward_ResetPoints_NoticeMater(int step, long countdown);

    // 发奖用到的
    @AsyncInvocation
    void sendPointsRankAward(int serverId, int mainServerId, boolean isElite, Map<Long, Integer> rankMap);

    // 家族排名奖励
    @AsyncInvocation
    void sendFamilyRankAward(int serverId, int mainServerId, String familyName, int rank, Map<Long, Integer> rankAwardMap);

    @AsyncInvocation
    void viewMinPointsAward(int serverId, int fromServerId, long roleId);

    @AsyncInvocation
    void acquireMinPointsAward(int serverId, int fromServerId, long roleId, long points);

    @AsyncInvocation
    void sendPointsRank(int serverId, int fromServerId, long roleId, byte subtype);

    @AsyncInvocation
    void reqSupport(int serverId, int fromServerId, long roleId, long familyId);

    @AsyncInvocation
    void addSupport(int serverId, int fromServerId, long roleId, long familyId);

    @AsyncInvocation
    void sendApplicationSheet(int serverId, int fromServerId, long familyId, long roleId);

    @AsyncInvocation
    void apply(int serverId, int fromServerId, long familyId, long roleId);

    @AsyncInvocation
    void cancelApply(int serverId, int fromServerId, long familyId, long roleId);

    @AsyncInvocation
    void confirmTeamSheet(int serverId, int fromServerId, long familyId, long roleId, Set<Long> teamSheet);

    @AsyncInvocation
    void sendFixtures(int serverId, int fromServerId, long familyId, long roleId, long fightScore);

    @AsyncInvocation
    void sendUpdatedFixtures(int serverId, int fromServerId, long familyId, long roleId);

    /* 报名精英战/确认精英战名单 */
    @AsyncInvocation
    void sendTeamSheetChangedEmail(int serverId, int mainServerId, long familyId, Set<Long> addTeamSheet, Set<Long> delTeamSheet);

    @AsyncInvocation
    void sendFamilyRankObj(int serverId, int fromServerId, long familyId, long roleId);

    @AsyncInvocation
    void updateFixtureCache(int serverId, int battleType, int groupId, long winnerFamilyId);

    @AsyncInvocation
    void generateRemoteQulifications(int serverId, List<FamilyWarRemoteFamily> familyList);

    @AsyncInvocation
    void AsyncFihterEntityAndLockFamily(int serverId);

    @AsyncInvocation
    void updateFighterEntity(int serverId, Map<Long, FighterEntity> entityMap);

    @AsyncInvocation
    void addMember(int serverId, long familyId, FamilyMemberPo memberPo, FighterEntity entity);

    @AsyncInvocation
    void delMember(int serverId, long familyId, long roleId);


    @AsyncInvocation
    void roleState(int serverId, Set<Long> roleIds, boolean isOnline);

    /* 每场奖励 */
    @AsyncInvocation
    void sendEliteFightAward(int serverId, int mainServerId, boolean isWin, int type, int count, String opponentFamilyName, Map<Long, Map<Integer, Integer>> fighterAwardMap);

    @AsyncInvocation
    void sendNormalFightAward(int serverId, int mainServerId, Map<Long, Map<Integer, Integer>> fighterAwardMap);

    @AsyncInvocation
    void updateFlowInfo(int serverId, int warType, byte warState);

    @AsyncInvocation
    void resetDataBase(int serverId);

    @AsyncInvocation
    void startByDisaster(int serverId);

    /**
     * 调试用的，用来同步状态
     *
     * @param serverId
     * @param battleType
     * @param generalFlow
     * @param subFlow
     */
    @AsyncInvocation
    void AsyncState(int serverId, int battleType, int generalFlow, int subFlow, boolean isRunning);

    @AsyncInvocation
    void odd(int serverId, int odd);
}
