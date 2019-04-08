package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.newofflinepvp.NewOfflinePvpManager;
import com.stars.modules.newofflinepvp.NewOfflinePvpModule;
import com.stars.modules.newofflinepvp.prodata.OfflineInitializeVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.Scene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.Damage;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterNewOfflinePvp;
import com.stars.modules.scene.packet.fightSync.ClientSyncAttr;
import com.stars.modules.scene.packet.fightSync.ServerFightDamage;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.services.ServiceHelper;
import com.stars.services.summary.Summary;
import com.stars.util.LogUtil;
import com.stars.util.ServerLogConst;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by chenkeyu on 2017-03-11 14:35
 */
public class NewOfflinePvpScene extends FightScene {
    private long fightId;//对方id
    private byte roleOrRobot;//真人1或者机器人0
    private boolean isOver = false;

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object stageId) {
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        NewOfflinePvpModule offlinePvpModule = (NewOfflinePvpModule) moduleMap.get(MConst.NewOfflinePvp);
        offlinePvpModule.dealFightCount();
        String tmpStr = (String) obj;
        String[] data = tmpStr.split("-");
        this.stageId = Integer.parseInt(data[0]);
        this.roleOrRobot = Byte.parseByte(data[1]);
        this.fightId = Long.parseLong(data[2]);
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        ClientEnterNewOfflinePvp enterFight = new ClientEnterNewOfflinePvp();
        enterFight.setIsAgain(isAgain);
        enterFight.setStageId(stageId);
        enterFight.setFightType(stageVo.getStageType());
        enterFight.setLimitTime(stageVo.getFailConMap().get(SceneManager.FAIL_CONDITION_TIME));
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
        Map<String, FighterEntity> enemyEntityMap = getOtherFightEntity(fightId, roleOrRobot);
        for (FighterEntity fighterEntity : enemyEntityMap.values()) {
            if (fighterEntity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                fighterEntity.setPosition(stageVo.getEnemyPos(1));
                fighterEntity.setRotation(stageVo.getEnemyRot(1));
            }
            entityMap.put(fighterEntity.getUniqueId(), fighterEntity.copy());
        }
        enterFight.setFighterEntityList(entityMap.values());
        initDynamicBlockData(enterFight, stageVo);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        /* 是否自动战斗 */
        RoleModule rm = (RoleModule) moduleMap.get(MConst.Role);
        enterFight.setAutoFlag(rm.getAutoFightFlag(enterFight.getFightType()));
        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, this.stageId);
        sceneModule.send(enterFight);
        ServerLogModule logModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
        logModule.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_30.getThemeId(), logModule.makeJuci(),
                ThemeType.ACTIVITY_30.getThemeId(), stageId, 0);
    }

    /**
     * 组装对手的数据
     *
     * @param roleId
     * @param roleOrRobot
     * @return
     */
    private Map<String, FighterEntity> getOtherFightEntity(long roleId, byte roleOrRobot) {
        if (roleOrRobot == NewOfflinePvpManager.robot) {
            OfflineInitializeVo vo = NewOfflinePvpManager.getOfflineInitializeVo(roleId);
            return FighterCreator.createOfflinePvpRobot(FighterEntity.CAMP_ENEMY, vo);
        } else {
            Summary summary = ServiceHelper.summaryService().getSummary(roleId);
            return FighterCreator.createBySummary(FighterEntity.CAMP_ENEMY, summary);
        }
    }

    @Override
    public void exit(Map<String, Module> moduleMap) {
        if (stageStatus == SceneManager.STAGE_PROCEEDING || stageStatus == SceneManager.STAGE_PAUSE) {
            com.stars.util.LogUtil.info("手动回城");
            finishDeal(moduleMap, SceneManager.STAGE_FAIL);
        }
    }

    @Override
    public boolean isEnd() {
        return false;
    }

    /**
     * 能否重复进入
     *
     * @return
     */
    @Override
    public boolean isCanRepeatEnter(Scene newScene, byte newSceneType, int newSceneId, Object extend) {
        return true;
    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        if (isOver)
            return;
        isOver = true;
        this.endTimestamp = System.currentTimeMillis();
        NewOfflinePvpModule offlinePvpModule = (NewOfflinePvpModule) moduleMap.get(MConst.NewOfflinePvp);
        offlinePvpModule.exitFight(fightId, finish);
        byte logType = finish == SceneManager.STAGE_VICTORY ? ServerLogConst.ACTIVITY_WIN : ServerLogConst.ACTIVITY_FAIL;
        ServerLogModule logModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
        logModule.Log_core_activity(logType, ThemeType.ACTIVITY_30.getThemeId(), logModule.makeJuci(),
                ThemeType.ACTIVITY_30.getThemeId(), stageId, (endTimestamp - startTimestamp) / 1000);
    }

    @Override
    public void receivePacket(Map<String, Module> moduleMap, PlayerPacket packet) {
        if (stageStatus != SceneManager.STAGE_PROCEEDING)
            return;
        if (packet instanceof ServerFightDamage) {
            ServerFightDamage serverFightDamage = (ServerFightDamage) packet;
            dealFightDamage(moduleMap, serverFightDamage.getDamageList());
            if (stageStatus != SceneManager.STAGE_PROCEEDING) {
                com.stars.util.LogUtil.info("计算伤害包:{}", stageStatus);
                finishDeal(moduleMap, stageStatus);
            }
        }
    }

    private void dealFightDamage(Map<String, Module> moduleMap, List<Damage> damageList) {
        com.stars.util.LogUtil.info("伤害包计算开始,关卡状态:{}", stageStatus);
        Map<String, Integer> curHpMap = new HashMap<>();
        for (Damage damage : damageList) {
            if (!entityMap.containsKey(damage.getReceiverId()))
                continue;
            // todo:验证伤害值
            FighterEntity receiver = entityMap.get(damage.getReceiverId());
            // 血量变化前受害者已死亡
            if (receiver.isDead())
                continue;
            // 受害者血量变化
            receiver.changeHp(damage.getValue());
            com.stars.util.LogUtil.info("受害者:{}--血量:{}", receiver.getUniqueId(), receiver.getAttribute().getHp());
            curHpMap.put(receiver.getUniqueId(), receiver.getAttribute().getHp());
            if (receiver.isDead() && receiver.getFighterType() == FighterEntity.TYPE_PLAYER) {
                com.stars.util.LogUtil.info("有人死了：{}", receiver.getCamp());
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
            NewOfflinePvpModule opm = (NewOfflinePvpModule) moduleMap.get(MConst.NewOfflinePvp);
            opm.send(clientSyncAttr);
        }
        LogUtil.info("伤害包计算结束,关卡状态:{}", stageStatus);
    }

    @Override
    public void defeatCheckTime(int conditonParam) {
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

    public void killAll() {
        for (FighterEntity fighterEntity : entityMap.values()) {
            if (fighterEntity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                if (fighterEntity.isDead()) {
                    stageStatus = SceneManager.STAGE_VICTORY;
                }
            }
        }
    }

    @Override
    public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {

    }

    @Override
    public void areaSpawnMonster(Map<String, Module> moduleMap, int spawnId) {

    }
}
