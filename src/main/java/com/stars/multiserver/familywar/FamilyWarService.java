package com.stars.multiserver.familywar;

import com.stars.modules.familyactivities.war.packet.ClientFamilyWarMainIcon;
import com.stars.modules.familyactivities.war.packet.ui.ClientFamilyWarUiApply;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.PktAuxFamilyWarApplicant;
import com.stars.multiserver.familywar.data.FamilyWarFixture;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 家族战本服跨服公共业务
 *
 * @author zenghaofei
 */
public interface FamilyWarService extends ActorService, Service {

    @AsyncInvocation
    void updateFlowInfo(int serverId, int warType, byte warState);

    @AsyncInvocation
    void updateFamilyWarFixture(int serverId, int warType, Map<Integer, Long> fixFamily);

    @AsyncInvocation
    void getOnRankFamily(int serverId, int type, List<Long> familyIds);

    @AsyncInvocation
    void createFamilyInfo();

    FamilyWarFixture getFixture(int serverId, int warType);

    @AsyncInvocation
    void save();

    @AsyncInvocation
    void startQualifying(int serverId);

    @AsyncInvocation
    void setOptions(int serverId, int activityId, int flag, int countdown, String text);

    @AsyncInvocation
    void AsyncBattleType(int serverId, int battleType, int generalFlow, int subFlow, boolean isRunning);

    @AsyncInvocation
    void modifyConnectorRoute(int mainServerId, long roleId, int fightServerId);

    @AsyncInvocation
    void sendMainIcon(int serverId, ClientFamilyWarMainIcon mainIcon, HashSet<Long> familyList, Set<Long> failFamilyList);

    @AsyncInvocation
    void lockFamily(int serverId, long familyId);

    @AsyncInvocation
    void halfLockFamily(int serverId, long familyId);

    @AsyncInvocation
    void sendAward(int serverId, long roleId, short eventType, int emailTemplateId, Map<Integer, Integer> toolMap);

    @AsyncInvocation
    void sendEmailToSingle(int serverId, long roleId, int templateId, Long senderId, String senderName, Map<Integer, Integer> affixMap, String... params);

    @AsyncInvocation
    void sendApplicationSheet(int serverId, long roleId, Map<Long, PktAuxFamilyWarApplicant> applicantMap, ClientFamilyWarUiApply packet);

    @AsyncInvocation
    void chat(int serverId, String title, byte channel, long sender, long receiverId, String content, boolean hasObject);

    @AsyncInvocation
    void announce(int serverId, String message);

    @AsyncInvocation
    void unLockFamily(int serverId, List<Long> familyIds);

    @AsyncInvocation
    void AsyncBattle(int serverId, int battleType);

    @AsyncInvocation
    void startQualify(int serverId, int warType);

    @AsyncInvocation
    void startQualifyByDisaster(int serverId);

    @AsyncInvocation
    void startRemoteByDisaster(int serverId);

    @AsyncInvocation
    void changeServerState(int serverId, byte qualificationState);

    @AsyncInvocation
    void containFamily(int serverId, long familyId, long roleId, byte qualificationState);

    @AsyncInvocation
    void updateFighterEntity(int serverId, int warType, Set<Long> roleIds);

    @AsyncInvocation
    void roleOnline(long roleId);

    @AsyncInvocation
    void roleOffline(long roleId);

    @AsyncInvocation
    void SyncRoleStateToFamilyWarServer();

    @AsyncInvocation
    void dailyCheck();

    @AsyncInvocation
    void onCallMainServer(int serverId);

}
