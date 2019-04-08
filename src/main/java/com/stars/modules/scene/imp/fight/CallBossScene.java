package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.achievement.event.JoinActivityEvent;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.callboss.CallBossManager;
import com.stars.modules.callboss.prodata.CallBossVo;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.Damage;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFight;
import com.stars.modules.scene.packet.fightSync.ServerFightDamage;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.services.ServiceHelper;
import com.stars.services.callboss.CallBossConstant;
import com.stars.services.callboss.cache.CallBossCache;
import com.stars.util.ServerLogConst;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/9/6.
 */
public class CallBossScene extends FightScene {
    private int callBossId;// 召唤bossId
    private int monsterAttrId;// boss属性Id
    private long callTimestamp;// 召唤时间戳
    private int collectDamage;// 收集造成伤害

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
        int callbossId = (int) obj;
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        if (!CallBossManager.callBossVoMap.containsKey(callbossId)) {
            sceneModule.warn("找不到召唤boss,bossid=" + callbossId);
            return false;
        }
        CallBossCache callBossCache = ServiceHelper.callBossService().getCallBossCache(sceneModule.id(), callbossId);
        if (callBossCache == null || callBossCache.getStatus() != CallBossConstant.BOSS_STATUS_ALIVE) {
            //sceneModule.warn("无法进入,boss未召唤或已经死亡");
            return false;
        }
        // 根据缓存伤害值判断能否进入击杀
        CallBossVo callBossVo = CallBossManager.getCallBossVo(callbossId);
        MonsterAttributeVo monsterAttr = SceneManager.getMonsterAttrVo(callBossVo.getStageMonsterId());
        if (calCallBossHp(monsterAttr.getHp(), callBossVo.getLiveTime(), callBossCache.getCallTime()) -
                callBossCache.getReceiveDamage() <= 0) {
            sceneModule.warn("无法进入,boss已经死亡");
            return false;
        }
        int stageId = CallBossManager.getCallBossVo(callbossId).getStageId();
        if (!SceneManager.stageVoMap.containsKey(stageId))
            return false;
        this.stageId = stageId;
        this.callBossId = callbossId;
        this.monsterAttrId = callBossVo.getStageMonsterId();
        this.callTimestamp = callBossCache.getCallTime();
        this.collectDamage = callBossCache.getReceiveDamage();
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
    	RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
    	//发送该boss的数据给客户端
    	ServiceHelper.callBossService().sendCallBossData(roleModule.id(), callBossId);
    	
