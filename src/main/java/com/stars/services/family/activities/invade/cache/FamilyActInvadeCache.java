package com.stars.services.family.activities.invade.cache;

import com.stars.core.attr.FormularUtils;
import com.stars.core.event.Event;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.baseteam.BaseTeamManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.familyactivities.invade.FamilyInvadeManager;
import com.stars.modules.familyactivities.invade.event.FamilyInvadeAwardBoxEvent;
import com.stars.modules.familyactivities.invade.packet.ClientFamilyInvade;
import com.stars.modules.familyactivities.invade.prodata.FamilyInvadeVo;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.imp.fight.FamilyActInvadeScene;
import com.stars.modules.scene.prodata.NpcInfoVo;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.services.family.activities.invade.FamilyActInvadeConstant;
import com.stars.services.family.activities.invade.FamilyActInvadeFlow;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liuyuheng on 2016/10/18.
 */
public class FamilyActInvadeCache {
    // 家族Id
    private long familyId;
    // 刷怪npc序列,生成唯一Id
    private AtomicInteger spawnNpcSeq;
    // 刷怪npc状态,已死亡从这里移除
    private Map<Integer, MonsterNpcCache> monsterTrigger;
    // 玩家数据池
    private Map<Long, BaseTeamMember> roleMemberMap;
    // 伤害排行榜
    private List<InvadeDamageCache> rankList;
    // 伤害缓存
    private Map<Long, InvadeDamageCache> damageCacheMap;
    // 战斗场景,<teamId, FamilyActInvadeScene>
    private Map<Integer, FamilyActInvadeScene> fightSceneMap;

    public FamilyActInvadeCache(long familyId) {
        this.familyId = familyId;
        spawnNpcSeq = new AtomicInteger(0);
        monsterTrigger = new ConcurrentHashMap<>();
        roleMemberMap = new ConcurrentHashMap<>();
        rankList = new LinkedList<>();
        damageCacheMap = new HashMap<>();
        fightSceneMap = new HashMap<>();
    }

    /**
     * 新增/更新玩家数据
     *
     * @param member
     */
    public void addUpdateMember(BaseTeamMember member) {
        roleMemberMap.put(member.getRoleId(), member);
    }

    /**
     * 移除玩家数据
     *
     * @param roleId
     */
    public void removeMember(long roleId) {
        quitFromFight(roleId);
        roleMemberMap.remove(roleId);
    }

    public boolean isMemberIn(long roleId) {
        return roleMemberMap.containsKey(roleId);
    }

    /**
     * 请求存在的怪物npc
     *
     * @param roleId
     */
    public void reqMonsterNpc(long roleId) {
        ClientFamilyInvade packet = new ClientFamilyInvade(ClientFamilyInvade.SPAWN_MONSTER_NPC);
        packet.setMonsterNpcCacheList(monsterTrigger.values());
        PlayerUtil.send(roleId, packet);
    }

    /**
     * 触发进入战斗
     *
     * @param triggerRoleId
     * @param monsterNpcUId
     * @param curPosX
     * @param curPosZ
     */
    public void enterFight(long triggerRoleId, int monsterNpcUId, float curPosX, float curPosZ) {
        MonsterNpcCache npcCache = monsterTrigger.get(monsterNpcUId);
        // 不是可挑战状态
        if (npcCache == null || npcCache.getStatus() != FamilyActInvadeConstant.NPC_AVAILABLE) {
            PlayerUtil.send(triggerRoleId, new ClientText("familyinvade_tips_enemyfighting"));
            return;
        }
        // 不在坐标范围内
        NpcInfoVo npcInfoVo = SceneManager.getNpcVo(npcCache.getNpcId());
        if (!FormularUtils.isPointInCircle(npcCache.getPosX() / 10f, npcCache.getPosZ() / 10f, npcInfoVo.getRange() / 10f,
                curPosX, curPosZ)) {
            PlayerUtil.send(triggerRoleId, new ClientText("角色不在触发范围内"));
            return;
        }
        // 没有队伍,则创建一个
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(triggerRoleId);
        if (team == null) {
            team = ServiceHelper.baseTeamService().createTeamSync(roleMemberMap.get(triggerRoleId),
                    BaseTeamManager.TEAM_TYPE_FAMILYINVADE, FamilyInvadeManager.minTeamCount,
                    FamilyInvadeManager.minTeamCount, 0);
        }
        // 队伍战斗中
        if (team.isFight()) {
            return;
        }
        FamilyInvadeVo invadeVo = FamilyInvadeManager.getInvadeVo(team.getAverageLevel(),
                team.getMembers().size() == 1 ? FamilyActInvadeConstant.TEAM_SINGLE : FamilyActInvadeConstant.TEAM_MULTI,
                npcCache.getType());
        if (invadeVo == null) {
            return;
        }
        FamilyActInvadeScene invadeScene = (FamilyActInvadeScene) SceneManager.newScene(SceneManager.SCENETYPE_FAMILY_INVADE);
        fightSceneMap.put(team.getTeamId(), invadeScene);
        if (!invadeScene.canEnter(null, invadeVo)) {
            return;
        }
        invadeScene.setFamilyId(familyId);
        invadeScene.setTeamId(team.getTeamId());
        invadeScene.addTeamMemberFighter(team.getMembers().values());
        invadeScene.enter(null, monsterNpcUId);
        // 修改npc状态
        npcCache.setStatus(FamilyActInvadeConstant.NPC_CHALLENGING);
        // 修改队伍状态
        team.setFight(Boolean.TRUE);
        // update to client
        ClientFamilyInvade packet = new ClientFamilyInvade(ClientFamilyInvade.UPDATE_MONSTER_NPC);
        packet.setMonsterNpcStatus(monsterNpcUId, FamilyActInvadeConstant.NPC_CHALLENGING);
        sendPacketToAll(packet, -1);
    }

