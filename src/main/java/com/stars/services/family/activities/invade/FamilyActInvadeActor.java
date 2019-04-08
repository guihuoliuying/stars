package com.stars.services.family.activities.invade;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.modules.data.DataManager;
import com.stars.modules.familyactivities.invade.packet.ClientFamilyInvade;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.activities.ActConst;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.services.family.activities.invade.cache.FamilyActInvadeCache;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by liuyuheng on 2016/10/17.
 */
public class FamilyActInvadeActor extends ServiceActor implements FamilyActInvadeService {
    private Map<Long, FamilyActInvadeCache> familyInvadeMap = new HashMap<>();// <familyid, cache>

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd("familyActInvadeService", this);
        synchronized (FamilyActInvadeActor.class) {
            FamilyActInvadeFlow faiFlow = new FamilyActInvadeFlow();
            faiFlow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_INVADE));
        }
    }

    @Override
    public void printState() {

    }

    @Override
    public void start() {
        familyInvadeMap = new HashMap<>();
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.FamilyActInvade, new InvadeTask(), 0, 1, TimeUnit.SECONDS);
    }

    // 活动时间结束
    @Override
    public void timeOver() {
        // 停止刷怪线程
        SchedulerManager.shutDownNow(ExcutorKey.FamilyActInvade);
        // 如果还有没触发的怪,需要干掉
        for (FamilyActInvadeCache familyActInvadeCache : familyInvadeMap.values()) {
            familyActInvadeCache.clearAvailableNpc();
        }
    }

    @Override
    public void end() {
        familyInvadeMap.clear();
    }

    @Override
    public void addUpdateMember(long familyId, BaseTeamMember member) {
        if (familyId <= 0)
            return;
        FamilyActInvadeCache cache = familyInvadeMap.get(familyId);
        if (cache == null) {
            cache = new FamilyActInvadeCache(familyId);
            familyInvadeMap.put(familyId, cache);
            // 有成员参与,锁定家族
            ServiceHelper.familyMainService().lockFamily(familyId);
        }
        cache.addUpdateMember(member);
    }

    @Override
    public void removeMember(long familyId, long roleId) {
        if (!familyInvadeMap.containsKey(familyId))
            return;
        familyInvadeMap.get(familyId).removeMember(roleId);
    }

    @Override
    public boolean isMemberIn(long familyId, long roleId) {
        if (!familyInvadeMap.containsKey(familyId))
            return false;
        return familyInvadeMap.get(familyId).isMemberIn(roleId);
    }

    // 战斗结束,更新npc状态
    @Override
    public void challengeFinish(long familyId, int monsterNpcUId, byte result, Map<String, Integer> damageMap, int teamId) {
        if (!familyInvadeMap.containsKey(familyId))
            return;
        familyInvadeMap.get(familyId).challengeFinish(monsterNpcUId, result, damageMap, teamId);
    }

    @Override
    public void reqMonsterNpc(long familyId, long roleId) {
        if (!familyInvadeMap.containsKey(familyId))
            return;
        familyInvadeMap.get(familyId).reqMonsterNpc(roleId);
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "请求家族入侵怪物NPC", true));
        }
    }

    @Override
    public void triggerFight(long familyId, long roleId, int monsterNpcId, float curPosX, float curPosZ) {
        if (!familyInvadeMap.containsKey(familyId))
            return;
        familyInvadeMap.get(familyId).enterFight(roleId, monsterNpcId, curPosX, curPosZ);
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "进入家族入侵", true));
        }
    }

    @Override
    public void reqRankList(long familyId, long roleId) {
        if (!familyInvadeMap.containsKey(familyId))
            return;
        familyInvadeMap.get(familyId).sendRankList(roleId);
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "请求家族入侵排行榜", true));
        }
    }

    @Override
    public void receiveFightPacket(long familyId, long roleId, PlayerPacket packet) {
        FamilyActInvadeCache invadeCache = familyInvadeMap.get(familyId);
        if (invadeCache == null) {
            return;
        }
        invadeCache.receiveFightPacket(roleId, packet);
    }

    @Override
    public void quitFromFight(long familyId, long roleId) {
        FamilyActInvadeCache invadeCache = familyInvadeMap.get(familyId);
        if (invadeCache == null) {
            return;
        }
        invadeCache.quitFromFight(roleId);
        ClientFamilyInvade packet = new ClientFamilyInvade(ClientFamilyInvade.BACK_TO_CITY);
        PlayerUtil.send(roleId, packet);
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(roleId, new SpecialAccountEvent(roleId, "退出家族入侵", true));
        }
    }

    class InvadeTask implements Runnable {

        @Override
        public void run() {
            for (FamilyActInvadeCache invadeCache : familyInvadeMap.values()) {
                invadeCache.spawnMonsterNpc();
            }
        }
    }
}