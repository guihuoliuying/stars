package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.newequipment.NewEquipmentConstant;
import com.stars.modules.newequipment.NewEquipmentManager;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.newequipment.userdata.RoleEquipment;
import com.stars.modules.offlinepvp.OfflinePvpModule;
import com.stars.modules.offlinepvp.event.OfflinePvpVictoryEvent;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.Damage;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterOfflinePvp;
import com.stars.modules.scene.packet.fightSync.ClientSyncAttr;
import com.stars.modules.scene.packet.fightSync.ServerFightDamage;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.tool.ToolModule;
import com.stars.util.MapUtil;
import com.stars.util.ServerLogConst;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by liuyuheng on 2016/10/12.
 */
public class OfflinePvpScene extends FightScene {
    private byte enemyIndex;// 挑战对手序号
    private long startTime = System.currentTimeMillis();//开打时间
    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object index) {
        byte enemyIndex = Byte.parseByte(index.toString());
        OfflinePvpModule opm = (OfflinePvpModule) moduleMap.get(MConst.OfflinePvp);
        if (!opm.canChallenge(enemyIndex)) {
            opm.warn("无法挑战,剩余挑战次数不足,或已战胜过对手");
            return false;
        }
        int stageId = opm.getFightStageId();
        if (stageId == 0) {
            opm.warn("找不到当前等级对应stageid");
            return false;
        }
        this.stageId = stageId;
        this.enemyIndex = enemyIndex;
        ServerLogModule log = (ServerLogModule)moduleMap.get(MConst.ServerLog);
        log.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_102.getThemeId(), stageId);
        this.startTime = System.currentTimeMillis();
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        ClientEnterOfflinePvp enterPacket = new ClientEnterOfflinePvp();
        enterPacket.setStageId(stageId);
        enterPacket.setLimitTime(stageVo.getVictoryConMap().get(SceneManager.VICTORY_CONDITION_TIME));
        enterPacket.setFightType(SceneManager.getStageVo(stageId).getStageType());
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
        /* 对手 */
        OfflinePvpModule opm = (OfflinePvpModule) moduleMap.get(MConst.OfflinePvp);
        Map<String, FighterEntity> enemyEntityMap = opm.getEnemyFighterEntity(enemyIndex);
        for (FighterEntity enemyEntity : enemyEntityMap.values()) {
            if (enemyEntity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                enemyEntity.setPosition(stageVo.getEnemyPos(1));
                enemyEntity.setRotation(stageVo.getEnemyRot(1));
            }
            entityMap.put(enemyEntity.getUniqueId(), enemyEntity.copy());
        }
        enterPacket.setFighterEntityList(entityMap.values());
        /* 成长buff */
        enterPacket.setGrowBuff(opm.getGrowBuff());
        /* 动态阻挡数据 */
        initDynamicBlockData(enterPacket, stageVo);
        /* 是否自动战斗 */
        RoleModule rm = (RoleModule) moduleMap.get(MConst.Role);
        enterPacket.setAutoFlag(rm.getAutoFightFlag(enterPacket.getFightType()));
        // 增加挑战次数
        opm.addChallegeCount();
        opm.send(enterPacket);
    }

    @Override
    public void exit(Map<String, Module> moduleMap) {
        if (stageStatus == SceneManager.STAGE_PROCEEDING || stageStatus == SceneManager.STAGE_PAUSE) {
            finishDeal(moduleMap, SceneManager.STAGE_FAIL);
            ServerLogModule log = (ServerLogModule)moduleMap.get(MConst.ServerLog);
            log.Log_core_activity(ServerLogConst.ACTIVITY_FAIL, ThemeType.ACTIVITY_102.getThemeId(), log.makeJuci(), ThemeType.ACTIVITY_102.getThemeId(), this.stageId, (System.currentTimeMillis()-this.startTime)/1000);
        }
    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        this.endTimestamp = System.currentTimeMillis();
        ClientStageFinish clientStageFinish = new ClientStageFinish(SceneManager.SCENETYPE_OFFLINEPVP, finish);
        OfflinePvpModule opm = (OfflinePvpModule) moduleMap.get(MConst.OfflinePvp);
        Map<Integer, Integer> rewardMap = opm.getVictoryReward();
        // 胜利通关
        if (finish == SceneManager.STAGE_VICTORY) {
            ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
            Map<Integer,Integer> map = toolModule.addAndSend(rewardMap, EventType.OFFLINEPVP.getCode());
            
            //勋章产出加成
            ForeShowModule foreShowModule = (ForeShowModule)moduleMap.get(MConst.ForeShow);
            if (foreShowModule.isOpen(ForeShowConst.MEDAL)) {
            	NewEquipmentModule newEquipmentModule = (NewEquipmentModule)moduleMap.get(MConst.NewEquipment);
                RoleEquipment roleEquipment = newEquipmentModule.getRoleEquipByType(NewEquipmentConstant.MEDAL_EQUIPMENT_TYPE);
                if (roleEquipment != null) {
    				int medalEquipmentId = roleEquipment.getEquipId();
    				Map<Integer, Integer> addReward = NewEquipmentManager.calMedalAddReward(NewEquipmentConstant.OfflinePvp_AddProduce_TargetId, medalEquipmentId, map);
    				if (addReward != null && addReward.size() > 0) {
    					toolModule.addAndSend(addReward, EventType.OFFLINEPVP.getCode());
        		        MapUtil.add(map, addReward);
					}
    			}
			}
            
            clientStageFinish.setItemMap(map);
            opm.dispatchEvent(new OfflinePvpVictoryEvent(enemyIndex));
            ServerLogModule log = (ServerLogModule)moduleMap.get(MConst.ServerLog);
            log.Log_core_activity(ServerLogConst.ACTIVITY_WIN, ThemeType.ACTIVITY_102.getThemeId(), log.makeJuci(), ThemeType.ACTIVITY_102.getThemeId(), this.stageId, (System.currentTimeMillis()-this.startTime)/1000);
        }else if(finish == SceneManager.STAGE_FAIL){
        	ServerLogModule log = (ServerLogModule)moduleMap.get(MConst.ServerLog);
            log.Log_core_activity(ServerLogConst.ACTIVITY_FAIL, ThemeType.ACTIVITY_102.getThemeId(), log.makeJuci(), ThemeType.ACTIVITY_102.getThemeId(), this.stageId, (System.currentTimeMillis()-this.startTime)/1000);
        }
        opm.send(clientStageFinish);
    }

    @Override
    public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {

    }

    @Override
    public void areaSpawnMonster(Map<String, Module> moduleMap, int spawnId) {

    }

    @Override
    public void receivePacket(Map<String, Module> moduleMap, PlayerPacket packet) {
        if (stageStatus != SceneManager.STAGE_PROCEEDING)
            return;
        // 伤害包处理
        if (packet instanceof ServerFightDamage) {
            ServerFightDamage serverFightDamage = (ServerFightDamage) packet;
            dealFightDamage(moduleMap, serverFightDamage.getDamageList());
            if (stageStatus != SceneManager.STAGE_PROCEEDING) {
                finishDeal(moduleMap, stageStatus);
            }
        }
    }

    @Override
    public void victoryCheckTime(int conditonParam) {
        if (System.currentTimeMillis() - startTimestamp < conditonParam) {
            return;
        }
        FighterEntity selfPlayerEntity = null;
        FighterEntity enemyPlayerEntity = null;
        for (FighterEntity entity : entityMap.values()) {
            if (entity.getFighterType() == FighterEntity.TYPE_SELF) {
                selfPlayerEntity = entity;
            }
            if (entity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                enemyPlayerEntity = entity;
            }
        }
        // 剩余血量
        double restSelf = 1.0 * selfPlayerEntity.getAttribute().getHp() / selfPlayerEntity.getAttribute().getMaxhp();
        double restEnemy = 1.0 * enemyPlayerEntity.getAttribute().getHp() / enemyPlayerEntity.getAttribute().getMaxhp();
        if (restSelf != restEnemy) {
            if (restSelf > restEnemy) {
                stageStatus = SceneManager.STAGE_VICTORY;
            } else {
                stageStatus = SceneManager.STAGE_FAIL;
            }
            return;
        }
        // 战力
        if (selfPlayerEntity.getFightScore() != enemyPlayerEntity.getFightScore()) {
            if (selfPlayerEntity.getFightScore() > enemyPlayerEntity.getFightScore()) {
                stageStatus = SceneManager.STAGE_VICTORY;
            } else {
                stageStatus = SceneManager.STAGE_FAIL;
            }
            return;
        }
        // 随机
        int random = new Random().nextInt(2);
        if (random > 0) {
            stageStatus = SceneManager.STAGE_VICTORY;
        } else {
            stageStatus = SceneManager.STAGE_FAIL;
        }
    }

    /**
     * 处理客户端上传伤害包
     *
     * @param list
     */
    private void dealFightDamage(Map<String, Module> moduleMap, List<Damage> list) {
        Map<String, Integer> curHpMap = new HashMap<>();
        for (Damage damage : list) {
            if (!entityMap.containsKey(damage.getReceiverId()))
                continue;
            // todo:验证伤害值
            FighterEntity receiver = entityMap.get(damage.getReceiverId());
            // 血量变化前受害者已死亡
            if (receiver.isDead())
                continue;
            // 受害者血量变化
            receiver.changeHp(damage.getValue());
            curHpMap.put(receiver.getUniqueId(), receiver.getAttribute().getHp());
            if (receiver.isDead() && receiver.getFighterType() == FighterEntity.TYPE_PLAYER) {
                if (receiver.getCamp() == FighterEntity.CAMP_ENEMY) {// 敌方死亡
                    stageStatus = SceneManager.STAGE_VICTORY;
                } else {// 我方死亡
                    stageStatus = SceneManager.STAGE_FAIL;
                }
            }
        }
        if (!curHpMap.isEmpty()) {
            // 同步属性(血量)到客户端
            ClientSyncAttr clientSyncAttr = new ClientSyncAttr(curHpMap);
            OfflinePvpModule opm = (OfflinePvpModule) moduleMap.get(MConst.OfflinePvp);
            opm.send(clientSyncAttr);
        }
    }
}
