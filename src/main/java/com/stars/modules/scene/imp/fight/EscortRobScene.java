package com.stars.modules.scene.imp.fight;

import com.stars.core.event.Event;
import com.stars.core.module.Module;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.escort.EscortActivityFlow;
import com.stars.modules.escort.EscortConstant;
import com.stars.modules.escort.EscortManager;
import com.stars.modules.escort.event.NoticeServerAddEscortAwardEvent;
import com.stars.modules.escort.prodata.CargoAIVo;
import com.stars.modules.pk.packet.ClientUpdatePlayer;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.Damage;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientSpawnMonster;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.ServerAreaSpawnMonster;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDungeon;
import com.stars.modules.scene.packet.fightSync.ClientSyncAttr;
import com.stars.modules.scene.packet.fightSync.ClientSyncOrder;
import com.stars.modules.scene.packet.fightSync.ServerFightDamage;
import com.stars.modules.scene.packet.fightSync.ServerSyncOrder;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by wuyuxing on 2016/12/15.
 */
public class EscortRobScene extends FightScene {

    private byte escortType;
    private int sectionId;  //镖车ai Id
    private List<Long> memberRoleIds = new LinkedList<>();
    private boolean isSingleMode = false;//单人劫镖模式
    private int teamId;// 队伍Id
    private long leaderId;
    private int leaderRemainRobTimes;
    private boolean isFinish = false;

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
        if(obj == null || "".equals(obj)) return false;
        int sectionId = (int)obj;
        CargoAIVo aiVo = EscortManager.getCargoAiVoByPowerSection(sectionId);
        if(aiVo == null) return false;
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        if(stageVo == null) return;
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;