    /**
     * 刷出怪物npc
     */
    public void spawnMonsterNpc() {
        int normalCount = 0;
        int eliteCount = 0;
        for (MonsterNpcCache npcCache : monsterTrigger.values()) {
            if (npcCache.getType() == FamilyActInvadeConstant.NORMAL_MONSTER) {
                normalCount++;
            } else if (npcCache.getType() == FamilyActInvadeConstant.ELITE_MONSTER) {
                eliteCount++;
            }
        }
        // 小怪
        List<MonsterNpcCache> list = executeSpawn(FamilyActInvadeConstant.NORMAL_MONSTER, normalCount);
        // 精英怪
        list.addAll(executeSpawn(FamilyActInvadeConstant.ELITE_MONSTER, eliteCount));
        if (list.isEmpty())
            return;
        // update to client
        ClientFamilyInvade packet = new ClientFamilyInvade(ClientFamilyInvade.SPAWN_MONSTER_NPC);
        packet.setMonsterNpcCacheList(list);
        sendPacketToAll(packet, -1);
    }

    /**
     * 挑战结束,更新npc状态,更新伤害排行榜
     *
     * @param monsterNpcUId
     * @param result
     * @param damageMap
     */
    public void challengeFinish(int monsterNpcUId, byte result, Map<String, Integer> damageMap, int teamId) {
        if (!monsterTrigger.containsKey(monsterNpcUId))
            return;
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(teamId);
        if (team !=null) {
            // 销毁scene
            fightSceneMap.remove(team.getTeamId());
            team.setFight(Boolean.FALSE);
        }
        byte status;
        // 挑战失败 && 活动进行中,才将状态置为可触发
        if (result == SceneManager.STAGE_FAIL && FamilyActInvadeFlow.isStarted()) {
            monsterTrigger.get(monsterNpcUId).setStatus(FamilyActInvadeConstant.NPC_AVAILABLE);
            status = FamilyActInvadeConstant.NPC_AVAILABLE;
        } else {
            monsterTrigger.remove(monsterNpcUId);
            status = FamilyActInvadeConstant.NPC_DEAD;
        }
        // 挑战胜利才将伤害加到排行榜
        if (!damageMap.isEmpty() && result == SceneManager.STAGE_VICTORY) {
            addDamageToRank(damageMap);
        }
        // 更新npc状态
        ClientFamilyInvade packet = new ClientFamilyInvade(ClientFamilyInvade.UPDATE_MONSTER_NPC);
        packet.setMonsterNpcStatus(monsterNpcUId, status);
        sendPacketToAll(packet, -1);
        finishDeal();
    }

    /**
     * 活动结束处理
     * npc全部移除 && 活动时间结束
     * 进行排行榜发奖;刷宝箱;解散所有队伍;解锁家族
     */
    public void finishDeal() {
        if (isMonsterNpcAllClear() && !FamilyActInvadeFlow.isStarted()) {
            // 解散所有队伍
            ServiceHelper.baseTeamService().disbandTeamByTeamtype(BaseTeamManager.TEAM_TYPE_FAMILYINVADE);
            // 刷宝箱
            spawnAwardBox();
            // 排行榜发奖
            rankReward();
            ServiceHelper.familyMainService().unlockFamily(familyId);
        }
    }

