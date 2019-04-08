package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.drop.DropUtil;
import com.stars.modules.elitedungeon.EliteDungeonManager;
import com.stars.modules.elitedungeon.event.EliteDungeonDropEvent;
import com.stars.modules.marry.MarryManager;
import com.stars.modules.marry.event.MarrySceneFinishEvent;
import com.stars.modules.marry.prodata.MarryBattleScoreVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.Damage;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientMarryBattleScore;
import com.stars.modules.scene.packet.ClientRoleRevive;
import com.stars.modules.scene.packet.ClientSpawnMonster;
import com.stars.modules.scene.packet.ServerAreaSpawnMonster;
import com.stars.modules.scene.packet.clientEnterFight.ClientEneterMarryBattle;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDungeon;
import com.stars.modules.scene.packet.fightSync.ClientSyncAttr;
import com.stars.modules.scene.packet.fightSync.ClientSyncOrder;
import com.stars.modules.scene.packet.fightSync.ServerFightDamage;
import com.stars.modules.scene.packet.fightSync.ServerSyncOrder;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.ReviveConfig;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.teamdungeon.TeamDungeonManager;
import com.stars.modules.teamdungeon.event.BackToCityFromTeamDungeonEvent;
import com.stars.modules.teamdungeon.prodata.TeamDungeonVo;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.util.LogUtil;
import com.stars.util.RandomUtil;
import com.stars.util.ServerLogConst;

import java.util.*;

import static com.stars.modules.scene.SceneManager.STAGE_PROCEEDING;
import static com.stars.modules.scene.SceneManager.getStageVo;


/**
 * Created by zhanghaizhen on 2017/05/20.
 */
