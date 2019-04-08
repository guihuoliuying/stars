package com.stars.modules.scene.imp.fight;

import com.stars.core.event.Event;
import com.stars.core.module.Module;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.elitedungeon.EliteDungeonManager;
import com.stars.modules.elitedungeon.event.BackToCityFromEliteDungeonEvent;
import com.stars.modules.elitedungeon.event.EliteDungeonAddImageDataEvent;
import com.stars.modules.elitedungeon.event.EliteDungeonDropEvent;
import com.stars.modules.elitedungeon.event.EliteDungeonFinishEvent;
import com.stars.modules.elitedungeon.prodata.EliteDungeonVo;
import com.stars.modules.pk.packet.ClientUpdatePlayer;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.Damage;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientFightTime;
import com.stars.modules.scene.packet.ClientRoleRevive;
import com.stars.modules.scene.packet.ClientSpawnMonster;
import com.stars.modules.scene.packet.ServerAreaSpawnMonster;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDungeon;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterEliteDungeon;
import com.stars.modules.scene.packet.fightSync.ClientSyncAttr;
import com.stars.modules.scene.packet.fightSync.ClientSyncOrder;
import com.stars.modules.scene.packet.fightSync.ServerFightDamage;
import com.stars.modules.scene.packet.fightSync.ServerSyncOrder;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by gaopeidian on 2017/4/7.
 */
