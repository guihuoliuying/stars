package com.stars.modules.scene.imp.fight;

import com.stars.core.event.Event;
import com.stars.core.module.Module;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.familyactivities.invade.event.FamilyInvadeDungeonDropEvent;
import com.stars.modules.pk.packet.ClientUpdatePlayer;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.Damage;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientRoleRevive;
import com.stars.modules.scene.packet.ClientSpawnMonster;
import com.stars.modules.scene.packet.ServerAreaSpawnMonster;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDungeon;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterTeamDungeon;
import com.stars.modules.scene.packet.fightSync.ClientSyncAttr;
import com.stars.modules.scene.packet.fightSync.ClientSyncOrder;
import com.stars.modules.scene.packet.fightSync.ServerFightDamage;
import com.stars.modules.scene.packet.fightSync.ServerSyncOrder;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.teamdungeon.TeamDungeonManager;
import com.stars.modules.teamdungeon.event.DeadInTeamDungeonEvent;
import com.stars.modules.teamdungeon.event.TeamDungeonDropEvent;
import com.stars.modules.teamdungeon.event.TeamDungeonFinishEvent;
import com.stars.modules.teamdungeon.prodata.TeamDungeonVo;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by liuyuheng on 2016/9/21.
 */
public class TeamDungeonScene extends FightScene {
    private int teamDungeonId;// 组队副本id
    protected List<Long> memberRoleIds = new LinkedList<>();
    public Map<String, Integer> damageMap = new HashMap<>();// 玩家造成伤害统计 <unqueId, damage>
    private String protectUId = null;// 守护目标唯一Id
    public int teamId;// 队伍Id
    private Set<String> deadRobotUids = new HashSet<String>();

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object teamDungeonId) {
        TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo((Integer) teamDungeonId);
        if (teamDungeonVo == null)
            return false;
        this.teamDungeonId = (int) teamDungeonId;
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object teamDungeonId) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        this.setSceneType(stageVo.getStageType());
        ClientEnterTeamDungeon enterTeamDungeon = new ClientEnterTeamDungeon();
        enterTeamDungeon.setStageId(stageId);
        enterTeamDungeon.setFightType(stageVo.getStageType());
        /* 初始化怪物 */
        initMonsterData(moduleMap, enterTeamDungeon, stageVo);
        enterTeamDungeon.setFighterEntityList(entityMap.values());
        /* 动态阻挡数据 */
        initDynamicBlockData(enterTeamDungeon, stageVo);
        sendPacketToTeamMembers(enterTeamDungeon, -1);
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
        // 守护目标剩余血量百分比值
        byte hpRemain = getProtectRemainHp();
        
        TeamDungeonFinishEvent event = new TeamDungeonFinishEvent(teamDungeonId, stageStatus, hpRemain);
        //传入所有玩家对怪物的总伤害
        event.setDamageMap(damageMap);
        event.setSpendTime((int) ((endTimestamp - startTimestamp) / 1000));
        event.setStageId(stageId);
        sendEventToTeamMembers(event, -1);
        ServiceHelper.teamDungeonService().removeFightScene(teamId);
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
            if (getSceneType() == SceneManager.SCENETYPE_TEAMDUNGEON) {
                sendEventToTeamMembers(new TeamDungeonDropEvent(dropIdMap), -1);
            } else if (getSceneType() == SceneManager.SCENETYPE_FAMILY_INVADE) {
                sendEventToTeamMembers(new FamilyInvadeDungeonDropEvent(dropIdMap), -1);
            }
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
        if (enterFightPacket instanceof ClientEnterTeamDungeon) {
            StageinfoVo stageVo = SceneManager.getStageVo(stageId);
            short spawnMonsterNum = 0;// 2016.11.10策划需求改为刷怪波数
            if (protectUId != null) {
                spawnMonsterNum = -1;
            }
            // 刷怪波数 = 刷怪组数量 + 区域刷怪数量
            spawnMonsterNum = (short) (spawnMonsterNum + stageVo.getMonsterSpawnIdList().size() + areaSpawnStateMap.size());
            ClientEnterTeamDungeon enterPacket = (ClientEnterTeamDungeon) enterFightPacket;
            enterPacket.setSpawnMonsterNumber(spawnMonsterNum);
        }
    }

    // 刷怪
    @Override
    public Map<String, FighterEntity> spawnMonster(Map<String, Module> moduleMap, int monsterSpawnId) {
        TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo(teamDungeonId);
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
            // 守护对象
//            if (teamDungeonVo.getTargetmonster() == monsterAttrVo.getStageMonsterId())
//                protectUId = monsterUniqueId;
            if (monsterAttrVo.getCamp() == 3)
                protectUId = monsterUniqueId;
            
            FighterEntity monsterEntity = FighterCreator.create(FighterEntity.TYPE_MONSTER, monsterUniqueId,
                    getSpawnUId(monsterSpawnId), monsterSpawnId, monsterAttrVo, monsterSpawnVo.getAwake(),
                    monsterSpawnVo.getSpawnDelayByIndex(index++), null);
            entityMap.put(monsterUniqueId, monsterEntity);
            resultMap.put(monsterUniqueId, monsterEntity);

            //不是守护目标
            if (monsterUniqueId != protectUId) {
                spawnMapping.get(getSpawnUId(monsterSpawnId)).add(monsterUniqueId);
            }
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
        checkFinish(uIdList);
        if (stageStatus != SceneManager.STAGE_PROCEEDING) {
            finishDeal(null, stageStatus);
            return;
        }
        for (String uniqueId : uIdList) {
            this.deadTimeMap.put(uniqueId, System.currentTimeMillis());
        }
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
        int damage = getDamage(roleId);
        DeadInTeamDungeonEvent event = new DeadInTeamDungeonEvent(teamDungeonId, damage);
        event.setStageId(stageId);
        event.setSpendTime((int) ((System.currentTimeMillis() - startTimestamp) / 1000));
        ServiceHelper.roleService().notice(roleId, event);
    }

    private byte getProtectRemainHp(){
    	byte hpRemain = 0;
        if (protectUId != null) {
            FighterEntity protectEntity = entityMap.get(protectUId);
            hpRemain = (byte) Math.ceil(
                    100.0 * protectEntity.getAttribute().getHp() / protectEntity.getAttribute().getMaxhp());
        }
        return hpRemain;
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
				sendPacketToTeamMembers(new ClientRoleRevive(Long.parseLong(uid), true), -1);
			}
		}
    }
}
