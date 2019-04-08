package com.stars.multiserver.familywar.knockout.fight.stage;


import com.stars.core.attr.Attribute;
import com.stars.modules.drop.DropManager;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.packet.fight.ClientFamilyWarFightStageResult;
import com.stars.modules.pk.event.BackCityEvent;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFamilyWarEliteFight;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.skill.SkillManager;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.multiserver.familywar.knockout.FamilyWarKnockoutBattle;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.network.PacketUtil;
import com.stars.services.ServiceHelper;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.util.LogUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FamilyWarStageFight {

    private FamilyWarKnockoutBattle battle;
    /* 战斗服相关 */
    private String fightId;
    private int fightServerId;
    private long creationTimestamp;
    private long familyId;
    private int serverId;
    public Map<String, FighterEntity> fighterMap;
    public Map<String, FighterEntity> monsterMap;
    public Set<String> survivalSet;
    private Set<String> teamSheet;//精英成员过来打匹配

    public FamilyWarStageFight(String fightId, long familyId, int serverId,
                               Map<String, FighterEntity> fighterEntityMap,
                               FamilyWarKnockoutBattle battle, Set<String> teamSheet) {
        this.fightId = fightId;
        this.creationTimestamp = System.currentTimeMillis();
        this.familyId = familyId;
        this.serverId = serverId;
        this.fighterMap = fighterEntityMap;
        this.battle = battle;
        fightServerId = ServiceHelper.fsManagerService().getFightServer(FSManagerServiceActor.FIGHT_SERVER_LEVEL_COMM);
        monsterMap = new ConcurrentHashMap<>();
        survivalSet = new HashSet<>(fighterEntityMap.keySet());
        this.teamSheet = teamSheet;
    }


    public void createFight(byte camp, int battleType) {
        // 准备RPC调用
        FamilyWarStageFightArgs args = new FamilyWarStageFightArgs();
        args.setBattleId(battle.getBattleId());
        args.setMainServerId(serverId);
        List<Long> roleIdList = new ArrayList<>();
        Map<Long, Integer> roleWarType = new HashMap<>();
        for (String roleId : fighterMap.keySet()) {
            roleIdList.add(Long.parseLong(roleId));
            roleWarType.put(Long.parseLong(roleId), battleType);
        }
        args.setRoleIds(roleIdList);
        args.setRoleWarType(roleWarType);
        LogUtil.info("familywar|关卡战斗roleId:{}", args.getRoleIds());
        int aveFightSocre = 0;
        for (FighterEntity entity : fighterMap.values()) {
            LogUtil.info("hp:{},maxHp:{},fightScore:{},角色坐标:{}", entity.getAttribute().getHp(), entity.getAttribute().getMaxhp(), entity.getFightScore(), entity.getPosition());
            aveFightSocre += entity.getFightScore();
        }
        LogUtil.info("familywar|size:{}", fighterMap.size());
        if (fighterMap.size() != 0) {
            aveFightSocre = aveFightSocre / fighterMap.size();
        }
        Map<String, FighterEntity> nonPlayerEntity = getMonsterFighterEntity(FamilyActWarManager.stageIdOfStageFight, camp, aveFightSocre);
        monsterMap.putAll(nonPlayerEntity);
        // RPC调用
        LogUtil.info("familywar|创建战斗 fightServerId:{}", fightServerId);
        battle.fightService().createFight(fightServerId, FightConst.T_FAMILY_WAR_STAGE_FIGHT,
                MultiServerHelper.getServerId(), fightId, createEnterStageFightPacket(), args);
        battle.fightService().addMonster(fightServerId, FightConst.T_FAMILY_WAR_STAGE_FIGHT,
                MultiServerHelper.getServerId(), fightId, new ArrayList<>(nonPlayerEntity.values()));
    }

    public void stopFight(boolean backCity) {
        LogUtil.info("familywar|battle:{}", battle);
        battle.restoreFighterState(familyId, fighterMap.keySet());
        battle.fightService().stopFight(fightServerId, FightConst.T_FAMILY_WAR_STAGE_FIGHT,
                MultiServerHelper.getServerId(), fightId);
        if (backCity) {
            for (String roleId : fighterMap.keySet()) {
                // 抛事件
                battle.roleService().notice(serverId, Long.parseLong(roleId), new BackCityEvent());
            }
        }
    }

    public boolean checkTimeout() {
        StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfStageFight);
        return System.currentTimeMillis() - creationTimestamp >= stageVo.getFailConMap().get(SceneManager.FAIL_CONDITION_TIME);
    }

    public void end() {
        LogUtil.info("计时结束");
        stopFight(false);
        for (String fighterId : survivalSet) {
            FighterEntity entity = fighterMap.get(fighterId);
            if (FamilyWarUtil.isPlayer(entity)) {
                // 结算界面
                ClientFamilyWarFightStageResult packet = new ClientFamilyWarFightStageResult();
                packet.setWin(false);
                battle.roleService().send(serverId, Long.parseLong(fighterId), packet);
                battle.logEvent(FamilyWarConst.normalWarLog, FamilyWarConst.failLog, battle.getFamilyWar() == null ? 0 : battle
                                .getFamilyWar().getNormalPointsRankList(Long.parseLong(fighterId)).getRank(fighterId), 0, 0,
                        Long.parseLong(fighterId), battle.getBattleType() == FamilyWarConst.W_TYPE_LOCAL ? 1 : 2, battle.getBattleType() == FamilyWarConst.W_TYPE_QUALIFYING ? 1 : 2, new HashMap<Integer, Integer>());
            }
        }
    }