    /**
     * 请求排行榜
     *
     * @param roleId
     */
    public void sendRankList(long roleId) {
        List<InvadeDamageCache> list = new LinkedList<>();
        for (int i = 0; i < FamilyActInvadeConstant.RANK_SHOW_MAX; i++) {
            if (i >= rankList.size()) {
                break;
            }
            InvadeDamageCache cache = rankList.get(i);
            cache.setRank(i + 1);
            list.add(cache);
        }
        // 最后加上自己
        InvadeDamageCache selfCache = damageCacheMap.get(roleId);
        if (selfCache == null) {
            String roleName;
            if (!roleMemberMap.containsKey(roleId)) {
                RoleSummaryComponent rsc = (RoleSummaryComponent) ServiceHelper.summaryService().
                        getSummaryComponent(roleId, "role");
                roleName = rsc.getName();
            } else {
                roleName = roleMemberMap.get(roleId).getName();
            }
            selfCache = new InvadeDamageCache(roleId, roleName, 0);
        } else {
            selfCache.setRank(rankList.indexOf(selfCache) + 1);
        }
        list.add(selfCache);
        // send to client
        ClientFamilyInvade packet = new ClientFamilyInvade(ClientFamilyInvade.RANK_LIST);
        packet.setDamageRankList(list);
        PlayerUtil.send(roleId, packet);
    }

    /**
     * 清除剩余可触发的npc
     */
    public void clearAvailableNpc() {
        Map<Integer, Byte> changeMap = new HashMap<>();
        for (MonsterNpcCache npcCache : monsterTrigger.values()) {
            if (npcCache.getStatus() == FamilyActInvadeConstant.NPC_AVAILABLE) {
                changeMap.put(npcCache.getNpcId(), FamilyActInvadeConstant.NPC_DEAD);
                monsterTrigger.remove(npcCache.getUniqueId());
            }
        }
        ClientFamilyInvade packet = new ClientFamilyInvade(ClientFamilyInvade.UPDATE_MONSTER_NPC);
        packet.setMonsterNpcStatus(changeMap);
        sendPacketToAll(packet, -1);
        finishDeal();
    }

