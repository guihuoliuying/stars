package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.prodata.DungeoninfoVo;
import com.stars.modules.poemdungeon.PoemDungeonModule;
import com.stars.modules.poemdungeon.teammember.RobotTeamMember;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.event.PassStageEvent;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDungeon;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.tool.ToolModule;
import com.stars.util.MapUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by gaopeidian on 2017/4/7.
 */
public class PoemDungeonScene extends DungeonScene {
    @Override
    public void enter(Map<String, Module> moduleMap, Object dungeonId) {
        int tmpDungeonId = (int) dungeonId;
        //这里进来说明已经通过了canEnter的测试，所以不再判空了;
        DungeoninfoVo dungeonVo = DungeonManager.getDungeonVo(tmpDungeonId);
        StageinfoVo stageVo = SceneManager.getStageVo(dungeonVo.getStageId());
        this.stageId = dungeonVo.getStageId();
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        this.dungeonId = dungeonVo.getDungeonId();

        DungeonModule dungeonModule = (DungeonModule) moduleMap.get(MConst.Dungeon);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        if (sceneModule.getGmDungeonId() != tmpDungeonId) {
            dungeonModule.addEnterCount(tmpDungeonId);
        }
        sceneModule.setGmDungeonId(0);
        ClientEnterDungeon enterFight = new ClientEnterDungeon();
        enterFight.setDungeonId(tmpDungeonId);
         /* 副本失败时间 */
        if (stageVo.getFailConMap().containsKey(SceneManager.FAIL_CONDITION_TIME)) {
            enterFight.setFailTime(stageVo.getFailConMap().get(SceneManager.FAIL_CONDITION_TIME));
        }

        //注入助战机器人
        PoemDungeonModule poemDungeonModule = (PoemDungeonModule) moduleMap.get(MConst.PoemDungeon);
        List<RobotTeamMember> robotList = poemDungeonModule.getRobotMembers();
        addTeamMemberFighter(robotList);

        requestSendClientEnterFight(moduleMap, enterFight, stageVo);

        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, stageId);
        sceneModule.send(enterFight);
//        sceneModule.cacheSend(enterFight);
        //日志
        String goType = "1";
        int juci = dungeonModule.getDungeonCount(tmpDungeonId);
        if (dungeonModule.isFirstPass(tmpDungeonId)) {
            goType = "1";
        } else {
            goType = "2";
        }
        ServerLogModule log = (ServerLogModule) moduleMap.get(MConst.ServerLog);
        log.Log_core_case(ThemeType.DUNGEON_START.getOperateId(), ThemeType.DUNGEON_START.getOperateName(), "enter", juci + "", tmpDungeonId + "", "", goType, dungeonVo.getBossIcon() + 1);
    }

    /**
     * 加入队伍成员的属性
     *
     * @param collection
     */
    public void addTeamMemberFighter(Collection<RobotTeamMember> collection) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        for (RobotTeamMember teamMember : collection) {
            for (FighterEntity entity : teamMember.getEntityMap().values()) {
                FighterEntity newEntity = entity.copy();

                // 玩家注入出生位置/朝向
                if (newEntity != null && newEntity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                    newEntity.setPosition(stageVo.getPosition());
                    newEntity.setRotation(stageVo.getRotation());
                }
                entityMap.put(entity.getUniqueId(), newEntity);
            }
        }
    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        this.endTimestamp = System.currentTimeMillis();
        // 关卡掉落
        Map<Integer, Integer> rewardMap = totalDropMap;
        DungeonModule dungeonModule = (DungeonModule) moduleMap.get(MConst.Dungeon);
        byte star = 0;
        int juci = dungeonModule.getDungeonCount(dungeonId);
        DungeoninfoVo dungeonVo = DungeonManager.getDungeonVo(dungeonId);
        // 胜利通关
        String goType = "1";
        boolean isFirstPass = false;
        StringBuffer info = new StringBuffer();
        if (finish == SceneManager.STAGE_VICTORY) {
            if (dungeonModule.isFirstPass(dungeonId)) {
                goType = "l";
                isFirstPass = true;
            } else {
                goType = "2";
                isFirstPass = false;
            }
            // 评星
            star = calStar(dungeonModule.id());
            // 通关奖励+首通奖励
            MapUtil.add(rewardMap, dungeonModule.getPassReward(dungeonId));
            dungeonModule.passDungeon(dungeonId, star);

            info.append("fight_time:");
            info.append((this.endTimestamp - this.startTimestamp) / 1000).append("#sp_case:");
            info.append(dungeonModule.getRoleMaxDungeonId((byte) 0)).append("#nm_case:").append(dungeonModule.getRoleMaxDungeonId((byte) 1));
            //日志
            ServerLogModule log = (ServerLogModule) moduleMap.get(MConst.ServerLog);
            log.Log_core_case(ThemeType.DUNGEON_WIN.getOperateId(), ThemeType.DUNGEON_WIN.getOperateName(), "1", juci + "", dungeonId + "", info.toString(), goType, dungeonVo.getBossIcon() + 1);
        } else if (finish == SceneManager.STAGE_FAIL) {//挑战失败
            info.append("fight_time:");
            info.append((this.endTimestamp - this.startTimestamp) / 1000).append("#sp_case:");
            info.append(dungeonModule.getRoleMaxDungeonId((byte) 0)).append("#nm_case:").append(dungeonModule.getRoleMaxDungeonId((byte) 1));
            ServerLogModule log = (ServerLogModule) moduleMap.get(MConst.ServerLog);
            log.Log_core_case(ThemeType.DUNGEON_FAIL.getOperateId(), ThemeType.DUNGEON_FAIL.getOperateName(), "1", juci + "", dungeonId + "", info.toString(), goType, dungeonVo.getBossIcon() + 1);
        }
        ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
        Map<Integer, Integer> map = toolModule.addAndSend(rewardMap, EventType.DUNGEONSCENE.getCode());
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        int jobId = roleModule.getRoleRow().getJobId();

        // 自动使用宝箱类型物品合并显示
        switchBoxTool(map, jobId);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        ClientStageFinish clientStageFinish = new ClientStageFinish(SceneManager.SCENETYPE_POEM_DUNGEON, finish);
        clientStageFinish.setStar(star);
        clientStageFinish.setItemMap(map);
        clientStageFinish.setUseTime((int) Math.floor((endTimestamp - startTimestamp) / 1000.0));
        sceneModule.send(clientStageFinish);
        if (finish == SceneManager.STAGE_VICTORY) {
            sceneModule.dispatchEvent(new PassStageEvent(this.dungeonId, star, isFirstPass));
        }
    }
}