public class MarryDungeonScene extends TeamDungeonScene {
    private TeamDungeonVo teamDungeonVo;
    private StageinfoVo stageinfoVo;
    private int remainReviveTimes; //剩余复活次数
    private int marryTeamId;
    public Map<String, Integer> marryBattleScoreMap;
    private Set<String> deadRobotUids = new HashSet<>();
    private boolean isForceOut = false;

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object vo) {
        TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo((int) vo);
        if (teamDungeonVo == null)
            return false;
        this.teamDungeonVo = teamDungeonVo;
        this.marryTeamId = (int) vo;
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        /* 初始化流程 */
        stageinfoVo = getStageVo(this.stageId);
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = STAGE_PROCEEDING;
        ReviveConfig reviveConfig = SceneManager.getReviveConfig(stageinfoVo.getStageType());
        if (reviveConfig == null) {
            remainReviveTimes = 0;
        } else {
            remainReviveTimes = reviveConfig.getLimitNum();
        }
        ClientEneterMarryBattle enterFight = new ClientEneterMarryBattle();
        enterFight.setStageId(stageId);
        enterFight.setFightType(SceneManager.SCENETYPE_MARRY_DUNGEON);
        enterFight.setTotalMarryBattleScore(stageinfoVo.getVictoryConMap().get(SceneManager.VITORY_CONDITION_MARRY_BATTLE_SCORE)); //胜利需要积分
        enterFight.setFailTime(stageinfoVo.getFailConMap().get(SceneManager.FAIL_CONDITION_TIME));
        /* 副本失败时间 */
        if (stageVo.getFailConMap().containsKey(SceneManager.FAIL_CONDITION_TIME)) {
            enterFight.setFailTime(stageVo.getFailConMap().get(SceneManager.FAIL_CONDITION_TIME));
        }

        /* 初始化怪物 */
        initMonsterData(moduleMap, enterFight, stageVo);
        enterFight.setFighterEntityList(entityMap.values());
        /* 动态阻挡数据 */
        initDynamicBlockData(enterFight, stageVo);
        sendPacketToTeamMembers(enterFight, -1);
//        requestSendClientEnterFight(moduleMap, enterFight, stageinfoVo);
        // 先发剧情播放记录
        //日常活动事件
        ServiceHelper.marryService().addDungeon(memberRoleIds);
        // 日志
//        ServerLogModule logModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
//        logModule.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_33.getThemeId(), logModule.makeJuci(),
//                ThemeType.ACTIVITY_33.getThemeId(), stageId, 0);
        com.stars.util.LogUtil.info("结婚组队|进入战斗,stageId:{}", stageId);

    }

    /**
     * 请求发送响应请求战斗协议;
     *
     * @param stageVo 场景vo数据
     */
    protected void requestSendClientEnterFight(Map<String, Module> moduleMap, ClientEnterDungeon enterFight,
                                               StageinfoVo stageVo) {
        enterFight.setIsAgain(isAgain);
        enterFight.setStageId(stageId);
        enterFight.setFightType(SceneManager.getStageVo(stageId).getStageType());
        List<FighterEntity> fighterList = new LinkedList<>();
        /* 出战角色 */
        FighterEntity roleEntity = FighterCreator.createSelf(moduleMap, stageVo.getPosition(), stageVo.getRotation());
        entityMap.put(roleEntity.getUniqueId(), roleEntity);
        /* 出战伙伴 */
        BuddyModule buddyModule = (BuddyModule) moduleMap.get(MConst.Buddy);
        // 有出战伙伴
        if (buddyModule.getFightBuddyId() != 0) {
            FighterEntity buddyEntity = FighterCreator.create(FighterEntity.TYPE_BUDDY, FighterEntity.CAMP_SELF,
                    buddyModule.getRoleBuddy(buddyModule.getFightBuddyId()));
            entityMap.put(buddyEntity.getUniqueId(), buddyEntity);
        }
        /* 预加载怪物 */
        initMonsterData(moduleMap, enterFight, stageVo);
        fighterList.addAll(entityMap.values());
        enterFight.setFighterEntityList(fighterList);
        /* 动态阻挡数据 */
        initDynamicBlockData(enterFight, stageVo);
        /* 是否自动战斗 */
        RoleModule rm = (RoleModule) moduleMap.get(MConst.Role);
        enterFight.setAutoFlag(rm.getAutoFightFlag(enterFight.getFightType()));

    }

    private void enemyDead(Map<String, Module> moduleMap, Map<String, List<String>> uIdMap) {
        com.stars.util.LogUtil.info("死亡的怪物|uIdMap:{}", uIdMap);
        List<String> uIdList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : uIdMap.entrySet()) {
            for (String monsterUId : entry.getValue()) {
                uIdList.add(monsterUId);
                FighterEntity entity = entityMap.get(monsterUId);
                try {
                    if (marryBattleScoreMap.containsKey(entry.getKey()) || entry.getKey().startsWith("b")) {
                        String uid = entry.getKey();
                        int addMarryBattleScore = calMarryBattleScore(entity.getMonsterAttrId());
                        if (entry.getKey().startsWith("b")) {
                            for (String fightUid : marryBattleScoreMap.keySet()) {
                                if (entry.getKey().contains(fightUid)) {
                                    uid = fightUid;
                                }
                            }
                        }
                        marryBattleScoreMap.put(uid, marryBattleScoreMap.get(uid) + addMarryBattleScore);
                    }
                } catch (Exception e) {
                    com.stars.util.LogUtil.info("{} 出异常了:{}", entry.getKey(), marryBattleScoreMap);
                    e.printStackTrace();
                }
            }
        }
        com.stars.util.LogUtil.info("结婚组队，怪物死亡得到的积分:{}", marryBattleScoreMap);
        ClientMarryBattleScore client = new ClientMarryBattleScore();
        client.setMarryBattleScoreMap(marryBattleScoreMap);
        sendPacketToTeamMembers(client, -1);

        Map<String, FighterEntity> sendMonsterMap = new HashMap<>();
        // 销毁陷阱怪数据
        List<String> destroyTrapMonsterList = new ArrayList<String>();
        // 动态阻挡状态改变
        Map<String, Byte> blockStatusMap = new HashMap<>();
        Map<String, Integer> dropIdMap = new HashMap<>();// 怪物掉落组
        for (String monsterUId : uIdList) {
            FighterEntity monsterEntity = entityMap.get(monsterUId);// 怪物fighter数据
            MonsterAttributeVo monsterAttr = SceneManager.getMonsterAttrVo(monsterEntity.getMonsterAttrId());// 产品数据
            if (monsterAttr == null) {
                continue;
            }
            spawnMapping.get(monsterEntity.getSpawnUId()).remove(monsterUId);

            MonsterSpawnVo spawnVo = SceneManager.getMonsterSpawnVo(monsterEntity.getSpawnConfigId());
            // 刷怪组全部死亡,关闭动态阻挡
            if (spawnMapping.get(monsterEntity.getSpawnUId()).isEmpty()) {
                blockStatusMap.putAll(closeBlock(monsterEntity.getSpawnConfigId()));
            }
            // 判断下一波刷怪条件
            if (spawnMapping.get(monsterEntity.getSpawnUId()).size() == Integer.parseInt(spawnVo.getNextConParam())) {
                if (spawnVo.getNextSpawnId() != 0) {
                    sendMonsterMap.putAll(spawnMonster(moduleMap, spawnVo.getNextSpawnId()));
                    destroyTrapMonsterList = destroyTrapMonster(spawnVo.getNextSpawnId());
                    blockStatusMap.putAll(openBlock(spawnVo.getNextSpawnId()));
                }
            }
            dropIdMap.put(monsterUId, monsterAttr.getDropId());
        }
        // 怪物死亡,将对应掉落组id发到队员module处理
        if (!dropIdMap.isEmpty()) {
            sendEventToTeamMembers(new EliteDungeonDropEvent(dropIdMap), -1);
        }
        // 胜利失败检测
        checkFinish(uIdList);
        if (stageStatus != SceneManager.STAGE_PROCEEDING) {
            finishDeal(moduleMap, stageStatus);
        }
        // 如果还有下波怪,继续刷
        if (blockStatusMap.size() == 0 && sendMonsterMap.size() == 0) {
            return;
        }
        ClientSpawnMonster clientSpawnMonster = new ClientSpawnMonster();
        clientSpawnMonster.setBlockStatusMap(blockStatusMap);
        clientSpawnMonster.setSpawnMonsterMap(sendMonsterMap);
        clientSpawnMonster.setDestroyTrapMonsterList(destroyTrapMonsterList);
        sendPacketToTeamMembers(clientSpawnMonster, -1);
    }

    @Override
    public void receivePacket(Map<String, Module> moduleMap, PlayerPacket packet) {
        if (stageStatus != SceneManager.STAGE_PROCEEDING)
            return;
        // 区域刷怪
        if (packet instanceof ServerAreaSpawnMonster) {
            ServerAreaSpawnMonster serverAreaSpawnMonster = (ServerAreaSpawnMonster) packet;
            areaSpawnMonster(null, serverAreaSpawnMonster.getSpawnId());
        }
        // AI/技能指令转发
        if (packet instanceof ServerSyncOrder) {
            ServerSyncOrder serverSyncOrder = (ServerSyncOrder) packet;
            ClientSyncOrder clientSyncOrder = new ClientSyncOrder();
            clientSyncOrder.setOrders(serverSyncOrder.getOrders());
            sendPacketToTeamMembers(clientSyncOrder, serverSyncOrder.getRoleId());
        }
        // 伤害包处理
        if (packet instanceof ServerFightDamage) {
            ServerFightDamage serverFightDamage = (ServerFightDamage) packet;
            dealFightDamage(serverFightDamage.getDamageList());
        }
    }

    @Override
    public void selfDead(List<String> uIdList) {
        for (String uniqueId : uIdList) {
            this.deadTimeMap.put(uniqueId, System.currentTimeMillis());
        }
    }

    @Override
    public void exit(long roleId) {
        super.exit(roleId);
    }

    public void dead(long roleId, int teamDungeonId) {
        memberRoleIds.remove(roleId);
    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        endTimestamp = System.currentTimeMillis();
        byte finalStatus = ServerLogConst.ACTIVITY_FAIL;  //初始化是失败
        //发奖
        int useTime = (int) Math.floor((endTimestamp - startTimestamp) / 1000.0);
        Map<Integer, Integer> awardMap = new HashMap<>();
        Map<Integer, Integer> extraItemMap = new HashMap<>();
        if (finish == SceneManager.STAGE_VICTORY) {
            awardMap = teamDungeonVo.getVictoryRewards();
            finalStatus = ServerLogConst.ACTIVITY_WIN; //胜利
            int dropId = teamDungeonVo.getTimeDropId(useTime);
            if (dropId != 0) {
                extraItemMap = DropUtil.executeDrop(dropId, 1);
            }
        } else if (finish == SceneManager.STAGE_FAIL) {
            awardMap = teamDungeonVo.getDefeatRewards();
        }
        MarrySceneFinishEvent finishEvent = new MarrySceneFinishEvent(finalStatus, SceneManager.SCENETYPE_MARRY_DUNGEON,
                stageId, finish, awardMap, useTime, new HashMap<>(marryBattleScoreMap), extraItemMap);
        sendEventToTeamMembers(finishEvent, -1);
        com.stars.util.LogUtil.info("结婚组队，结算信息| {} ", finishEvent.toString());
        marryBattleScoreMap.clear();
        ServiceHelper.teamDungeonService().removeFightScene(teamId);
    }


    /**
     * 处理客户端上传伤害包
     *
     * @param list
     */
    private void dealFightDamage(List<Damage> list) {
        List<String> deadSelfIds = new LinkedList<>();// 我方死亡实体id
        Map<String, List<String>> deadEnemyIds = new LinkedHashMap<>();// 敌方死亡实体id
        Map<String, Integer> curHpMap = new HashMap<>();
        for (Damage damage : list) {
            if (!entityMap.containsKey(damage.getReceiverId()))
                continue;
            // todo:验证伤害值
            FighterEntity receiver = entityMap.get(damage.getReceiverId());
            FighterEntity giver = entityMap.get(damage.getGiverId());
            // 统计玩家造成伤害
            if (giver.getFighterType() == FighterEntity.TYPE_PLAYER && damage.getValue() < 0) {
                collectPlayerDamage(giver.getUniqueId(), damage.getValue());
            } else if (giver.getFighterType() == FighterEntity.TYPE_BUDDY && damage.getValue() < 0) {
                String masterRoleId = entityMap.get(giver.getUniqueId()).getMasterUId();
                collectPlayerDamage(masterRoleId, damage.getValue());
            }
            // 血量变化前受害者已死亡
            if (receiver.isDead()) {
                curHpMap.put(receiver.getUniqueId(), 0);
                continue;
            }
            // 受害者血量变化
            receiver.changeHp(damage.getValue());
            curHpMap.put(receiver.getUniqueId(), receiver.getAttribute().getHp());
            if (receiver.isDead()) {
                if (receiver.getCamp() == FighterEntity.CAMP_ENEMY) {// 敌方死亡
                    List<String> deadEnemys = deadEnemyIds.get(damage.getGiverId());
                    if (deadEnemys == null) {
                        deadEnemys = new ArrayList<>();
                        deadEnemyIds.put(damage.getGiverId(), deadEnemys);
                    }
                    deadEnemys.add(damage.getReceiverId());
                } else {// 我方/中立方死亡
                    deadSelfIds.add(damage.getReceiverId());
                }
            }
        }
        if (!curHpMap.isEmpty()) {
            // 同步属性(血量)到客户端
            ClientSyncAttr clientSyncAttr = new ClientSyncAttr(curHpMap);
            clientSyncAttr.setDamageMap(damageMap);
            sendPacketToTeamMembers(clientSyncAttr, -1);
        }
        if (!deadEnemyIds.isEmpty())
            enemyDead(null, deadEnemyIds);
        if (!deadSelfIds.isEmpty())
            selfDead(deadSelfIds);
    }

    /**
     * 收集玩家伤害
     *
     * @param uniqueId
     * @param damage
     */
    private void collectPlayerDamage(String uniqueId, int damage) {
        int value = -1 * damage + (damageMap.containsKey(uniqueId) ? damageMap.get(uniqueId) : 0);
        damageMap.put(uniqueId, value);
    }

    public void addTeamMemberFighter(Collection<BaseTeamMember> collection) {
        marryBattleScoreMap = new HashMap<>();
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        for (BaseTeamMember teamMember : collection) {
            for (FighterEntity entity : teamMember.getEntityMap().values()) {
                FighterEntity newEntity = entity.copy();
                // 玩家注入出生位置/朝向
                if (newEntity != null && newEntity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                    newEntity.setPosition(stageVo.getPosition());
                    newEntity.setRotation(stageVo.getRotation());
                    if (teamMember.isPlayer()) {
                        memberRoleIds.add(Long.parseLong(newEntity.getUniqueId()));
                        marryBattleScoreMap.put(newEntity.getUniqueId(), 0);
                    } else {
                        newEntity.setFighterType(FighterEntity.TYPE_ROBOT);
                        newEntity.setIsRobot(true);
                    }
                }
                entityMap.put(entity.getUniqueId(), newEntity);
            }
        }
    }

    public void checkFinish(List<String> uIdList) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        // 先检测胜利
        for (Map.Entry<Byte, Integer> entry : stageVo.getVictoryConMap().entrySet()) {
            switch (entry.getKey()) {
                case SceneManager.VICTORY_CONDITION_KILL_ALL:// 全部击杀
                    killAll();
                    break;
                case SceneManager.VICTORY_CONDITION_KILL_APPOINT:// 击杀指定
                    killAppoint(SceneManager.STAGE_VICTORY, entry.getValue(), uIdList);
                    break;
                case SceneManager.VICTORY_CONDITION_TIME:// 指定时间后
                    victoryCheckTime(entry.getValue());
                    break;
                case SceneManager.VICTORY_CONDITION_KILL_BOSS:// 击杀boss类型怪物
                    killBoss(uIdList);
                    break;
                case SceneManager.VITORY_CONDITION_MARRY_BATTLE_SCORE:
                    victoryCheckMarryBattleScore(entry.getValue());
                    break;
                default:
                    break;
            }
        }
        // 失败
        for (Map.Entry<Byte, Integer> entry : stageVo.getFailConMap().entrySet()) {
            switch (entry.getKey()) {
                case SceneManager.FAIL_CONDITION_SELFDEAD:
                    break;
                case SceneManager.FAIL_CONDITION_TIME:// 指定时间后
                    defeatCheckTime(entry.getValue());
                    break;
                case SceneManager.FAIL_CONDITION_KILL_APPOINT:// 击杀指定 失败
                    killAppoint(SceneManager.STAGE_FAIL, entry.getValue(), uIdList);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 获得足够的情义积分后
     *
     * @param conditonParam
     */
    public void victoryCheckMarryBattleScore(int conditonParam) {
        int allValue = 0;
        for (int score : marryBattleScoreMap.values()) {
            allValue += score;
        }
        if (allValue >= conditonParam) {
            stageStatus = SceneManager.STAGE_VICTORY;
        }
        LogUtil.info("结婚组队|scoreMap:{}", marryBattleScoreMap);
    }

    /**
     * 计算能获得情义副本积分
     *
     * @param monsterId
     * @return
     */
    private int calMarryBattleScore(int monsterId) {
        MarryBattleScoreVo marryBattleScoreVo = MarryManager.getMarryBattleVo(monsterId);
        if (marryBattleScoreVo == null)
            return 0;
        int randOdd = RandomUtil.rand(marryBattleScoreVo.getMinScore(), marryBattleScoreVo.getMaxSocre());
        return randOdd;
    }

    /**
     * 每秒执行一次，用于做一些定时/计时处理的操作
     */
    public void onTime() {
        //检查死亡的机器人是否到自动复活时间，是的话就让机器人自动复活
        deadRobotUids.clear();
        Set<Map.Entry<String, Long>> entrySet = this.deadTimeMap.entrySet();
        for (Map.Entry<String, Long> entry : entrySet) {
            String uid = entry.getKey();
            FighterEntity entity = this.entityMap.get(uid);
            if (entity != null && entity.getIsRobot()) {
                deadRobotUids.add(uid);
            }
        }

        for (String uid : deadRobotUids) {
            if (checkRevive(uid)) {
                ClientRoleRevive clientRoleRevive = new ClientRoleRevive();
                clientRoleRevive.setSubType((byte) 1);
                clientRoleRevive.setReviceRoleId(uid);
                clientRoleRevive.setSuc(true);
                sendPacketToTeamMembers(clientRoleRevive, -1);
            }
        }

        //时间相关的胜利失败检测
        StageinfoVo stageVo = SceneManager.getStageVo(this.stageId);
        if (stageVo != null) {
            if (this.stageStatus == SceneManager.STAGE_PROCEEDING && stageVo.containTimeCondition()) {
                // 胜利失败检测
                checkFinish(null);
                if (stageStatus != SceneManager.STAGE_PROCEEDING) {
                    finishDeal(null, stageStatus);
                }
            }
        }
        //强制回城检测
        checkForceBackToCity();
    }

    private void checkForceBackToCity() {
        if (!isForceOut
                && (this.stageStatus == SceneManager.STAGE_FAIL || this.stageStatus == SceneManager.STAGE_VICTORY)
                && (int) ((System.currentTimeMillis() - endTimestamp) / 1000) >= (EliteDungeonManager.delayTime + 8)) {
            //force back to city
            sendEventToTeamMembers(new BackToCityFromTeamDungeonEvent(teamDungeonVo.getTeamdungeonid()), -1);
            ServiceHelper.teamDungeonService().removeFightScene(teamId);

            BaseTeam team = ServiceHelper.baseTeamService().getTeam(teamId);
            if (team != null) {
                team.setFight(Boolean.FALSE);
            }

            isForceOut = true;
        }
    }
}