    public void receiveFightPacket(long roleId, PlayerPacket packet) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(roleId);
        if (team == null)
            return;
        if (!fightSceneMap.containsKey(team.getTeamId()))
            return;
        fightSceneMap.get(team.getTeamId()).receivePacket(null, packet);
    }

    /**
     * 战斗中退出
     *
     * @param roleId
     */
    public void quitFromFight(long roleId) {
        // 有队伍,先退队
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(roleId);
        if (team != null) {
            // 队伍正在战斗
            FamilyActInvadeScene familyActInvadeScene = fightSceneMap.get(team.getTeamId());
            if (familyActInvadeScene != null && familyActInvadeScene.stageStatus == SceneManager.STAGE_PROCEEDING) {
                familyActInvadeScene.exit(roleId);
                if (familyActInvadeScene.hasNoPlayer()) {
                    familyActInvadeScene.stageStatus = SceneManager.STAGE_FAIL;
                    challengeFinish(familyActInvadeScene.monsterNpcUId, SceneManager.STAGE_FAIL,
                            familyActInvadeScene.damageMap, familyActInvadeScene.teamId);
                }
            }
            ServiceHelper.baseTeamService().leaveTeam(roleId);
        }
    }

    /**
     * 伤害加到排行榜
     *
     * @param damageMap
     */
    private void addDamageToRank(Map<String, Integer> damageMap) {
        for (Map.Entry<String, Integer> entry : damageMap.entrySet()) {
            long roleId = Long.valueOf(entry.getKey());
            int damage = entry.getValue();
            InvadeDamageCache damageCache = damageCacheMap.get(roleId);
            if (damageCache == null) {
                damageCache = new InvadeDamageCache(roleId, roleMemberMap.get(roleId).getName(), damage);
                damageCacheMap.put(roleId, damageCache);
                rankList.add(damageCache);
            } else {
                damageCache.setDamage(damageCache.getDamage() + damage);
            }
        }
        Collections.sort(rankList);
    }

    /**
     * 排行榜发奖
     */
    private void rankReward() {
        if (!isMonsterNpcAllClear())
            return;
        Collections.sort(rankList);
        for (int index = 1; index <= rankList.size(); index++) {
            InvadeDamageCache cache = rankList.get(index - 1);
            if (index > FamilyInvadeManager.rewardRankMax) {
                break;
            }
            for (int[] rankAward : FamilyInvadeManager.rankGroup) {
                if (rankAward[0] <= index && index <= rankAward[1]) {
                    ServiceHelper.emailService().sendToSingle(
                            cache.getRoleId(), FamilyInvadeManager.emailTemplateId, Long.valueOf(cache.getRank()),
                            "家族邮件", FamilyInvadeManager.getRankAward(rankAward[2]), String.valueOf(index));
                    break;
                }
            }
        }
    }

    /**
     * npc全部死亡
     *
     * @return
     */
    private boolean isMonsterNpcAllClear() {
        return monsterTrigger.size() == 0;
    }

    /**
     * 刷宝箱
     */
    private void spawnAwardBox() {
        if (!isMonsterNpcAllClear())
            return;
        Map<String, AwardBoxCache> boxMap = new HashMap<>();
        int boxNum = FamilyInvadeManager.awardBoxNum;
        long curTimestamp = System.currentTimeMillis();
        for (int i = 0; i < boxNum; i++) {
            AwardBoxCache boxCache = new AwardBoxCache("box" + i, FamilyInvadeManager.awardBoxNpcId,
                    FamilyInvadeManager.getPosition(), curTimestamp);
            boxMap.put(boxCache.getAwardBoxUId(), boxCache);
        }
        // send to member
        sendEventToAll(new FamilyInvadeAwardBoxEvent(boxMap), -1);
    }

    /**
     * 执行刷npc逻辑
     *
     * @param monsterType
     * @param aliveMonsterNum
     * @return
     */
    private List<MonsterNpcCache> executeSpawn(byte monsterType, int aliveMonsterNum) {
        List<MonsterNpcCache> list = new LinkedList<>();
        int count = calSpawnMonsterNum(monsterType, aliveMonsterNum);
        if (count == 0)
            return list;
        int npcId = 0;
        if (monsterType == FamilyActInvadeConstant.NORMAL_MONSTER) {
            npcId = FamilyInvadeManager.normalMonsterNpcId;
        }
        if (monsterType == FamilyActInvadeConstant.ELITE_MONSTER) {
            npcId = FamilyInvadeManager.eliteMonsterNpcId;
        }
        for (int i = 0; i < count; i++) {
            int uniqueId = spawnNpcSeq.incrementAndGet();
            MonsterNpcCache monsterNpc = new MonsterNpcCache(uniqueId, npcId, monsterType,
                    FamilyInvadeManager.getPosition(), FamilyInvadeManager.getRotation());
            monsterTrigger.put(uniqueId, monsterNpc);
            list.add(monsterNpc);
        }
        return list;
    }

    /**
     * 计算刷怪数量
     *
     * @param monsterType
     * @param aliveMonsterNum
     * @return
     */
    private int calSpawnMonsterNum(byte monsterType, int aliveMonsterNum) {
        int curMemberCount = roleMemberMap.size();
        int spawnCoef = 0;// 刷怪系数
        if (monsterType == FamilyActInvadeConstant.NORMAL_MONSTER) {
            spawnCoef = FamilyInvadeManager.normalMonsterSpawnCoef;
        }
        if (monsterType == FamilyActInvadeConstant.ELITE_MONSTER) {
            spawnCoef = FamilyInvadeManager.eliteMonsterSpawnCoef;
        }
        int canSpawnNum = (int) Math.ceil(curMemberCount * spawnCoef / 100.0);
        return Math.max(0, canSpawnNum - aliveMonsterNum);
    }

    /**
     * 给活动中的所有人发包
     *
     * @param packet
     * @param exception
     */
    private void sendPacketToAll(Packet packet, int exception) {
        if (packet == null) {
            return;
        }
        for (long roleId : roleMemberMap.keySet()) {
            if (roleId == exception)
                continue;
            PlayerUtil.send(roleId, packet);
        }
    }

    /**
     * 给活动中的所有人通知事件
     *
     * @param event
     * @param exception
     */
    private void sendEventToAll(Event event, long exception) {
        if (event == null)
            return;
        for (long roleId : roleMemberMap.keySet()) {
            if (roleId == exception)
                continue;
            ServiceHelper.roleService().notice(roleId, event);
        }
    }
}
