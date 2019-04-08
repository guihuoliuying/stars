package com.stars.modules.runeDungeon;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.drop.DropModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.runeDungeon.proData.RuneDungeonVo;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientSpawnMonster;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDungeon;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterRuneDungeon;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;

import java.util.*;

public class RuneDungeonScene extends FightScene{
	
	private byte playType;
	
	private int dungeonId;
	
	private int buffId;
	
	private int addNum;
	
	private List<Long> friendList = new ArrayList<>();
	
	private boolean isFinish = false;

	@Override
	public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
		if(obj == null || "".equals(obj)) return false;
		int tmpStageId = (int) obj;
        StageinfoVo stageVo = SceneManager.getStageVo(tmpStageId);
        if (stageVo == null) {
            return false;
        }
        return true;
	}

	@Override
	public void enter(Map<String, Module> moduleMap, Object obj) {
//		Object[] params = (Object[]) obj;
		int tmpStageId = (int)obj;
//		this.playType = (Byte)params[1];
//		this.dungeonId = (Integer)params[2];
//		this.buffId = (Integer)params[3];
//		this.addNum = (Integer)params[4];
        this.stageId = tmpStageId;
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        StageinfoVo stageVo = SceneManager.getStageVo(tmpStageId);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        ClientEnterRuneDungeon enterPacket = new ClientEnterRuneDungeon();
        enterPacket.setBuffId(buffId);
        enterPacket.setAddNum(addNum);
        requestSendClientEnterFight(moduleMap, enterPacket, stageVo);
        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, stageId);
        sceneModule.send(enterPacket);
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
        enterFight.setFightType(stageVo.getStageType());
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
        /* 副本失败时间 */
        if(stageVo.getFailConMap().containsKey(SceneManager.FAIL_CONDITION_TIME)){
        	enterFight.setFailTime(stageVo.getFailConMap().get(SceneManager.FAIL_CONDITION_TIME));
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
    
    public void addFriendFighter(Map<String, FighterEntity> setEntityMap){
    	for(FighterEntity entity : setEntityMap.values()){   
    		entityMap.put(entity.getUniqueId(), entity);
    	}
    }
    
    public void addToFiendList(long friendId){
    	friendList.add(friendId);
    }

	@Override
	public void exit(Map<String, Module> moduleMap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isEnd() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void finishDeal(Map<String, Module> moduleMap, byte finish) {
		if(isFinish){
			return;
		}
		isFinish = true;
		this.endTimestamp = System.currentTimeMillis();
		RuneDungeonModule runeDungeonModule = (RuneDungeonModule)moduleMap.get(MConst.RuneDungeon);
		runeDungeonModule.fightEnd(finish, playType, dungeonId, stageId, endTimestamp-startTimestamp);
		if(finish==SceneManager.STAGE_VICTORY){
			RuneDungeonVo runeDungeonVo = RuneDungeonManager.runeDungeonMap.get(dungeonId);
			int helpdrop = runeDungeonVo.getHelpdrop();
			DropModule dropModule = (DropModule)moduleMap.get(MConst.Drop);
			Map<Integer, Integer> toolMap = dropModule.executeDrop(helpdrop, 1, true);
			//发放助战好友奖励
			for(long friendId : friendList){				
				ServiceHelper.runeDungeonService().sendHelpFightAward(friendId, runeDungeonModule.id(), toolMap);
			}
		}
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
            	LogUtil.info("monster dead but not get entity monsteruid="+monsterUId);
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
            MapUtil.add(totalDropMap, monsterEntity.getDropMap());
        }
		// 胜利失败检测
        if (stageStatus == SceneManager.STAGE_PROCEEDING || stageStatus == SceneManager.STAGE_PAUSE) {
            checkFinish(uIdList);
            if (stageStatus != SceneManager.STAGE_PROCEEDING && stageStatus != SceneManager.STAGE_PAUSE) {
                finishDeal(moduleMap, stageStatus);
            }
        }
        // 如果还有下波怪,继续刷
        if (blockStatusMap.size() == 0 && sendMonsterMap.size() == 0) {
            return;
        }
        ClientSpawnMonster clientSpawnMonster = new ClientSpawnMonster();
        clientSpawnMonster.setSpawinId(spawnSeq);
        clientSpawnMonster.setBlockStatusMap(blockStatusMap);
        clientSpawnMonster.setSpawnMonsterMap(sendMonsterMap);
        clientSpawnMonster.setDestroyTrapMonsterList(destroyTrapMonsterList);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        clientSpawnMonster.setSpawinId(spawnSeq);
        sceneModule.send(clientSpawnMonster);
        addResendPacket(spawnSeq, clientSpawnMonster);
	}

	@Override
	public void areaSpawnMonster(Map<String, Module> moduleMap, int spawnId) {
		// TODO Auto-generated method stub
		
	}

	public byte getPlayType() {
		return playType;
	}

	public void setPlayType(byte playType) {
		this.playType = playType;
	}

	public int getDungeonId() {
		return dungeonId;
	}

	public void setDungeonId(int dungeonId) {
		this.dungeonId = dungeonId;
	}

	public int getBuffId() {
		return buffId;
	}

	public void setBuffId(int buffId) {
		this.buffId = buffId;
	}

	public int getAddNum() {
		return addNum;
	}

	public void setAddNum(int addNum) {
		this.addNum = addNum;
	}

}