//    public void endFight() {
//        battle.finishStageFight(fightId, true);
//        return;
//    }

    private byte[] createEnterStageFightPacket() {
        StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfStageFight);
        ClientEnterFamilyWarEliteFight enterPacket = new ClientEnterFamilyWarEliteFight();
        enterPacket.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_STAGE_FIGTH); // buffer
        enterPacket.setStageId(FamilyActWarManager.stageIdOfStageFight);
        enterPacket.setLimitTime(stageVo.getFailConMap().get(SceneManager.FAIL_CONDITION_TIME) / 1000);
        enterPacket.setSkillVoMap(new HashMap<>(SkillManager.getSkillVoMap()));
        enterPacket.addMonsterVoMap(stageVo.getMonsterVoMap());
        return PacketUtil.packetToBytes(enterPacket);
    }

    public Map<String, FighterEntity> getMonsterFighterEntity(int stageId, byte camp, int aveFightSocre) {
        Map<String, FighterEntity> retMap = new HashMap<>();
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        for (int monsterSpawnId : stageVo.getMonsterSpawnIdList()) {
            retMap.putAll(spawnMonster(stageId, monsterSpawnId, camp, aveFightSocre));
        }
        return retMap;
    }

    public Map<String, FighterEntity> spawnMonster(int stageId, int monsterSpawnId, byte camp, int aveFightSocre) {
        Map<String, FighterEntity> resultMap = new HashMap<>();
        MonsterSpawnVo monsterSpawnVo = SceneManager.getMonsterSpawnVo(monsterSpawnId);
        if (monsterSpawnVo == null) {
            LogUtil.error("找不到刷怪组配置monsterspawnid={},请检查表", monsterSpawnId, new IllegalArgumentException());
            return resultMap;
        }
        int index = 0;
        for (MonsterAttributeVo monsterAttrVo : monsterSpawnVo.getMonsterAttrList()) {
            String monsterUniqueId = getMonsterUId(stageId, monsterSpawnId, monsterAttrVo.getStageMonsterId());
            FighterEntity monsterEntity = FighterCreator.create(FighterEntity.TYPE_MONSTER, monsterUniqueId,
                    getSpawnUId(monsterSpawnId), monsterSpawnId, monsterAttrVo, monsterSpawnVo.getAwake(),
                    monsterSpawnVo.getSpawnDelayByIndex(index++), null);
            if (camp == FamilyWarConst.K_CAMP2)
                monsterEntity.setCamp(FamilyWarConst.K_CAMP1);
            coeEntity(monsterEntity, aveFightSocre);
            resultMap.put(monsterUniqueId, monsterEntity);
        }
        return resultMap;
    }

    private void coeEntity(FighterEntity entity, int aveFightSocre) {
        printTowerAttr(entity, "计算前|成员平均战力" + aveFightSocre);
        entity.getAttribute().setAttack((int) (entity.getAttribute().getAttack() / 10000.0 * aveFightSocre * FamilyActWarManager.familywar_coefficient_attack_zz));
        entity.getAttribute().setHp((int) (entity.getAttribute().getHp() / 10000.0 * aveFightSocre * FamilyActWarManager.familywar_coefficient_hp_zz));
        entity.getAttribute().setMaxhp((int) (entity.getAttribute().getMaxhp() / 10000.0 * aveFightSocre * FamilyActWarManager.familywar_coefficient_hp_zz));
        entity.getAttribute().setDefense((int) (entity.getAttribute().getDefense() / 10000.0 * aveFightSocre * FamilyActWarManager.familywar_coefficient_defense_zz));
        entity.getAttribute().setHit((int) (entity.getAttribute().getHit() / 10000.0 * aveFightSocre * FamilyActWarManager.familywar_coefficient_hit_zz));
        entity.getAttribute().setAvoid((int) (entity.getAttribute().getAvoid() / 10000.0 * aveFightSocre * FamilyActWarManager.familywar_coefficient_avoid_zz));
        entity.getAttribute().setCrit((int) (entity.getAttribute().getCrit() / 10000.0 * aveFightSocre * FamilyActWarManager.familywar_coefficient_crit_zz));
        entity.getAttribute().setAnticrit((int) (entity.getAttribute().getAnticrit() / 10000.0 * aveFightSocre * FamilyActWarManager.familywar_coefficient_anticrit_zz));
        printTowerAttr(entity, "计算后");
    }

    private void printTowerAttr(FighterEntity entity, String text) {
        Attribute attribute = entity.getAttribute();
        LogUtil.info("familywar|塔属性:{}|uid:{},hp:{},maxHp:{},attack:{},anticrit:{},avoid:{},crit:{},defense:{},hit:{}"
                , text, entity.getUniqueId(), attribute.getHp(), attribute.getMaxhp(), attribute.getAttack(), attribute.getAnticrit(), attribute.getAvoid(), attribute.getCrit(), attribute.getDefense(), attribute.getHit());
    }

    protected String getSpawnUId(int spawnId) {
        return Integer.toString(spawnId);
    }

    protected String getMonsterUId(int stageId, int spawnId, int monsterId) {
        return "m" + stageId + getSpawnUId(spawnId) + monsterId;
    }

    public void onFightCreationSucceeded() {

    }

    public void onFightCreationFailed() {

    }

    public void onFighterAddingSucceeded() {
        // 切换连接
//        MultiServerHelper.modifyConnectorRoute(0L, fightServerId);
    }

    public void onFighterAddingFailed() {

    }

    public void handleDamage(Map<String, HashMap<String, Integer>> damageMap) {

    }

    /**
     * 战场死亡处理
     *
     * @param deadMap
     */
    public void handleDead(Map<String, String> deadMap) {
        LogUtil.info("玩家死亡");
        for (Map.Entry<String, String> dead : deadMap.entrySet()) {
            String victimUid = dead.getKey();
            if (monsterMap.containsKey(victimUid)) {
                FighterEntity entity = monsterMap.get(victimUid);
                if (SceneManager.getMonsterAttrVo(entity.getMonsterAttrId()).getMonsterVo().getType() == 1) {
                    handlePass();
                }
            }
            if (survivalSet.contains(victimUid)) {
                battle.removeFighterFromBattle(familyId, Long.parseLong(victimUid));
                survivalSet.remove(victimUid);
                handleFighterDead(victimUid);
                if (survivalSet.isEmpty()) {
                    handleFail(victimUid);
                }
            }
        }
    }

    /**
     * 关卡通关
     */
    public void handlePass() {
        LogUtil.info("关卡通关");
        stopFight(false);
        battle.removeStageFight(fightId);
//        stat.addFamilyPoints(FamilyActWarManager.familywar_pvewinscore);
        battle.updateFamilyPoint(familyId, FamilyActWarManager.familywar_pvewinscore);
        battle.sendToAllFighter(familyId, true);
        // 士气
        battle.updateMorale(familyId, FamilyActWarManager.familywar_smallpairscore); // 增加士气
        for (String fighterId : survivalSet) {
            // 从战场中玩家移除
            battle.removeFighterFromBattle(familyId, Long.parseLong(fighterId));
            FighterEntity entity = fighterMap.get(fighterId);
            if (FamilyWarUtil.isPlayer(entity)) {
                // 计算受害者的奖励
                Map<Integer, Integer> toolMap = DropManager.executeDrop(FamilyActWarManager.dropIdOfStageFightWinAward, 1);
                battle.accumulateNormalFightAward(Long.parseLong(fighterId), toolMap);
                battle.updateNormalPoints(fighterId, FamilyActWarManager.familywar_score_pvewin);
                // 结算界面
                ClientFamilyWarFightStageResult packet = new ClientFamilyWarFightStageResult();
                packet.setIsElite((byte) (teamSheet.contains(fighterId) ? 1 : 0));
                packet.setPoints(FamilyActWarManager.familywar_score_pvewin);
                packet.setWin(true);
                packet.setMoraleDelta(FamilyActWarManager.familywar_smallpairscore);
                packet.setToolMap(toolMap);
                battle.roleService().send(serverId, Long.parseLong(fighterId), packet);
                battle.logEvent(FamilyWarConst.normalWarLog, FamilyWarConst.successLog, battle.getFamilyWar() == null ? 0 : battle
                                .getFamilyWar().getNormalPointsRankList(Long.parseLong(fighterId)).getRank(fighterId), FamilyActWarManager.familywar_score_pvewin, 0,
                        Long.parseLong(fighterId), battle.getBattleType() == FamilyWarConst.W_TYPE_LOCAL ? 1 : 2, battle.getBattleType() == FamilyWarConst.W_TYPE_QUALIFYING ? 1 : 2, toolMap);
            }
        }
    }

    /**
     * 关卡失败
     */
    public void handleFail(String victimUid) {
        LogUtil.info("关卡失败:{}", victimUid);
        // stop fight actor
        stopFight(false);
        battle.removeStageFight(fightId);
        FighterEntity entity = fighterMap.get(victimUid);
        if (FamilyWarUtil.isPlayer(entity)) {
            // 结算界面
            ClientFamilyWarFightStageResult packet = new ClientFamilyWarFightStageResult();
            packet.setWin(false);
            battle.roleService().send(serverId, Long.parseLong(victimUid), packet);
            battle.logEvent(FamilyWarConst.normalWarLog, FamilyWarConst.failLog, battle.getFamilyWar() == null ? 0 : battle
                            .getFamilyWar().getNormalPointsRankList(Long.parseLong(victimUid)).getRank(victimUid), 0, 0,
                    Long.parseLong(victimUid), battle.getBattleType() == FamilyWarConst.W_TYPE_LOCAL ? 1 : 2, battle.getBattleType() == FamilyWarConst.W_TYPE_QUALIFYING ? 1 : 2, new HashMap<Integer, Integer>());
        }
    }

    /**
     * 玩家死亡
     *
     * @param loserId
     */
    public void handleFighterDead(String loserId) {
        // 结算界面
        ClientFamilyWarFightStageResult packet = new ClientFamilyWarFightStageResult();
        packet.setWin(false);
        battle.roleService().send(serverId, Long.parseLong(loserId), packet);
        battle.logEvent(FamilyWarConst.normalWarLog, FamilyWarConst.failLog, battle.getFamilyWar() == null ? 0 : battle
                        .getFamilyWar().getNormalPointsRankList(Long.parseLong(loserId)).getRank(loserId), 0, 0,
                Long.parseLong(loserId), battle.getBattleType() == FamilyWarConst.W_TYPE_LOCAL ? 1 : 2, battle.getBattleType() == FamilyWarConst.W_TYPE_QUALIFYING ? 1 : 2, new HashMap<Integer, Integer>());
    }

    public Map<String, FighterEntity> getFighterEntities() {
        return fighterMap;
    }

    public FamilyWarKnockoutBattle getBattle() {
        return battle;
    }

    public void setBattle(FamilyWarKnockoutBattle battle) {
        this.battle = battle;
    }

    public String getFightId() {
        return fightId;
    }

    public void setFightId(String fightId) {
        this.fightId = fightId;
    }

    public int getFightServerId() {
        return fightServerId;
    }

    public void setFightServerId(int fightServerId) {
        this.fightServerId = fightServerId;
    }
}
