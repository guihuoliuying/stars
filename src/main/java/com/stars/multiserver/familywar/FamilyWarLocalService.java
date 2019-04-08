package com.stars.multiserver.familywar;

import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.services.Service;
import com.stars.services.family.main.userdata.FamilyMemberPo;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 因为该接口承担了多种功能，估计需要在标识上进行区分
 * Created by zhaowenshuo on 2016/11/22.
 */
public interface FamilyWarLocalService extends ActorService, Service {

    @AsyncInvocation
    void AsyncFihterEntity();

    /* 总的统一入口 */
    @AsyncInvocation
    void sendMainIcon2All(long countdown);

    @AsyncInvocation
    void sendMainIcon2Role(long roleId, boolean isMaster, long familyId);

    @AsyncInvocation
    void sendApplicationSheet(long familyId, long roleId);

    @AsyncInvocation
    void apply(long familyId, long roleId);

    @AsyncInvocation
    void cancelApply(long familyId, long roleId);

    @AsyncInvocation
    void confirmTeamSheet(long familyId, long roleId, Set<Long> teamSheet);

    @AsyncInvocation
    void sendFixtures(long familyId, long roleId);

    @AsyncInvocation
    void sendUpdatedFixtures(long familyId, long roleId);

    @AsyncInvocation
    void enterFight(long familyId, long roleId, FighterEntity fighterEntity);

    @AsyncInvocation
    void enterSafeScene(long roleId);

    @AsyncInvocation
    void enterSafeScene(long familyId, long roleId);

    @AsyncInvocation
    void getFixtures(boolean isStr, String text);

    @AsyncInvocation
    void cancelFight(long familyId, long roleId);

    @AsyncInvocation
    void sendPointsRank(long roleId, byte subtype);

    // 定时检查
    @AsyncInvocation
    void checkAndEndTimeout();

    // 发奖用到的
    @AsyncInvocation
    void sendPointsRankAward(int mainServerId, boolean isLocal, boolean isElite, Map<Long, Integer> rankMap);

    // 积分达标奖励
    @AsyncInvocation
    void viewMinPointsAward(long roleId);

    @AsyncInvocation
    void acquireMinPointsAward(long roleId, long acquirePoints);

    // 家族排名奖励
    @AsyncInvocation
    void sendFamilyRankAward(int mainServerId, String familyName, int rank, Map<Long, Integer> rankAwardMap);

    /**/
    @AsyncInvocation
    void start(int mainServerId);

    void startByDisaster(int mainServerId);

    @AsyncInvocation
    void end(int mainServerId);

    @AsyncInvocation
    void onNormalFightCreationSucceeded(int mainServerId, int fightServerId, String battleId, String fightId);

    // 通知修改creationTime
    @AsyncInvocation
    void onNormalFightStarted(int mainServerId, int fightServerId, String battleId, String fightId);

    @AsyncInvocation
    void onFightCreateFail(int mainServerId, int fightServerId, String battleId, String fightId, int warType);

    /* 开始各阶段赛事（四分之一决赛/二分之一决赛/决赛/34名决赛带 */
    @AsyncInvocation
    void generateFixtures();

    @AsyncInvocation
    void startQuarterFinals();

    @AsyncInvocation
    void endQuarterFinals();

    @AsyncInvocation
    void startSemiFinals();

    @AsyncInvocation
    void endSemiFinals();

    @AsyncInvocation
    void startFinal();

    @AsyncInvocation
    void endFinal();

    void sendAward_ResetPoints_NoticeMater(int step, long countdown);

    /* 报名精英战/确认精英战名单 */
    @AsyncInvocation
    void sendTeamSheetChangedEmail(int mainServerId, long familyId, Set<Long> addTeamSheet, Set<Long> delTeamSheet);

    /* 每场奖励 */
    @AsyncInvocation
    void sendEliteFightAward(int mainServerId, boolean isWin, int type, int count, String opponentFamilyName, Map<Long, Map<Integer, Integer>> fighterAwardMap);

    @AsyncInvocation
    void sendNormalFightAward(int mainServerId, Map<Long, Map<Integer, Integer>> fighterAwardMap);

    /* 战斗处理（跨服） */
    @AsyncInvocation
    void handleEliteFightDamage(int mainServerId, String battleId, String fightId, Map<String, HashMap<String, Integer>> damageMap);

    @AsyncInvocation
    void handleEliteFightDead(int mainServerId, String battleId, String fightId, Map<String, String> deadMap);

    @AsyncInvocation
    void handleNormalFightDamage(int mainServerId, String battleId, String fightId, Map<String, HashMap<String, Integer>> damageMap);

    @AsyncInvocation
    void handleNormalFightDead(int mainServerId, String battleId, String fightId, Map<String, String> deadMap);

    @AsyncInvocation
    void handleStageFightDead(int mainServerId, String battleId, String fightId, Map<String, String> deadMap);

    @AsyncInvocation
    void handleFighterQuit(int mainServerId, int fightServerId, String fightId, String battleId, long roleId, short type);

//    @AsyncInvocation
//    void handleFighterQuit(int mainServerId, String battleId, long roleId);

    /* 进入战斗的共用api */
    @AsyncInvocation
    void onPremittedToEnter(int mainServerId, int fightServerId, String fightId, byte camp, long roleId);

    @AsyncInvocation
    void onFighterAddingSucceeded(int mainServerId, int fightServerId, String battleId, String fightId, byte camp, long roleId);

    @AsyncInvocation
    void onNormalFighterAddingSucceeded(int mainServerId, int fightServerId, String battleId, String fightId, byte camp, long roleId);

    @AsyncInvocation
    void onClientPreloadFinished(int mainServerId, int fightServerId, String battleId, String fightId, long roleId);

    @AsyncInvocation
    void match();

    @AsyncInvocation
    void syncBattleFightUpdateInfo();

    @AsyncInvocation
    void modifyConnectorRoute(int mainServerId, long roleId, int fightServerId);

    @AsyncInvocation
    void revive(int mainServerId, int fightServerId, String battleId, String fightId, long roleId, byte reqType);

    @AsyncInvocation
    void handleRevive(String battleId, String fightId, String fighterUid);

    @AsyncInvocation
    void onStageFightCreationSucceeded(int mainServerId, int fightServerId, String battleId, String fightId);

    @AsyncInvocation
    void onStageFighterAddingSucceeded(int mainServerId, int fightServerId, String battleId, String fightId, Set<Long> entitySet);

    @AsyncInvocation
    void reqSupport(long roleId, long familyId);

    @AsyncInvocation
    void addSupport(long roleId, long familyId);

    @AsyncInvocation
    void addMember(long familyId, FamilyMemberPo memberPo, FighterEntity entity);

    @AsyncInvocation
    void delMember(long familyId, long roleId);
}