        CallBossVo callBossVo = CallBossManager.getCallBossVo(callBossId);
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        ClientEnterFight enterFight = new ClientEnterFight();
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        enterFight.setFightType(stageVo.getStageType());
        enterFight.setStageId(stageId);
        List<FighterEntity> fighterList = new LinkedList<>();
        /* 出战角色 */
        fighterList.add(FighterCreator.createSelf(moduleMap, stageVo.getPosition(), stageVo.getRotation()));
        /* 出战伙伴 */
        BuddyModule buddyModule = (BuddyModule) moduleMap.get(MConst.Buddy);
        // 有出战伙伴
        if (buddyModule.getFightBuddyId() != 0) {
            fighterList.add(FighterCreator.create(FighterEntity.TYPE_BUDDY, FighterEntity.CAMP_SELF,
                    buddyModule.getRoleBuddy(buddyModule.getFightBuddyId())));
        }
        /* 刷怪 */
        for (int monsterSpawnId : stageVo.getMonsterSpawnIdList()) {
            spawnMonster(moduleMap, monsterSpawnId);
        }
        for (FighterEntity monsterEntity : entityMap.values()) {
            // 计算注入召唤boss当前血量
            if (monsterEntity.getMonsterAttrId() == monsterAttrId)
                monsterEntity.getAttribute().setHp(calCallBossHp(monsterEntity.getAttribute().getMaxhp(),
                        callBossVo.getLiveTime(), callTimestamp) - collectDamage);
        }
        fighterList.addAll(entityMap.values());
        enterFight.setFighterEntityList(fighterList);
        /* 动态阻挡数据 */
        initDynamicBlockData(enterFight, stageVo);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, stageId);
        sceneModule.send(enterFight);
        sceneModule.dispatchEvent(new DailyFuntionEvent(DailyManager.DAILYID_CALLBOSS, 1));
        sceneModule.dispatchEvent(new JoinActivityEvent(JoinActivityEvent.CALLBOSS));
        // 开始日志
        ServerLogModule logModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
        logModule.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_10.getThemeId(), logModule.makeJuci(),
                ThemeType.ACTIVITY_10.getThemeId(), stageId, 0);
    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        this.endTimestamp = System.currentTimeMillis();
        ClientStageFinish clientStageFinish = new ClientStageFinish(SceneManager.SCENETYPE_CALLBOSS, finish);
        clientStageFinish.setUseTime((int) Math.floor((endTimestamp - startTimestamp) / 1000.0));
        clientStageFinish.setDamage(collectDamage);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        sceneModule.send(clientStageFinish);
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        ServiceHelper.callBossService().addRoleDamage(sceneModule.id(), roleModule.getRoleRow().getName(),
                callBossId, collectDamage);
        // 结束日志
        ServerLogModule logModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
        byte logType = finish == SceneManager.STAGE_VICTORY ? ServerLogConst.ACTIVITY_WIN : ServerLogConst.ACTIVITY_FAIL;
        logModule.Log_core_activity(logType, ThemeType.ACTIVITY_10.getThemeId(), logModule.makeJuci(),
                ThemeType.ACTIVITY_10.getThemeId(), stageId,
                (endTimestamp - startTimestamp) / 1000);
    }

    @Override
    public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {
        finishDeal(moduleMap, SceneManager.STAGE_VICTORY);
    }

    @Override
    public void selfDead(Map<String, Module> moduleMap) {
        super.selfDead(moduleMap);
    }

    @Override
    public void areaSpawnMonster(Map<String, Module> moduleMap, int spawnId) {

    }

    @Override
    public void exit(Map<String, Module> moduleMap) {
        if (stageStatus == SceneManager.STAGE_PROCEEDING || stageStatus == SceneManager.STAGE_PAUSE) {
            SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
            RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
            ServiceHelper.callBossService().addRoleDamage(sceneModule.id(), roleModule.getRoleRow().getName(),
                    callBossId, collectDamage);
        }
    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public void receivePacket(Map<String, Module> moduleMap, PlayerPacket packet) {
        if (packet instanceof ServerFightDamage) {
            collectGiveDamage((ServerFightDamage) packet);
        }
    }

    /**
     * 收集造成伤害值
     * 最大值为boss血量
     *
     * @param packet
     */
    private void collectGiveDamage(ServerFightDamage packet) {
        for (Damage damage : packet.getDamageList()) {
            if (!damage.getGiverId().equals(String.valueOf(packet.getRoleId())))
                continue;
            FighterEntity monsterEntity = entityMap.get(damage.getReceiverId());
            if (monsterEntity == null || monsterEntity.getMonsterAttrId() != monsterAttrId)
                continue;
            // todo:伤害验证
            if (damage.getValue() > 0) {
                continue;
            }
            collectDamage = Math.min(monsterEntity.getAttribute().getMaxhp(), (collectDamage + damage.getValue() * -1));
            monsterEntity.changeHp(damage.getValue());
        }
    }

    /**
     * 计算当前召唤boss血量
     * 总血量 - 时间扣除血量(向上取整)
     *
     * @param fullHp
     * @param liveTime
     * @param startTimestamp
     * @return
     */
    private int calCallBossHp(int fullHp, int liveTime, long startTimestamp) {
        return (fullHp - (int) Math.ceil((System.currentTimeMillis() - startTimestamp) / 1000.0 * fullHp / liveTime));
    }
}
