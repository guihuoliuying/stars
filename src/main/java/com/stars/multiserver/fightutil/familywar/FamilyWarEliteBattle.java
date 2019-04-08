package com.stars.multiserver.fightutil.familywar;

import com.stars.core.attr.Attribute;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.packet.*;
import com.stars.modules.pk.packet.ServerOrder;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFamilyWarEliteFight;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.event.FamilyWarSendPacketEvent;
import com.stars.multiserver.familywar.knockout.FamilyWarKnockoutBattle;
import com.stars.multiserver.familywar.knockout.fight.elite.EliteFightTower;
import com.stars.multiserver.fight.ServerOrders;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.multiserver.fightutil.AbstractBattle;
import com.stars.multiserver.fightutil.FightPersonalStat;
import com.stars.multiserver.fightutil.FightResult;
import com.stars.multiserver.fightutil.FightStat;
import com.stars.network.PacketUtil;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.stars.modules.familyactivities.war.FamilyActWarManager.*;
import static java.lang.Long.parseLong;

/**
 * Created by chenkeyu on 2017-05-04 18:31
 */
public class FamilyWarEliteBattle extends AbstractBattle {
    private FamilyWarKnockoutBattle knockoutBattle;
    private String camp1FamilyName;
    private String camp2FamilyName;
    private Map<String, Integer> payReviveCountMap;
    private Map<String, EliteFightTower> towerMap = new HashMap<>();
    private FamilyWarEliteBattleStat battleStat;
    private long camp1FamilyId;
    private long camp2FamilyId;

