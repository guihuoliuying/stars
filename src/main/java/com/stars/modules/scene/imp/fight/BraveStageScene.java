package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.DailyModule;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.event.PassBraveStageEvent;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientSpawnMonster;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDungeon;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.task.TaskManager;
import com.stars.modules.task.TaskModule;
import com.stars.modules.task.prodata.TaskVo;
import com.stars.modules.task.userdata.RoleAcceptTask;
import com.stars.modules.task.userdata.RoleAcceptTaskTable;
import com.stars.modules.tool.ToolModule;
import com.stars.util.MapUtil;

import java.util.*;

/**
 * 勇者试炼关卡战斗场景
 * Created by gaopeidian on 2016/11/18.
 */
public class BraveStageScene extends FightScene {
    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object stageId) {
    	int tmpStageId = (int) stageId;
    	StageinfoVo stageVo = SceneManager.getStageVo(tmpStageId);
        if (stageVo == null) {
            return false;
        }
    	
        // 判断是否存在已接受的任务，targetId是该stageId的，有则可以进入
    	TaskModule taskModule = (TaskModule)moduleMap.get(MConst.Task);
    	RoleAcceptTaskTable acceptTaskTable = taskModule.getAcceptTaskTable();
    	if (acceptTaskTable != null) {
    		Map<Integer, RoleAcceptTask> acceptTaskMap = acceptTaskTable.getAcceptTaskMap();
    		if (acceptTaskMap != null) {
    			for (RoleAcceptTask roleAcceptTask : acceptTaskMap.values()) {				
    				TaskVo taskVo = TaskManager.getTaskById(roleAcceptTask.getTaskId());
    				if (taskVo != null && taskVo.getSort() == TaskManager.Task_Sort_HuoDong 
    						&& Integer.parseInt(taskVo.getTarget()) == tmpStageId) {
    					return true;
    				}       			
    			}
    		}
		}

        PlayerUtil.send(taskModule.id(), new ClientText("bravepractise_task_not_exist"));
        return false;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object stageId) {
    	int tmpStageId = (int) stageId;
    	this.stageId = tmpStageId;
    	
    	//打印开始日志
    	DailyModule dailyModule = (DailyModule)moduleMap.get(MConst.Daily);
        int count = dailyModule.getDailyCount(DailyManager.DAILYID_BRAVE_PRACTISE) + 1;
        ServerLogModule serverLogModule = (ServerLogModule)moduleMap.get(MConst.ServerLog);
        serverLogModule.Log_core_activity(ThemeType.ACTIVITY_20.getOperateId(), ThemeType.ACTIVITY_START.getOperateName(),
        		ThemeType.ACTIVITY_20.getThemeId(), serverLogModule.makeJuci(), ThemeType.ACTIVITY_20.getThemeId(),
        		Integer.toString(this.stageId), "");
    	     
        //这里进来说明已经通过了canEnter的测试，所以不再判空了;
        StageinfoVo stageVo = SceneManager.getStageVo(tmpStageId);
        
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        ClientEnterDungeon enterFight = new ClientEnterDungeon();
        requestSendClientEnterFight(moduleMap, enterFight, stageVo);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, this.stageId);
        sceneModule.send(enterFight);
    }

    /**
     * 请求发送响应请求战斗协议;
     *
     * @param stageVo       场景vo数据
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

    @Override
    public void exit(Map<String, Module> moduleMap) {
        if (stageStatus == SceneManager.STAGE_PROCEEDING || stageStatus == SceneManager.STAGE_PAUSE) {
            ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
            toolModule.addAndSend(totalDropMap, EventType.BRAVESTAGE.getCode());
        }
    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {
        // 刷怪数据
        Map<String, FighterEntity> sendMonsterMap = new HashMap<>();
        // 销毁陷阱怪数据
        List<String> destroyTrapMonsterList = new ArrayList<String>();
        // 动态阻挡状态改变
        Map<String, Byte> blockStatusMap = new HashMap<>();
        for (String monsterUId : uIdList) {
            FighterEntity monsterEntity = entityMap.get(monsterUId);
            if (monsterEntity == null) {
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
            // 怪物死亡增加掉落
            com.stars.util.MapUtil.add(totalDropMap, monsterEntity.getDropMap());
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
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        clientSpawnMonster.setSpawinId(spawnSeq);
        sceneModule.send(clientSpawnMonster);
        addResendPacket(spawnSeq, clientSpawnMonster);
    }

    @Override
    public void selfDead(Map<String, Module> moduleMap) {
        super.selfDead(moduleMap);
    }

    @Override
    public void areaSpawnMonster(Map<String, Module> moduleMap, int spawnId) {
        if (areaSpawnStateMap.containsKey(spawnId) && !areaSpawnStateMap.get(spawnId)) {
            ClientSpawnMonster clientSpawnMonster = new ClientSpawnMonster();
            clientSpawnMonster.setBlockStatusMap(openBlock(spawnId));
            clientSpawnMonster.setSpawnMonsterMap(spawnMonster(moduleMap, spawnId));
            clientSpawnMonster.setDestroyTrapMonsterList(destroyTrapMonster(spawnId));
            areaSpawnStateMap.put(spawnId, true);
            SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
            clientSpawnMonster.setSpawinId(spawnSeq);
            sceneModule.send(clientSpawnMonster);
            addResendPacket(spawnSeq, clientSpawnMonster);
        }
    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        this.endTimestamp = System.currentTimeMillis();
        
        //打印结束日志
        DailyModule dailyModule = (DailyModule)moduleMap.get(MConst.Daily);
        int count = dailyModule.getDailyCount(DailyManager.DAILYID_BRAVE_PRACTISE) + 1;
        String passTimeSecondStr = Integer.toString((int)((this.endTimestamp - this.startTimestamp)/1000));
        ServerLogModule serverLogModule = (ServerLogModule)moduleMap.get(MConst.ServerLog);
        ThemeType result = ThemeType.ACTIVITY_FAIL;

        
        // 关卡掉落
        Map<Integer, Integer> rewardMap = totalDropMap;
        
        //测试打印奖励的需求
        printTestInfo("BravePractise.DropReward", totalDropMap , -1 , -1);
        //测试打印奖励的需求
        
        byte star = 0;
        // 胜利通关
        if (finish == SceneManager.STAGE_VICTORY) {        	
            // 通关奖励
        	TaskModule taskModule = (TaskModule)moduleMap.get(MConst.Task);
        	RoleAcceptTaskTable acceptTaskTable = taskModule.getAcceptTaskTable();
        	if (acceptTaskTable != null) {
        		Map<Integer, RoleAcceptTask> acceptTaskMap = acceptTaskTable.getAcceptTaskMap();
        		if (acceptTaskMap != null) {
        			for (RoleAcceptTask roleAcceptTask : acceptTaskMap.values()) {				
        				TaskVo taskVo = TaskManager.getTaskById(roleAcceptTask.getTaskId());
        				if (taskVo != null && taskVo.getSort() == TaskManager.Task_Sort_HuoDong 
        						&& Integer.parseInt(taskVo.getTarget()) == stageId) {
        					MapUtil.add(rewardMap, taskVo.getAwardMap());
                			//测试打印奖励的需求
                			printTestInfo("BravePractise.PassReward", taskVo.getAwardMap() , stageId , taskVo.getId());
                	        //测试打印奖励的需求
        				}       			
        			}
        		}
			}
            result = ThemeType.ACTIVITY_WIN;
        }

        serverLogModule.Log_core_activity(ThemeType.ACTIVITY_20.getOperateId(), result.getOperateName(),
        		ThemeType.ACTIVITY_20.getThemeId(),serverLogModule.makeJuci(), ThemeType.ACTIVITY_20.getThemeId(),
                Integer.toString(this.stageId), passTimeSecondStr);
        
        ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
        Map<Integer, Integer> map = toolModule.addAndSend(rewardMap, EventType.BRAVESTAGE.getCode());
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        int jobId = roleModule.getRoleRow().getJobId();
        // 自动使用宝箱类型物品合并显示
        switchBoxTool(map, jobId);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        ClientStageFinish clientStageFinish = new ClientStageFinish(SceneManager.SCENETYPE_BRAVE_STAGE, finish);
        clientStageFinish.setStar(star);
        clientStageFinish.setItemMap(map);
        clientStageFinish.setUseTime((int) Math.floor((endTimestamp - startTimestamp) / 1000.0));
        sceneModule.send(clientStageFinish);
        if (finish == SceneManager.STAGE_VICTORY) {
            sceneModule.dispatchEvent(new PassBraveStageEvent(this.stageId));
        }
    }

    //测试打印奖励的需求
    public void printTestInfo(String info , Map<Integer, Integer> passReward , int stageId , int taskId){
    	String rewardStr = "";
        if (passReward != null) {
			Set<Map.Entry<Integer, Integer>> entrySet = passReward.entrySet();
			for (Map.Entry<Integer, Integer> entry : entrySet) {
				int itemId = entry.getKey();
				int count = entry.getValue();
				rewardStr += itemId + "-" + count + ",";
			}
		}    
        
        String printStr = "==========" + info + ":" + rewardStr;
        if (stageId > 0) {
			printStr += "stageId:" + stageId;
		}
        printStr += ",";
        if (taskId > 0) {
			printStr += "taskId:" + taskId;
		}
        printStr += "==========";
//        LogUtil.info(printStr);
    }
    
}