        ClientEnterDungeon enterFight = new ClientEnterDungeon();
        enterFight.setStageId(stageId);
        enterFight.setFightType(stageVo.getStageType());
        /* 副本失败时间 */
        if(stageVo.getFailConMap().containsKey(SceneManager.FAIL_CONDITION_TIME)){
            enterFight.setFailTime(stageVo.getFailConMap().get(SceneManager.FAIL_CONDITION_TIME));
        }
        /* 初始化怪物 */
        initMonsterData(moduleMap, enterFight, stageVo);
        enterFight.setFighterEntityList(entityMap.values());
        /* 动态阻挡数据 */
        initDynamicBlockData(enterFight, stageVo);
        sendPacketToTeamMembers(enterFight, -1);
    }

    @Override
    public void exit(Map<String, Module> moduleMap) {

    }

    public void exit(long roleId) {
        memberRoleIds.remove(roleId);
        ClientUpdatePlayer packet = new ClientUpdatePlayer();
        List<String> removeFighter = new ArrayList<>();
        removeFighter.add(Long.toString(roleId));
        packet.setRemoveFighter(removeFighter);
        sendPacketToTeamMembers(packet, -1);
    }

    public boolean hasNoPlayer(){
        return memberRoleIds.isEmpty();
    }

    @Override
    public boolean isEnd() {
        return false;
    }

    /**
     * 获得双倍奖励
     */
    private Map<Integer,Integer> getDoubleMap(Map<Integer,Integer> award){
        Map<Integer,Integer> doubleMap = new HashMap<>();
        for(Map.Entry<Integer,Integer> entry:award.entrySet()){
            doubleMap.put(entry.getKey(),entry.getValue() * 2);
        }
        return doubleMap;
    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        if(isFinish) return;
        if(finish == SceneManager.STAGE_VICTORY){//胜利
            CargoAIVo aiVo = EscortManager.getCargoAiVoByPowerSection(sectionId);
            Map<Integer,Integer> robAward = aiVo == null ? null:aiVo.getAwardMap();
            if(EscortActivityFlow.isStarted()){//活动时间内翻倍
                robAward = getDoubleMap(robAward);
            }

            NoticeServerAddEscortAwardEvent event = new NoticeServerAddEscortAwardEvent(EscortConstant.SUB_TYPE_ROB_SUCCESS,robAward);
            sendEventToTeamMembers(event,-1);

            ClientStageFinish packet = new ClientStageFinish(SceneManager.SCENETYPE_ESCORT_FIGHT, EscortConstant.RESULT_ROB_SUCCESS);
            packet.setItemMap(robAward);
            packet.setDoubleAward(EscortActivityFlow.isStarted()?(byte)1:(byte)0);
            sendPacketToTeamMembers(packet,-1);
        }else{//失败
            ClientStageFinish packet = new ClientStageFinish(SceneManager.SCENETYPE_ESCORT_FIGHT, EscortConstant.RESULT_ROB_FAIL);
            sendPacketToTeamMembers(packet,-1);
        }
        isFinish = true;
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
            FighterEntity receiver = entityMap.get(damage.getReceiverId());
            FighterEntity giver = entityMap.get(damage.getGiverId());
            // 血量变化前受害者已死亡
            if (receiver.isDead()) continue;
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
            sendPacketToTeamMembers(clientSyncAttr, -1);
        }
        if (!deadEnemyIds.isEmpty())
            enemyDead(null, deadEnemyIds);
        if (!deadSelfIds.isEmpty())
            selfDead(deadSelfIds);

        // 胜利失败检测
        checkFinish(null);
        if (stageStatus != SceneManager.STAGE_PROCEEDING) {
            finishDeal(null, stageStatus);
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

    /**
     * 我方/中立方 死亡
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
            if (monsterAttrVo.getIsTrap() == 0) {
                //添加到统计非陷阱怪物的集合
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

    /**
     * 给队员发包
     */
    protected void sendPacketToTeamMembers(Packet packet, long exceptRoleId) {
        for (long roleId : memberRoleIds) {
            if (roleId != exceptRoleId)
                PlayerUtil.send(roleId, packet);
        }
    }

    /**
     * 通知队员事件
     */
    protected void sendEventToTeamMembers(Event event, long exceptRoleId) {
        for (long roleId : memberRoleIds) {
            if (roleId != exceptRoleId)
                ServiceHelper.roleService().notice(roleId, event);
        }
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public boolean isSingleMode() {
        return isSingleMode;
    }

    public void setSingleMode(boolean isSingleMode) {
        this.isSingleMode = isSingleMode;
    }

    public byte getEscortType() {
        return escortType;
    }

    public void setEscortType(byte escortType) {
        this.escortType = escortType;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    /**
     * 加入队伍成员的战斗实体
     */
    public void addTeamMemberFighter(Collection<BaseTeamMember> collection) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        if(stageVo == null) return;
        for (BaseTeamMember teamMember : collection) {
            FighterEntity newEntity = teamMember.getRoleEntity().copy();
            // 玩家注入出生位置/朝向
            if (newEntity != null && newEntity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                newEntity.setPosition(stageVo.getPosition());
                newEntity.setRotation(stageVo.getRotation());
                if (teamMember.isPlayer()) {
                    memberRoleIds.add(Long.parseLong(newEntity.getUniqueId()));
                }
            }
            entityMap.put(newEntity.getUniqueId(), newEntity);
        }
    }

    public void addSinglePlayerFighter(long roleId,Map<String, Module> moduleMap,StageinfoVo stageVo){
        /* 玩家实体 */
        FighterEntity playerEntity = FighterCreator.createSelf(moduleMap, FighterEntity.CAMP_SELF);
        playerEntity.setFighterType(FighterEntity.TYPE_PLAYER);
        playerEntity.setPosition(stageVo.getPosition());
        playerEntity.setRotation(stageVo.getRotation());
        memberRoleIds.add(roleId);
        entityMap.put(playerEntity.getUniqueId(), playerEntity);

        /* 出战伙伴 */
        BuddyModule buddyModule = (BuddyModule) moduleMap.get(MConst.Buddy);
        if (buddyModule.getFightBuddyId() != 0) {
            FighterEntity buddyEntity = FighterCreator.create(FighterEntity.TYPE_BUDDY, FighterEntity.CAMP_SELF,
                    buddyModule.getRoleBuddy(buddyModule.getFightBuddyId()));
            entityMap.put(buddyEntity.getUniqueId(), buddyEntity);
        }
    }

    public int getLeaderRemainRobTimes() {
        return leaderRemainRobTimes;
    }

    public void setLeaderRemainRobTimes(int leaderRemainRobTimes) {
        this.leaderRemainRobTimes = leaderRemainRobTimes;
    }

    public long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(long leaderId) {
        this.leaderId = leaderId;
    }

    public List<Long> getMemberRoleIds() {
        return memberRoleIds;
    }
}