    @Override
    public void onInitFight() {
//        this.camp1FamilyId = knockoutBattle.getCamp1FamilyId();
//        this.camp2FamilyId = knockoutBattle.getCamp2FamilyId();
//        fight = new Fight();
//        fight.setFightServerId(ServiceHelper.fsManagerService().getFightServer(FSManagerServiceActor.FIGHT_SERVER_LEVEL_COMM));
//        fight.setFightId(knockoutBattle.getEliteFightId());
//        fight.setCamp1Id(camp1FamilyId);
//        fight.setCamp2Id(camp2FamilyId);
//        camp1FamilyName = knockoutBattle.getFamilyName(camp1FamilyId);
//        camp2FamilyName = knockoutBattle.getFamilyName(camp2FamilyId);
//        fight.setCamp1MainServerId(knockoutBattle.getCamp1MainServerId());
//        fight.setCamp2MainServerId(knockoutBattle.getCamp2MainServerId());
//        fight.setCamp1FighterMap(knockoutBattle.campFighterMap(camp1FamilyId));
//        fight.setCamp2FighterMap(knockoutBattle.campFighterMap(camp2FamilyId));
//        fight.setCamp1TotalFightScore(knockoutBattle.getFamilyFightScore(camp1FamilyId));
//        fight.setCamp2TotalFightScore(knockoutBattle.getFamilyFightScore(camp2FamilyId));
//        battleStat = new FamilyWarEliteBattleStat(camp1FamilyId, camp2FamilyId);
//        for (FighterEntity entity : fight.getFighterMap().values()) {
//            long fighterId = parseLong(entity.getUniqueId());
//            battleStat.addPersonalStat(fighterId, entity.getName(), fight.getCamp(fighterId));
//        }
//        fight.initFight(this, battleStat);
//        int maxFightSocre = 0;
//        for (FighterEntity entity : fight.getCamp1FighterMap().values()) {
//            maxFightSocre = entity.getFightScore() > maxFightSocre ? entity.getFightScore() : maxFightSocre;
//        }
//        for (FighterEntity entity : fight.getCamp2FighterMap().values()) {
//            maxFightSocre = entity.getFightScore() > maxFightSocre ? entity.getFightScore() : maxFightSocre;
//        }
//        Map<String, FighterEntity> nonPlayerEntity = getMonsterFighterEntity(stageIdOfEliteFight);
//        for (FighterEntity entity : nonPlayerEntity.values()) {
//            Byte towerType = towerTypeMap.get(entity.getSpawnConfigId());
//            if (towerType == null) {
//                continue;
//            }
//            printTowerAttr(entity, "计算前|成员最高战力" + maxFightSocre);
//            entity.getAttribute().setAttack((int) (entity.getAttribute().getAttack() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_attack));
//            entity.getAttribute().setHp((int) (entity.getAttribute().getHp() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_hp));
//            entity.getAttribute().setMaxhp((int) (entity.getAttribute().getMaxhp() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_hp));
//            entity.getAttribute().setDefense((int) (entity.getAttribute().getDefense() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_defense));
//            entity.getAttribute().setHit((int) (entity.getAttribute().getHit() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_hit));
//            entity.getAttribute().setAvoid((int) (entity.getAttribute().getAvoid() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_avoid));
//            entity.getAttribute().setCrit((int) (entity.getAttribute().getCrit() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_crit));
//            entity.getAttribute().setAnticrit((int) (entity.getAttribute().getAnticrit() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_anticrit));
//            printTowerAttr(entity, "计算后");
//            EliteFightTower tower = new EliteFightTower(entity.getUniqueId(), entity.getCamp(), towerType, entity.getPosition(), entity.getAttribute().getMaxhp());
//            towerMap.put(tower.getUid(), tower);
//        }
//        fight.startFight(FightConst.T_FAMILY_WAR_ELITE_FIGHT, createEnterEliteFightPacket());
//        fight.addMonster(FightConst.T_FAMILY_WAR_ELITE_FIGHT, nonPlayerEntity);
//        //通知玩家进入战斗场景
//        ClientFamilyWarBattleStartTips packet = new ClientFamilyWarBattleStartTips();
//        LogUtil.info("familywar|通知阵营1玩家:{}|进入精英战场", fight.getCamp1FighterMap().keySet());
//        for (String fighterId : fight.getCamp1FighterMap().keySet()) {
//            ServiceHelper.roleService().notice(fight.getCamp1MainServerId(), parseLong(fighterId), new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_SAFE));
//        }
//        LogUtil.info("familywar|通知阵营2玩家:{}|进入精英战场", fight.getCamp2FighterMap());
//        for (String fighterId : fight.getCamp2FighterMap().keySet()) {
//            ServiceHelper.roleService().notice(fight.getCamp2MainServerId(), parseLong(fighterId), new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_SAFE));
//        }
//        //给内塔跟基地加上无敌的buff
//        addInvincibleBuff();
    }

    @Override
    public void enterFight(int mainServerId, long campId, long roleId) {
        sendStatPacket(campId, roleId, Long.toString(roleId));
        fight.enterFight(mainServerId, campId, roleId, stageIdOfEliteFight, FightConst.T_FAMILY_WAR_ELITE_FIGHT);
    }

    @Override
    public void handleDead(String victimUid, String attackerUid) {
        if (towerMap.containsKey(victimUid)) {
            EliteFightTower tower = towerMap.get(victimUid);
            if (tower.getType() == FamilyWarConst.K_TOWER_TYPE_CRYSTAL) {
                handleCrystalDead(tower, attackerUid);
            } else {
                handleTowerDead(tower, attackerUid);
            }
        } else { // 玩家
            handleFighterDead(victimUid, attackerUid);
        }
    }

    @Override
    public void handleDamage(String victimUid, Map<String, Integer> victimSufferedDamageMap) {
        if (towerMap.containsKey(victimUid)) {
            EliteFightTower tower = towerMap.get(victimUid);
            if (tower != null) {
                for (int demage : victimSufferedDamageMap.values()) {
                    tower.reduceHp(demage);
                }
            }
        }
    }