public class EliteDungeonScene extends FightScene {
    private int eliteDungeonId;// 精英副本id
    protected List<Long> memberRoleIds = new LinkedList<>();
    public Map<String, Integer> damageMap = new HashMap<>();// 玩家造成伤害统计 <unqueId, damage>
    public int teamId;// 队伍Id
    private Set<String> deadRobotUids = new HashSet<String>();    
    boolean isForceOut = false;//是否已经被强制退出战斗场景过，倒计时强拉回主城时用

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object eliteDungeonId) {
        EliteDungeonVo eliteDungeonVo = EliteDungeonManager.getEliteDungeonVo((Integer) eliteDungeonId);
        if (eliteDungeonVo == null)
            return false;
        this.eliteDungeonId = (int) eliteDungeonId;
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object teamDungeonId) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        this.startTimestamp = System.currentTimeMillis();
        //this.stageStatus = SceneManager.STAGE_PROCEEDING;
        this.stageStatus = SceneManager.STAGE_PAUSE;
        this.setSceneType(stageVo.getStageType());
        ClientEnterEliteDungeon enterEliteDungeon = new ClientEnterEliteDungeon();
        enterEliteDungeon.setStageId(stageId);
        enterEliteDungeon.setFightType(stageVo.getStageType());
        /* 副本失败时间 */
        if(stageVo.getFailConMap().containsKey(SceneManager.FAIL_CONDITION_TIME)){
        	enterEliteDungeon.setFailTime(stageVo.getFailConMap().get(SceneManager.FAIL_CONDITION_TIME));
        }
        /* 初始化怪物 */
        initMonsterData(moduleMap, enterEliteDungeon, stageVo);
        enterEliteDungeon.setFighterEntityList(entityMap.values());
        /* 动态阻挡数据 */
        initDynamicBlockData(enterEliteDungeon, stageVo);
        sendPacketToTeamMembers(enterEliteDungeon, -1);
    }

    public com.stars.network.server.packet.Packet startFightTime() {
        if (stageStatus == SceneManager.STAGE_PAUSE) {
            stageStatus = SceneManager.STAGE_PROCEEDING;
            startTimestamp = System.currentTimeMillis() - spendTime;
            spendTime = 0;
        }
        //这里改为了Math.ceil，原为floor, 因为计时是按>=0来结算的;
        //ClientFightTime packet = new ClientFightTime((int) Math.ceil((System.currentTimeMillis() - startTimestamp) / 1000.0));
        //这里再改为了Math.round
        ClientFightTime packet = new ClientFightTime((int) Math.round((System.currentTimeMillis() - startTimestamp) / 1000.0));
        return packet;
    }
    
    @Override
    public void exit(Map<String, Module> moduleMap) {
    	
    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        this.endTimestamp = System.currentTimeMillis();
      
        EliteDungeonFinishEvent event = new EliteDungeonFinishEvent(eliteDungeonId, stageStatus);
        //传入所有玩家对怪物的总伤害
        event.setDamageMap(damageMap);
        event.setSpendTime((int) ((endTimestamp - startTimestamp) / 1000));
        event.setStageId(stageId);
        sendEventToTeamMembers(event, -1);
        //记录镜像数据
        if(finish==SceneManager.STAGE_VICTORY){
        	EliteDungeonAddImageDataEvent addEvent = new EliteDungeonAddImageDataEvent();
        	addEvent.setStageId(eliteDungeonId);
        	for (long roleId : memberRoleIds) {        		
        		ServiceHelper.roleService().notice(roleId, addEvent);
        	}
        }
        //ServiceHelper.eliteDungeonService().removeFightScene(teamId);
    }

    @Override
    public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {
        // 刷怪数据
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
    public void selfDead(Map<String, Module> moduleMap) {

    }

    @Override
    public void areaSpawnMonster(Map<String, Module> moduleMap, int spawnId) {
        if (!areaSpawnStateMap.get(spawnId)) {
            ClientSpawnMonster clientSpawnMonster = new ClientSpawnMonster();
            clientSpawnMonster.setBlockStatusMap(openBlock(spawnId));
            clientSpawnMonster.setSpawnMonsterMap(spawnMonster(moduleMap, spawnId));
            clientSpawnMonster.setDestroyTrapMonsterList(destroyTrapMonster(spawnId));
            areaSpawnStateMap.put(spawnId, true);
            sendPacketToTeamMembers(clientSpawnMonster, -1);
        }
    }

    @Override
    protected void initMonsterData(Map<String, Module> moduleMap, ClientEnterDungeon enterFightPacket, Object obj) {
        super.initMonsterData(moduleMap, enterFightPacket, obj);
        if (enterFightPacket instanceof ClientEnterEliteDungeon) {
            StageinfoVo stageVo = SceneManager.getStageVo(stageId);
            short spawnMonsterNum = 0;// 2016.11.10策划需求改为刷怪波数
            // 刷怪波数 = 刷怪组数量 + 区域刷怪数量
            spawnMonsterNum = (short) (spawnMonsterNum + stageVo.getMonsterSpawnIdList().size() + areaSpawnStateMap.size());
            ClientEnterEliteDungeon enterPacket = (ClientEnterEliteDungeon) enterFightPacket;
            enterPacket.setSpawnMonsterNumber(spawnMonsterNum);
        }
    }

    // 刷怪
    @Override
    public Map<String, FighterEntity> spawnMonster(Map<String, Module> moduleMap, int monsterSpawnId) {
        Map<String, FighterEntity> resultMap = new HashMap<>();
        spawnSeq++;
        spawnMapping.put(getSpawnUId(monsterSpawnId), new LinkedList<String>());
        notTrapMonsterMap.put(getSpawnUId(monsterSpawnId), new LinkedList<String>());
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
            entityMap.put(monsterUniqueId, monsterEntity);
            resultMap.put(monsterUniqueId, monsterEntity);

            spawnMapping.get(getSpawnUId(monsterSpawnId)).add(monsterUniqueId);
            // 不是陷阱怪物 
            if (monsterAttrVo.getIsTrap() == 0) {
                notTrapMonsterMap.get(getSpawnUId(monsterSpawnId)).add(monsterUniqueId);
            }else if (monsterAttrVo.getIsTrap() == 1) {//是陷阱怪
            	//添加到陷阱的怪的集合
                trapMonsterMap.put(monsterUniqueId, monsterAttrVo.getStageMonsterId());
			}
        }
        return resultMap;
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

    public void setMemberRoleIds(List<Long> memberRoleIds){
    	this.memberRoleIds = memberRoleIds;
    }
    
    /**
     * 加入队伍成员的属性
     *
     * @param collection
     */
    public void addTeamMemberFighter(Collection<BaseTeamMember> collection) {
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
                    }else{
                    	newEntity.setFighterType(FighterEntity.TYPE_ROBOT);
                    	newEntity.setIsRobot(true);
                    }
                }
                entityMap.put(entity.getUniqueId(), newEntity);
            }
        }
    }

    /**
     * 给队员发包
     *
     * @param packet
     * @param exceptRoleId
     */
    protected void sendPacketToTeamMembers(Packet packet, long exceptRoleId) {
        for (long roleId : memberRoleIds) {
            if (roleId != exceptRoleId)
                PlayerUtil.send(roleId, packet);
        }
    }

    /**
     * 通知队员事件
     *
     * @param event
     * @param exceptRoleId
     */
    protected void sendEventToTeamMembers(Event event, long exceptRoleId) {
        for (long roleId : memberRoleIds) {
            if (roleId != exceptRoleId)
                ServiceHelper.roleService().notice(roleId, event);
        }
    }

    /**
     * 处理客户端上传伤害包
     *
     * @param list
     */
    private void dealFightDamage(List<Damage> list) {
        List<String> deadSelfIds = new LinkedList<>();// 我方死亡实体id
        List<String> deadEnemyIds = new LinkedList<>();// 敌方死亡实体id
        Map<String, Integer> curHpMap = new HashMap<>();
        for (Damage damage : list) {
            if (!entityMap.containsKey(damage.getReceiverId()))
                continue;
            // todo:验证伤害值
            FighterEntity receiver = entityMap.get(damage.getReceiverId());
            FighterEntity giver = entityMap.get(damage.getGiverId());
            String receiverUniqueId = receiver.getUniqueId();
            // 统计玩家造成伤害
            if (giver.getFighterType() == FighterEntity.TYPE_PLAYER && damage.getValue() < 0) {
                collectPlayerDamage(giver.getUniqueId(), damage.getValue());
            } else if (giver.getFighterType() == FighterEntity.TYPE_BUDDY && damage.getValue() < 0) {
                String masterRoleId = entityMap.get(giver.getUniqueId()).getMasterUId();
                collectPlayerDamage(masterRoleId, damage.getValue());
            }else if (giver.getFighterType() == FighterEntity.TYPE_ROBOT && damage.getValue() < 0) {
            	String uniqueId = giver.getUniqueId();
            	if(uniqueId.indexOf('r')!=-1){
            		uniqueId = uniqueId.substring(uniqueId.indexOf('r')+1);
            	}
                collectPlayerDamage(uniqueId, damage.getValue());
            }
//            if(receiver.getFighterType() == FighterEntity.TYPE_ROBOT){
//            	if(receiverUniqueId.indexOf('r')!=-1){
//            		receiverUniqueId = receiverUniqueId.substring(receiverUniqueId.indexOf('r')+1);
//            	}
//            }
            // 血量变化前受害者已死亡
            if (receiver.isDead()) {
                curHpMap.put(receiverUniqueId, 0);
                continue;
            }
            // 受害者血量变化
            receiver.changeHp(damage.getValue());
            curHpMap.put(receiverUniqueId, receiver.getAttribute().getHp());
//            System.out.println("血量：：： "+curHpMap.toString());
            if (receiver.isDead()) {
                if (receiver.getCamp() == FighterEntity.CAMP_ENEMY) {// 敌方死亡
                    deadEnemyIds.add(damage.getReceiverId());
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

    /**
     * 我方/中立方 死亡
     *
     * @param uIdList
     */
    protected void selfDead(List<String> uIdList) {
//        checkFinish(uIdList);
//        if (stageStatus != SceneManager.STAGE_PROCEEDING) {
//            finishDeal(null, stageStatus);
//            return;
//        }
        for (String uniqueId : uIdList) {
            this.deadTimeMap.put(uniqueId, System.currentTimeMillis());
        }
    }

    public boolean isIn(long roleId){
    	if (memberRoleIds == null) return false;
    	return memberRoleIds.contains(roleId);
    }
    
    public void exit(long roleId) {
        memberRoleIds.remove(roleId);
        ClientUpdatePlayer packet = new ClientUpdatePlayer();
        List<String> removeFighter = new ArrayList<>();
        removeFighter.add(Long.toString(roleId));
        packet.setRemoveFighter(removeFighter);
        sendPacketToTeamMembers(packet, -1);
    }
    
    public void dead(long roleId , int teamDungeonId){
    	memberRoleIds.remove(roleId);
    	//无失败奖励，这里不用发event通知module处理失败奖励
//        int damage = getDamage(roleId);
//        DeadInTeamDungeonEvent event = new DeadInTeamDungeonEvent(teamDungeonId, damage);
//        event.setStageId(stageId);
//        event.setSpendTime((int) ((System.currentTimeMillis() - startTimestamp) / 1000));
//        ServiceHelper.roleService().notice(roleId, event);
    }

    private int getDamage(long roleId){
		String myUniqueId = Long.toString(roleId);
		int damage = 0;
		if (damageMap.containsKey(myUniqueId)) {
			damage = damageMap.get(myUniqueId);
		}
		return damage;
    }

    public boolean hasNoPlayer() {
        return memberRoleIds.isEmpty();
    }
    
    public void checkForceBackToCity(){
    	if (!isForceOut 
    			&& (this.stageStatus == SceneManager.STAGE_FAIL || this.stageStatus == SceneManager.STAGE_VICTORY)
    			&& (int) ((System.currentTimeMillis() - endTimestamp) / 1000) >= (EliteDungeonManager.delayTime + 8)) {
			//force back to city
    		sendEventToTeamMembers(new BackToCityFromEliteDungeonEvent(eliteDungeonId) , -1);
    		ServiceHelper.eliteDungeonService().removeFightScene(teamId);
    		
    		BaseTeam team = ServiceHelper.baseTeamService().getTeam(teamId);
            if (team != null) {
            	team.setFight(Boolean.FALSE);
            }
  
            isForceOut = true;
		}
    }
    
    /**
     * 每秒执行一次，用于做一些定时/计时处理的操作
     */
    public void onTime(){
    	//检查死亡的机器人是否到自动复活时间，是的话就让机器人自动复活
    	deadRobotUids.clear();
    	Set<Map.Entry<String, Long>> entrySet = this.deadTimeMap.entrySet();
    	for (Map.Entry<String, Long> entry : entrySet) {
			String uid = entry.getKey();
			FighterEntity entity = this.entityMap.get(uid);
			if (entity != null && entity.getIsRobot()){
				deadRobotUids.add(uid);
			}
		}
    	
    	for (String uid : deadRobotUids) {
			if (checkRevive(uid)) {
				ClientRoleRevive clientRoleRevive = new ClientRoleRevive();
				clientRoleRevive.setSubType((byte)1);
				clientRoleRevive.setReviceRoleId(uid);
				clientRoleRevive.setSuc(true);
				sendPacketToTeamMembers(clientRoleRevive, -1);
			}
		}
    	
    	//时间相关的胜利失败检测
        StageinfoVo stageVo = SceneManager.getStageVo(this.stageId);
        if(stageVo != null){
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
}