    @Override
    public void handleRevive(String fighterUid) {
        if (StringUtil.isEmpty(fight.getReviveMap())) return;
        try {
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<String, Long>> iterator = fight.getReviveMap().entrySet().iterator();
            Map.Entry<String, Long> entry;
            while (iterator.hasNext()) {
                entry = iterator.next();
                if (now - entry.getValue() > (homeReviveTime + 2) * 1000) {
                    revive(entry.getKey(), homeRevive);
                }
            }
        } catch (Exception e) {
            LogUtil.error("familywar|家族战复活异常:" + e.getMessage());
        }
    }

    @Override
    public void stopFight() {
        fight.stopFight(FightConst.T_FAMILY_WAR_ELITE_FIGHT);
    }

    @Override
    public FightResult endFight() {
        return fight.endFight();
    }

    private void revive(String fighterUid, byte reqType) {
        if (!fight.getReviveMap().containsKey(fighterUid)) return;
        FighterEntity entity = fight.getFighterMap().get(fighterUid);
        if (entity == null) return;
        entity.setFighterType(FighterEntity.TYPE_PLAYER);
        entity.setState((byte) 1);
        byte camp = fight.getCamp(fighterUid);
        entity.setCamp(camp);
        if (reqType == homeRevive) {//安全复活
            StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfEliteFight);
            if (camp == FamilyWarConst.K_CAMP1) { //设置出生点复活
                entity.setPosition(stageVo.getPosition());
                entity.setRotation(stageVo.getRotation());
            } else {
                entity.setPosition(stageVo.getEnemyPos(0));
                entity.setRotation(stageVo.getEnemyRot(0));
            }
            fight.handleRevive(fighterUid, FightConst.T_FAMILY_WAR_ELITE_FIGHT);
            fight.getReviveMap().remove(fighterUid);
        } else if (reqType == payRevive) {//原地复活（付费）

        }
    }

    private void handleCrystalDead(EliteFightTower tower, String attackerUid) {
        long winnerFamilyId = fight.getCampId(attackerUid);
        long loserFamilyId = winnerFamilyId == camp1FamilyId ? camp2FamilyId : camp1FamilyId;
        //knockoutBattle.end(fight.getFightId(), winnerFamilyId, loserFamilyId, battleStat, true);
    }

    private void handleTowerDead(EliteFightTower tower, String attackerUid) {
        // 检查状态
        // 强制将血量改成0
        tower.setHp(0);
        // 加积分，加士气
        knockoutBattle.updateMorale(fight.getCampId(attackerUid), moraleDeltaOfDestoryTower); // 己方增加士气
        knockoutBattle.updateMorale(fight.getOpponentCampId(fight.getCampId((attackerUid))), -moraleDeltaOfLosingTower); // 对方减少士气
        knockoutBattle.updateElitePoints(attackerUid, pointsDeltaOfDestoryTower); // 积分
        battleStat.updatePersonalStat(getMainServerId(attackerUid), parseLong(attackerUid), 0, 0, 0, 0, pointsDeltaOfDestoryTower);
        syncPersonalPoints(attackerUid, knockoutBattle.getFightStatMap());
        // 强制同步一次信息
        knockoutBattle.sendBattleFightUpdateInfo();
        byte camp = tower.getCamp();
        byte category = FamilyWarConst.K_TOWER_CATEGORY_OUTER;
        if (tower.getType() == FamilyWarConst.K_TOWER_TYPE_TOP
                || tower.getType() == FamilyWarConst.K_TOWER_TYPE_MID
                || tower.getType() == FamilyWarConst.K_TOWER_TYPE_BOT) {
            category = FamilyWarConst.K_TOWER_CATEGORY_OUTER;
        } else if (tower.getType() == FamilyWarConst.K_TOWER_TYPE_BASEBOT
                || tower.getType() == FamilyWarConst.K_TOWER_TYPE_BASETOP) {
            category = FamilyWarConst.K_TOWER_CATEGORY_INNER;
        }
        removeInvincibleBuff(camp, category);
    }

    private void handleFighterDead(String victimUid, String attackerUid) {
        // 人杀人
        if (fight.getFighterMap().containsKey(attackerUid) && fight.getFighterMap().containsKey(victimUid)) {
            FighterEntity victimEntity = fight.getFighterMap().get(victimUid);
            FighterEntity attackerEntity = fight.getFighterMap().get(attackerUid);
            // 提示
            knockoutBattle.roleService().warn(getMainServerId(attackerUid), parseLong(attackerUid),
                    attackerEntity.getName() + "杀死" + victimEntity.getName());

            // 更新攻击者的连杀数量, 并发送公告
            Integer attackerComboKillCount = fight.getComboKillCountMap().get(attackerUid);
            if (attackerComboKillCount == null) {
                fight.getComboKillCountMap().put(attackerUid, attackerComboKillCount = 1);
            } else {
                fight.getComboKillCountMap().put(attackerUid, attackerComboKillCount += 1);
            }
            if (attackerComboKillCount >= killCountThresholdOfNotice) {
                long attackerFamilyId = fight.getCampId(attackerUid);
                ClientText packet = new ClientText("familywar_tips_selfdoublekill", attackerEntity.getName(), attackerComboKillCount.toString());
                for (long roleId : fight.getFighterIdListMap().get(attackerFamilyId)) {
                    knockoutBattle.roleService().notice(getMainServerId(attackerUid), roleId, new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_FAMILY_WAR));
                }

                // 对方
                long victimFamilyId = fight.getCampId(victimUid);
                packet = new ClientText("familywar_tips_enemydoublekill", attackerEntity.getName(), attackerComboKillCount.toString());
                for (long roleId : fight.getFighterIdListMap().get(victimFamilyId)) {
                    knockoutBattle.roleService().notice(getMainServerId(victimUid), roleId, new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_FAMILY_WAR));
                }
            }
            // 更新受害者的连杀数量(置为0)，并发送公告
            Integer victimComboKillCount = fight.getComboKillCountMap().get(victimUid);
            if (victimComboKillCount != null && victimComboKillCount >= killCountThresholdOfNotice) {
                // 己方
                long attackerFamilyId = fight.getCampId(attackerUid);
                ClientText packet = new ClientText("familywar_tips_selfoverdoublekill", attackerEntity.getName(), victimEntity.getName(), victimComboKillCount.toString());
                for (long roleId : fight.getFighterIdListMap().get(attackerFamilyId)) {
                    knockoutBattle.roleService().notice(getMainServerId(attackerUid), roleId, new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_FAMILY_WAR));
                }

                // 对方
                packet = new ClientText("familywar_tips_enemyoverdoublekill", attackerEntity.getName(), victimEntity.getName(), victimComboKillCount.toString());
                long victimFamilyId = fight.getCampId(victimUid);
                for (long roleId : fight.getFighterIdListMap().get(victimFamilyId)) {
                    knockoutBattle.roleService().notice(getMainServerId(victimUid), roleId, new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_FAMILY_WAR));
                }
            }
            fight.getComboKillCountMap().put(victimUid, 0);

            // 增加士气
            knockoutBattle.updateMorale(fight.getCampId(attackerUid), moraleDeltaOfKillFighterInEliteFight);

            // 计算积分
            Map<String, Long> damageMap = fight.getSufferedDamageMap().get(victimUid);
            if (damageMap != null) {
                long totalDamage = MapUtil.sum(damageMap, 0L);
                for (Map.Entry<String, Long> entry : damageMap.entrySet()) {
                    if (towerMap.containsKey(entry.getKey())) continue;
                    double ratio = (entry.getValue() * 1.0) / totalDamage;
                    if (ratio >= damageRatioThresholdOfPersonalPoints) {
                        long pointsDelta = (long) (ratio * coefficientA + coefficientAA);
                        knockoutBattle.updateElitePoints(entry.getKey(), pointsDelta);
                        int assistDelta = entry.getKey().equals(attackerUid) ? 0 : 1;
                        battleStat.updatePersonalStat(getMainServerId(entry.getKey()), parseLong(entry.getKey()), 0, 0, assistDelta, 0, pointsDelta);
                        syncPersonalPoints(entry.getKey(), knockoutBattle.getFightStatMap());
                    }
                }
            }

            // 更新统计信息
            battleStat.updatePersonalStat(getMainServerId(attackerUid), parseLong(attackerUid), 1, 0, 0, attackerComboKillCount, 0);
            battleStat.updatePersonalStat(getMainServerId(victimUid), parseLong(victimUid), 0, 1, 0, 0, 0);

            // 发送双方的连杀包
            knockoutBattle.roleService().send(getMainServerId(attackerUid),
                    parseLong(attackerUid), new ClientFamilyWarBattleFightKillCount(attackerComboKillCount));
            knockoutBattle.roleService().send(getMainServerId(victimUid),
                    parseLong(victimUid), new ClientFamilyWarBattleFightKillCount(0));

        }
        //搭打人
        if (towerMap.containsKey(attackerUid) && fight.getFighterMap().containsKey(victimUid)) {
            //TODO
        }
        // 发送复活框
        fight.getReviveMap().put(victimUid, System.currentTimeMillis());
        Integer payReviveCount = payReviveCountMap.get(victimUid);
        LogUtil.info(victimUid + "|familywar|===================发送复活框数据");
        ClientFamilyWarBattleFightRevive packet = new ClientFamilyWarBattleFightRevive();
        packet.setType(ClientFamilyWarBattleFightRevive.TYPE_OF_COUNT);
        packet.setReviveCount(payReviveCount == null ? 0 : payReviveCount.intValue());
        knockoutBattle.roleService().send(getMainServerId(victimUid), Long.parseLong(victimUid), packet);
    }

    private void sendStatPacket(long familyId, long roleId, String fighterId) {
        Map<Integer, FightStat> statMap = knockoutBattle.getFightStatMap();
        ClientFamilyWarBattleStat packet = new ClientFamilyWarBattleStat();
        FightPersonalStat personalStat = battleStat.getPersonalStatMap().get(roleId);
        packet.setMyKillCount(personalStat.getKillCount());
        packet.setMyDeadCount(personalStat.getDeadCount());
        packet.setMyAssistCount(personalStat.getAssistCount());
        ClientFamilyWarBattleFamilyPoints points = new ClientFamilyWarBattleFamilyPoints();
        for (FightStat fightStat : statMap.values()) {
            if (familyId == camp1FamilyId) {
                points.addMyFamilyPoints(fightStat.getCamp1TotalPoints());
                points.addEnemyFamilyPoints(fightStat.getCamp2TotalPoints());
            } else if (familyId == camp2FamilyId) {
                points.addMyFamilyPoints(fightStat.getCamp2TotalPoints());
                points.addEnemyFamilyPoints(fightStat.getCamp1TotalPoints());
            }
        }
        syncPersonalPoints(Long.toString(roleId), statMap);
        ServiceHelper.roleService().send(getMainServerId(fighterId), roleId, packet);
        ServiceHelper.roleService().send(getMainServerId(fighterId), roleId, points);
    }

    public int getMainServerId(String fighterUid) {
        if (fight.getCamp1FighterMap().containsKey(fighterUid)) {
            return fight.getCamp1MainServerId();
        }
        if (fight.getCamp2FighterMap().containsKey(fighterUid)) {
            return fight.getCamp2MainServerId();
        }
        return 0;
    }

    public void syncPersonalPoints(String fighterUid, Map<Integer, FightStat> statMap) {
        long roleId = Long.parseLong(fighterUid);
        long rolePoints = 0;
        for (FightStat fightStat : statMap.values()) {
            rolePoints += fightStat.getPersonalStatMap().get(roleId).getPoints();
        }
        ServiceHelper.roleService().send(
                getMainServerId(fighterUid), roleId,
                new ClientFamilyWarBattleFightPersonalPoint(rolePoints));
    }

    private byte[] createEnterEliteFightPacket() {
        StageinfoVo stageVo = SceneManager.getStageVo(stageIdOfEliteFight);
        ClientEnterFamilyWarEliteFight enterPacket = new ClientEnterFamilyWarEliteFight();
        enterPacket.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_ELITE_FIGHT); // buffer
        enterPacket.setStageId(stageIdOfEliteFight);
        enterPacket.setLimitTime(FamilyActWarManager.familywar_lasttime);
        enterPacket.setStartRemainderTime(FamilyActWarManager.DYNAMIC_BLOCK_TIME);
        enterPacket.setSkillVoMap(new HashMap<Integer, SkillVo>(SkillManager.getSkillVoMap()));
        enterPacket.addMonsterVoMap(stageVo.getMonsterVoMap());
        /* 动态阻挡数据 */
        Map<String, Byte> blockStatus = new HashMap<>();
        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
            if (dynamicBlock.getShowSpawnId() == 0) {
                blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
            }
        }
        LogUtil.info("动态阻挡数据Elites:{}", blockStatus);
        enterPacket.setBlockMap(stageVo.getDynamicBlockMap());
        enterPacket.addBlockStatusMap(blockStatus);
        return PacketUtil.packetToBytes(enterPacket);
    }

    private Map<String, FighterEntity> getMonsterFighterEntity(int stageId) {
        Map<String, FighterEntity> retMap = new HashMap<>();
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        for (int monsterSpawnId : stageVo.getMonsterSpawnIdList()) {
            retMap.putAll(spawnMonster(stageId, monsterSpawnId));
        }
        return retMap;
    }

    private Map<String, FighterEntity> spawnMonster(int stageId, int monsterSpawnId) {
        Map<String, FighterEntity> resultMap = new HashMap<>();
        MonsterSpawnVo monsterSpawnVo = SceneManager.getMonsterSpawnVo(monsterSpawnId);
        if (monsterSpawnVo == null) {
            LogUtil.error("familywar|找不到刷怪组配置monsterspawnid={},请检查表", monsterSpawnId, new IllegalArgumentException());
            return resultMap;
        }
        int index = 0;
        for (MonsterAttributeVo monsterAttrVo : monsterSpawnVo.getMonsterAttrList()) {
            String monsterUniqueId = getMonsterUId(stageId, monsterSpawnId, monsterAttrVo.getStageMonsterId());
            FighterEntity monsterEntity = FighterCreator.create(FighterEntity.TYPE_MONSTER, monsterUniqueId,
                    getSpawnUId(monsterSpawnId), monsterSpawnId, monsterAttrVo, monsterSpawnVo.getAwake(),
                    monsterSpawnVo.getSpawnDelayByIndex(index++), null);
            resultMap.put(monsterUniqueId, monsterEntity);
        }
        return resultMap;
    }

    /**
     * 添加无敌buff
     */
    public void addInvincibleBuff() {
        ArrayList<String> addBuffTowerList = new ArrayList<>();
        for (EliteFightTower tower : towerMap.values()) {
            if (tower.getType() == FamilyWarConst.K_TOWER_TYPE_BASEBOT
                    || tower.getType() == FamilyWarConst.K_TOWER_TYPE_BASETOP
                    || tower.getType() == FamilyWarConst.K_TOWER_TYPE_CRYSTAL) {
                addBuffTowerList.add(tower.getUid());
            }
        }
        ServerOrder order = ServerOrders.newAddBuffOrder(ServerOrder.NONE, ServerOrder.NONE, FamilyActWarManager.invincibleBuffId, 1);
        fight.setInvincibleBuffInstId(order.getInstanceId());
        order.setUniqueIDs(addBuffTowerList);
        MainRpcHelper.fightBaseService().addServerOrder(fight.getFightServerId(), FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                MultiServerHelper.getServerId(), fight.getFightId(), order);
    }

    /**
     * 移除无敌buff
     */
    public void removeInvincibleBuff(byte camp, byte category) {
        ArrayList<String> removeBuffTowerList = new ArrayList<>();
        for (EliteFightTower tower : towerMap.values()) {
            if (tower.getCamp() != camp) continue;
            if (category == FamilyWarConst.K_TOWER_CATEGORY_OUTER) {
                if (tower.getType() == FamilyWarConst.K_TOWER_TYPE_BASETOP || tower.getType() == FamilyWarConst.K_TOWER_TYPE_BASEBOT) {
                    removeBuffTowerList.add(tower.getUid());
                }
            } else if (category == FamilyWarConst.K_TOWER_CATEGORY_INNER) {
                if (tower.getType() == FamilyWarConst.K_TOWER_TYPE_CRYSTAL) {
                    removeBuffTowerList.add(tower.getUid());
                }
            }
        }
        ServerOrder order = ServerOrders.newRemoveBuffOrder(ServerOrder.NONE, fight.getInvincibleBuffInstId());
        order.setUniqueIDs(removeBuffTowerList);
        MainRpcHelper.fightBaseService().addServerOrder(fight.getFightServerId(), FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                MultiServerHelper.getServerId(), fight.getFightId(), order);
    }

    private String getMonsterUId(int stageId, int spawnId, int monsterId) {
        return "m" + stageId + getSpawnUId(spawnId) + monsterId;
    }

    private String getSpawnUId(int spawnId) {
        return Integer.toString(spawnId);
    }

    private void printTowerAttr(FighterEntity entity, String text) {
        Attribute attribute = entity.getAttribute();
        LogUtil.info("familywar|塔属性:{}|uid:{},hp:{},attack:{},anticrit:{},avoid:{},crit:{},defense:{},hit:{}"
                , text, entity.getUniqueId(), attribute.getHp(), attribute.getAttack(), attribute.getAnticrit(), attribute.getAvoid(), attribute.getCrit(), attribute.getDefense(), attribute.getHit());
    }

    public FamilyWarKnockoutBattle getKnockoutBattle() {
        return knockoutBattle;
    }

    public void setKnockoutBattle(FamilyWarKnockoutBattle knockoutBattle) {
        this.knockoutBattle = knockoutBattle;
    }

    public String getCamp1FamilyName() {
        return camp1FamilyName;
    }

    public void setCamp1FamilyName(String camp1FamilyName) {
        this.camp1FamilyName = camp1FamilyName;
    }

    public String getCamp2FamilyName() {
        return camp2FamilyName;
    }

    public void setCamp2FamilyName(String camp2FamilyName) {
        this.camp2FamilyName = camp2FamilyName;
    }

    public Map<String, Integer> getPayReviveCountMap() {
        return payReviveCountMap;
    }

    public void setPayReviveCountMap(Map<String, Integer> payReviveCountMap) {
        this.payReviveCountMap = payReviveCountMap;
    }

    public Map<String, EliteFightTower> getTowerMap() {
        return towerMap;
    }

    public void setTowerMap(Map<String, EliteFightTower> towerMap) {
        this.towerMap = towerMap;
    }

    public FamilyWarEliteBattleStat getBattleStat() {
        return battleStat;
    }

    public void setBattleStat(FamilyWarEliteBattleStat battleStat) {
        this.battleStat = battleStat;
    }

    public long getCamp1FamilyId() {
        return camp1FamilyId;
    }

    public void setCamp1FamilyId(long camp1FamilyId) {
        this.camp1FamilyId = camp1FamilyId;
    }

    public long getCamp2FamilyId() {
        return camp2FamilyId;
    }

    public void setCamp2FamilyId(long camp2FamilyId) {
        this.camp2FamilyId = camp2FamilyId;
    }
}
