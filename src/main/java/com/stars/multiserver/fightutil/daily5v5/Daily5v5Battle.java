package com.stars.multiserver.fightutil.daily5v5;

import com.stars.core.attr.Attribute;
import com.stars.modules.daily5v5.packet.*;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.pk.packet.ServerOrder;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDaily5v5;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.daily5v5.Daily5v5Manager;
import com.stars.multiserver.daily5v5.Daily5v5RpcHelper;
import com.stars.multiserver.daily5v5.data.*;
import com.stars.multiserver.familywar.knockout.fight.elite.EliteFightTower;
import com.stars.multiserver.fight.ServerOrders;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.multiserver.fightutil.*;
import com.stars.network.PacketUtil;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.RandomUtil;
import com.stars.util.StringUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static com.stars.multiserver.daily5v5.Daily5v5Manager.*;
import static java.lang.Long.parseLong;

//import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleFamilyPoints;
//import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleFightKillCount;
//import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleFightPersonalPoint;
//import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleFightRevive;
//import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleStartTips;
//import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleStat;

/**
 * Created by chenkeyu on 2017-05-04 18:31
 */
public class Daily5v5Battle extends AbstractBattle {
	private BattleData battleData;
	private BattleInfoHandler battleInfoHandler;
    private String camp1Name;
    private String camp2Name;
    private Map<String, Integer> payReviveCountMap;
    private Map<String, EliteFightTower> towerMap = new HashMap<>();
    private Daily5v5BattleStat battleStat;
    private long camp1Id;
    private long camp2Id;
    private MatchingTeamVo team1;
	private MatchingTeamVo team2;
	private int fightServerId;
	private long creatTimestamp;
	private Set<Long> fightingInBattleRoleIdMap; // 正在战斗中的roleId的哈希表
	private boolean isFinishEnd = false;
	private Map<String, int[]> reviveTimeMap = new HashMap<>();

    @Override
    public void onInitFight() {
        this.camp1Id = battleData.getCamp1Id();
        this.camp2Id = battleData.getCamp2Id();
        fight = new Fight();
        fight.setFightServerId(fightServerId);
        fight.setFightId(battleData.getFightId());
        fight.setCamp1Id(camp1Id);
        fight.setCamp2Id(camp2Id);
        camp1Name = battleData.getCamp1Name();
        camp2Name = battleData.getCamp2Name();
        fight.setCamp1FighterMap(battleData.getCamp1FighterMap());
        fight.setCamp2FighterMap(battleData.getCamp2FighterMap());
        fight.setCamp1TotalFightScore(battleData.getCamp1TotalFightScore());
        fight.setCamp2TotalFightScore(battleData.getCamp2TotalFightScore());
        fight.setFightBaseService(Daily5v5RpcHelper.fightBaseService());
        this.fightingInBattleRoleIdMap = new HashSet<>();
        battleStat = battleData.getFightStat();
        for (FighterEntity entity : fight.getFighterMap().values()) {
            long fighterId = parseLong(entity.getUniqueId());
            battleStat.addPersonalStat(fighterId, entity.getName(), fight.getCamp(entity.getUniqueId()));
        }
        fight.initFight(this, battleStat);
        payReviveCountMap = new ConcurrentHashMap<>();
        int maxFightSocre = 0;
        for (FighterEntity entity : fight.getCamp1FighterMap().values()) {
            maxFightSocre = entity.getFightScore() > maxFightSocre ? entity.getFightScore() : maxFightSocre;
        }
        for (FighterEntity entity : fight.getCamp2FighterMap().values()) {
            maxFightSocre = entity.getFightScore() > maxFightSocre ? entity.getFightScore() : maxFightSocre;
        }
        Map<String, FighterEntity> nonPlayerEntity = getMonsterFighterEntity(Daily5v5Manager.STAGEID);
        for (FighterEntity entity : nonPlayerEntity.values()) {
            Byte towerType = Daily5v5Manager.towerTypeMap.get(entity.getSpawnConfigId());
            if (towerType == null) {
                continue;
            }
            printTowerAttr(entity, "计算前|成员最高战力" + maxFightSocre);
            entity.getAttribute().setAttack((int) (entity.getAttribute().getAttack() / paramValue * maxFightSocre * Daily5v5Manager.coefficient_attack));
            entity.getAttribute().setHp((int) (entity.getAttribute().getHp() / paramValue * maxFightSocre * Daily5v5Manager.coefficient_hp));
            entity.getAttribute().setMaxhp((int) (entity.getAttribute().getMaxhp() / paramValue * maxFightSocre * Daily5v5Manager.coefficient_hp));
            entity.getAttribute().setDefense((int) (entity.getAttribute().getDefense() / paramValue * maxFightSocre * Daily5v5Manager.coefficient_defense));
            entity.getAttribute().setHit((int) (entity.getAttribute().getHit() / paramValue * maxFightSocre * Daily5v5Manager.coefficient_hit));
            entity.getAttribute().setAvoid((int) (entity.getAttribute().getAvoid() / paramValue * maxFightSocre * Daily5v5Manager.coefficient_avoid));
            entity.getAttribute().setCrit((int) (entity.getAttribute().getCrit() / paramValue * maxFightSocre * Daily5v5Manager.coefficient_crit));
            entity.getAttribute().setAnticrit((int) (entity.getAttribute().getAnticrit() / paramValue * maxFightSocre * Daily5v5Manager.coefficient_anticrit));
            printTowerAttr(entity, "计算后");
            EliteFightTower tower = new EliteFightTower(entity.getUniqueId(), entity.getCamp(), towerType, entity.getPosition(), entity.getAttribute().getMaxhp());
            towerMap.put(tower.getUid(), tower);
        }
        fight.startFight(FightConst.T_DAILY_5V5, createEnterEliteFightPacket(), battleData.getArgs());
        fight.addMonster(FightConst.T_DAILY_5V5, nonPlayerEntity);
        //通知玩家进入战斗场景
//        ClientFamilyWarBattleStartTips packet = new ClientFamilyWarBattleStartTips();
//        LogUtil.info("familywar|通知阵营1玩家:{}|进入精英战场", fight.getCamp1FighterMap().keySet());
//        for (String fighterId : fight.getCamp1FighterMap().keySet()) {
//        	battleInfoHandler.sendPacketEvent(getMainServerId(fighterId), parseLong(fighterId), packet);
//        }
//        LogUtil.info("familywar|通知阵营2玩家:{}|进入精英战场", fight.getCamp2FighterMap());
//        for (String fighterId : fight.getCamp2FighterMap().keySet()) {
//        	battleInfoHandler.sendPacketEvent(getMainServerId(fighterId), parseLong(fighterId), packet);
//        }
        //给内塔跟基地加上无敌的buff
        addInvincibleBuff();
    }
    
    public void handleClientPreloadFinished(long roleId){
    	byte camp = fight.getCamp(String.valueOf(roleId));
    	MatchingTeamVo myTeam = null;
    	if(camp==Daily5v5Manager.CAMP1){
    		myTeam = team1;
    	}else{
    		myTeam = team2;
    	}
    	List<Daily5v5MatchingVo> memberList = myTeam.getMemberList();
    	int size = memberList.size();
    	Daily5v5MatchingVo matchingVo = null;
    	for(int i=0;i<size;i++){
    		matchingVo = memberList.get(i);
    		if(matchingVo.getRoleId()==roleId){
    			break;
    		}
    	}
    	if(matchingVo==null) return;
    	//下发主动技能buff
    	Map<Integer, Daily5v5BuffInfo> initiativeBuff = matchingVo.getInitiativeBuff();
    	Iterator<Entry<Integer, Daily5v5BuffInfo>> ibIterator = initiativeBuff.entrySet().iterator();
    	Entry<Integer, Daily5v5BuffInfo> entry = null;
//    	for(;ibIterator.hasNext();){
//    		entry = ibIterator.next();
//    		ServerOrder order = ServerOrders.newAddBuffOrder(camp, ServerOrder.NONE, entry.getValue().getBuffId(), entry.getValue().getBuffLevel());
//    		order.setUniqueId(String.valueOf(roleId));
//    		fight.sendServerOrder(battleData.getFightId(), order, FightConst.T_DAILY_5V5);
//    	}
    	//被动天使祝福
//    	Daily5v5BuffInfo daily5v5BuffInfo = matchingVo.getPassivityBuff().get(Daily5v5Manager.ANGLE_PRAY);
//    	ServerOrder order = ServerOrders.newAddBuffOrder(camp, ServerOrder.NONE, daily5v5BuffInfo.getBuffId(), daily5v5BuffInfo.getBuffLevel());
//		order.setUniqueId(String.valueOf(roleId));
//		fight.sendServerOrder(battleData.getFightId(), order, FightConst.T_DAILY_5V5);
    	//buff增益
    	checkBuffAndAdd(String.valueOf(roleId), Daily5v5Manager.BUFF_ADD, true, false);
    }
    
    public void handleUseBuff(long roleId, int effectId){
    	PvpExtraEffect pvpExtraEffect = Daily5v5Manager.pvpExtraEffectMap.get(effectId);
    	int effectType = pvpExtraEffect.getEffecttype();
    	byte camp = fight.getCamp(String.valueOf(roleId));
    	List<Daily5v5MatchingVo> memberList = null;
    	if(camp==Daily5v5Manager.CAMP1){
    		memberList = team1.getMemberList();
    	}else{
    		memberList = team2.getMemberList();
    	}
    	ClientDaily5v5 packet = new ClientDaily5v5(Daily5v5Manager.USE_BUFF_RESULT);
    	int size = memberList.size();
    	Daily5v5MatchingVo matchingVo = null;
    	for(int i=0;i<size;i++){
    		matchingVo = memberList.get(i);
    		if(matchingVo.getRoleId()==roleId){
    			break;
    		}
    	}
    	if(matchingVo==null){
    		packet.setResult((byte)0);
    		Daily5v5RpcHelper.roleService().send(getMainServerId(String.valueOf(roleId)), roleId, packet);
    		return;
    	}
    	Map<Integer, Daily5v5BuffInfo> initiativeBuff = matchingVo.getInitiativeBuff();
    	if(!initiativeBuff.containsKey(effectType)){
    		packet.setResult((byte)0);
    		Daily5v5RpcHelper.roleService().send(getMainServerId(String.valueOf(roleId)), roleId, packet);
    		return;
    	}
    	Map<Integer, Integer> buffCD = matchingVo.getBuffCD();
    	Daily5v5BuffInfo daily5v5BuffInfo = initiativeBuff.get(effectType);
    	int[] paramArr = daily5v5BuffInfo.getParamArr();
    	int nowTime = (int)(System.currentTimeMillis()/1000);
    	if(buffCD.containsKey(effectType)){
    		Integer time = buffCD.get(effectType);
    		if((nowTime-time)<paramArr[paramArr.length-1]){
    			packet.setResult((byte)2);
        		Daily5v5RpcHelper.roleService().send(getMainServerId(String.valueOf(roleId)), roleId, packet);
    			return;
    		}
    	}
    	buffCD.put(effectType, nowTime);
    	ServerOrder order = ServerOrders.newAddBuffOrder(camp, ServerOrder.CHARACTER_TYPE_MONSTER, daily5v5BuffInfo.getBuffId(), 1);
		fight.sendServerOrder(battleData.getFightId(), order, FightConst.T_DAILY_5V5);
		
    	packet.setBuffId(effectId);
    	packet.setResult((byte)1);
    	Daily5v5RpcHelper.roleService().send(getMainServerId(String.valueOf(roleId)), roleId, packet);
    	byte enermy = 0;
    	for(Long sendRoleId : fightingInBattleRoleIdMap){
    		if(camp == fight.getCamp(String.valueOf(sendRoleId))){
    			enermy = 0;
    		}else{
    			enermy = 1;
    		}
    		Daily5v5RpcHelper.daily5v5Service().announce(getMainServerId(String.valueOf(sendRoleId)), sendRoleId, 
    				matchingVo.getRoleName(), enermy);
    	}
    }

    @Override
    public void enterFight(int mainServerId, long campId, long roleId) {
        sendStatPacket(campId, roleId, Long.toString(roleId));
        fight.enterFight(mainServerId, campId, roleId, STAGEID, FightConst.T_DAILY_5V5);
    }

    @Override
    public void handleDead(String victimUid, String attackerUid) {
        if (towerMap.containsKey(victimUid)) {
            EliteFightTower tower = towerMap.get(victimUid);
            if (tower.getType() == Daily5v5Manager.K_TOWER_TYPE_CRYSTAL) {
                handleCrystalDead(tower, attackerUid);
            } else {
                handleTowerDead(tower, attackerUid);
            }
        } else { // 玩家
            handleFighterDead(victimUid, attackerUid);
        }
    }
    
    public final void handleFighterQuit(long roleId) {
        fightingInBattleRoleIdMap.remove(roleId);
    }

    public final void handleFighterEnter(long roleId) {
        fightingInBattleRoleIdMap.add(roleId);
        LogUtil.info("daily5v5|add player[{}] into fightingInBattleRoleIdMap={}", roleId, fightingInBattleRoleIdMap);
    }
    
    /**
     * 是否在战斗
     */
    public final boolean checkIsFighting(long roleId){
    	return fightingInBattleRoleIdMap.contains(roleId);
    }

    @Override
    public void handleDamage(String victimUid, Map<String, Integer> victimSufferedDamageMap) {
		if (battleData.getCamp1FighterMap().containsKey(victimUid) || battleData.getCamp2FighterMap().containsKey(victimUid)) {
			if (!fight.getSufferedDamageMap().containsKey(victimUid)) {
				fight.getSufferedDamageMap().put(victimUid, toStringLongMap(victimSufferedDamageMap));
			} else {
				MapUtil.add(fight.getSufferedDamageMap().get(victimUid), toStringLongMap(victimSufferedDamageMap));
			}
		}
        if (towerMap.containsKey(victimUid)) {
            EliteFightTower tower = towerMap.get(victimUid);
            if (tower != null) {
                for (int demage : victimSufferedDamageMap.values()) {
                    tower.reduceHp(demage);
                }
            }
        }
    }
    
    private Map<String, Long> toStringLongMap(Map<String, Integer> map) {
        Map<String, Long> newMap = new HashMap<>();
        for (Entry<String, Integer> entry : map.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue().longValue());
        }
        return newMap;
    }

    @Override
    public void handleRevive(String fighterUid) {
        if (StringUtil.isEmpty(fight.getReviveMap())) return;
        try {
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<String, Long>> iterator = fight.getReviveMap().entrySet().iterator();
            Map.Entry<String, Long> entry;
            int passTime = 0;
            while (iterator.hasNext()) {
                entry = iterator.next();
                if(!checkIsFighting(Long.parseLong(entry.getKey()))) continue;
                int[] info = reviveTimeMap.get(entry.getKey());
                if(info==null) continue;
                passTime = (int)(now/1000 - entry.getValue()/1000)+1;//加1是为了保证玩家能复活
                if(passTime>=info[1]){                	
                	revive(entry.getKey(), homeRevive);
                }
            }
        } catch (Exception e) {
            LogUtil.error("daily5v5|家族战复活异常:" + e.getMessage());
        }
    }
    
    public void handChangeConn(String fighterUid){
    	//下发更新塔信息
    	ClientDaily5v5FightInitInfo packet = new ClientDaily5v5FightInitInfo();
    	packet.setTowerMap(towerMap);
    	Daily5v5RpcHelper.roleService().send(getMainServerId(fighterUid), Long.valueOf(fighterUid), packet);
    	sendFightUpdateInfo(Long.parseLong(fighterUid));
    }
    
    /**
     * 超时结束处理
     */
    public void handleTimeOut(){
    	//超时胜利判定
    	//哪队的塔多 胜利
//    	int camp1TowNum = 0;
//    	int camp2TowNum = 0;
//    	Iterator<EliteFightTower> iterator = towerMap.values().iterator();
//    	EliteFightTower eliteFightTower = null;
//    	for(;iterator.hasNext();){
//    		eliteFightTower = iterator.next();
//    		if(eliteFightTower.getCamp()==Daily5v5Manager.CAMP1){
//    			camp1TowNum += 1;
//    		}else{
//    			camp2TowNum +=1;
//    		}
//    	}
    	long winTeam = 0;
    	long loseTeam = 0;
//    	if(camp1TowNum>camp2TowNum){
//    		winTeam = camp1Id;
//    		loseTeam = camp2Id;
//    	}else if(camp2TowNum>camp1TowNum){
//    		winTeam = camp2Id;
//    		loseTeam = camp1Id;
//    	}else{
//    		//士气值高 胜利
//    		if(fight.getStat().getCamp1Morale()>fight.getStat().getCamp2Morale()){
//    			winTeam = camp1Id;
//	    		loseTeam = camp2Id;
//    		}else if(fight.getStat().getCamp2Morale()>fight.getStat().getCamp1Morale()){
//    			winTeam = camp2Id;
//        		loseTeam = camp1Id;
//    		}else{
//    			//总战力高  胜利
//    			if(battleData.getTotalFight1()>battleData.getTotalFight2()){
//    				winTeam = camp1Id;
//    	    		loseTeam = camp2Id;
//    			}else if(battleData.getTotalFight2()>battleData.getTotalFight1()){
//    				winTeam = camp2Id;
//    	    		loseTeam = camp1Id;
//    			}
//    		}    		
//    	}
    	if(fight.getStat().getCamp1TotalPoints()>fight.getStat().getCamp2TotalPoints()){
			winTeam = camp1Id;
    		loseTeam = camp2Id;
    	}else if(fight.getStat().getCamp2TotalPoints()>fight.getStat().getCamp1TotalPoints()){
			winTeam = camp2Id;
    		loseTeam = camp1Id;
    	}else{
    		int rand = RandomUtil.rand(1, 2);
    		if(rand==1){
    			fight.getStat().setCamp1TotalPoints(fight.getStat().getCamp1TotalPoints()+300);
    			winTeam = camp1Id;
        		loseTeam = camp2Id;
    		}else{
    			fight.getStat().setCamp2TotalPoints(fight.getStat().getCamp2TotalPoints()+300);
    			winTeam = camp2Id;
        		loseTeam = camp1Id;
    		}
    	}
    	finishFight(winTeam, loseTeam, (byte)2);
    }

    @Override
    public void stopFight() {
        fight.stopFight(FightConst.T_DAILY_5V5);
    }

    @Override
    public FightResult endFight() {
        return fight.endFight();
    }

    private void revive(String fighterUid, byte reqType) {
        if (!fight.getReviveMap().containsKey(fighterUid)) return;
        FighterEntity entity = fight.getFighterMap().get(fighterUid);
        if (entity == null) return;
        entity.setFighterType(FighterEntity.TYPE_PLAYER);
        entity.setState((byte) 1);
        byte camp = fight.getCamp(fighterUid);
        entity.setCamp(camp);
        if (reqType == homeRevive) {//安全复活
            StageinfoVo stageVo = SceneManager.getStageVo(STAGEID);
            if (camp == Daily5v5Manager.CAMP1) { //设置出生点复活
                entity.setPosition(stageVo.getPosition());
                entity.setRotation(stageVo.getRotation());
            } else {
                entity.setPosition(stageVo.getEnemyPos(0));
                entity.setRotation(stageVo.getEnemyRot(0));
            }
            fight.handleRevive(fighterUid, FightConst.T_DAILY_5V5);
            fight.getReviveMap().remove(fighterUid);
            //检测buff
            checkBuffAndAdd(fighterUid, Daily5v5Manager.HOME_GUARDIAN, true, true);
            sendAgainBuff(fighterUid);
        } 
//        else if (reqType == payRevive) {//原地复活（付费）
//
//        }
    }
    
    private void sendAgainBuff(String fighterUid){
    	List<Daily5v5MatchingVo> memberList = null;
    	if(fight.getCamp1FighterMap().containsKey(fighterUid)){
    		memberList = team1.getMemberList();
    	}else{
    		memberList = team2.getMemberList();
    	}
    	Daily5v5MatchingVo matchingVo = null;
    	int size = memberList.size();
    	for(int i=0;i<size;i++){
    		matchingVo = memberList.get(i);
    		if(matchingVo.getRoleId()==Long.parseLong(fighterUid)){
    			break;
    		}
    	}
    	if(matchingVo==null) return;
    	Map<Integer, Set<Integer>> passivityBuffCd = matchingVo.getPassivityBuffCd();
    	int addSize = 0;
    	if(StringUtil.isNotEmpty(passivityBuffCd)){
    		Iterator<Integer> iterator = passivityBuffCd.keySet().iterator();
    		int effectType = 0;
    		for(;iterator.hasNext();){
    			effectType = iterator.next();
    			addSize = passivityBuffCd.get(effectType).size();
    			for(int i=0;i<addSize;i++){    				
    				checkBuffAndAdd(fighterUid, effectType, true, true);
    			}
    		}
    	}
    }

    private void handleCrystalDead(EliteFightTower tower, String attackerUid) {
        long winnerTeamId = fight.getCampId(attackerUid);
        long loserTeamId = winnerTeamId == camp1Id ? camp2Id : camp1Id;
        finishFight(winnerTeamId, loserTeamId, (byte)1);
    }

    private void handleTowerDead(EliteFightTower tower, String attackerUid) {
        // 检查状态
        // 强制将血量改成0
        tower.setHp(0);
        // 加积分，加士气
        updateStatMorale(fight.getCampId(attackerUid), moraleDeltaOfDestoryTower); // 己方增加士气
        updateStatMorale(fight.getOpponentCampId(fight.getCampId((attackerUid))), -moraleDeltaOfLosingTower); // 对方减少士气
        battleInfoHandler.updateElitePoints(attackerUid, pointsDeltaOfDestoryTower); // 积分
        battleStat.updatePersonalStat(getMainServerId(attackerUid), parseLong(attackerUid), 0, 0, 0, 0, pointsDeltaOfDestoryTower);
        if(tower.getCamp()==Daily5v5Manager.CAMP2){                        	
        	battleStat.updateCampPoints(camp1Id, pointsDeltaOfDestoryTower);
        }else{
        	battleStat.updateCampPoints(camp2Id, pointsDeltaOfDestoryTower);                        	
        }
        syncPersonalPoints(attackerUid, battleData.getFightStat());
        // 强制同步一次信息
        sendBattleFightUpdateInfo();
        byte camp = tower.getCamp();
        byte category = Daily5v5Manager.K_TOWER_CATEGORY_OUTER;
        if (tower.getType() == Daily5v5Manager.K_TOWER_TYPE_TOP
                || tower.getType() == Daily5v5Manager.K_TOWER_TYPE_MID
                || tower.getType() == Daily5v5Manager.K_TOWER_TYPE_BOT) {
            category = Daily5v5Manager.K_TOWER_CATEGORY_OUTER;
        } else if (tower.getType() == Daily5v5Manager.K_TOWER_TYPE_BASEBOT
                || tower.getType() == Daily5v5Manager.K_TOWER_TYPE_BASETOP) {
            category = Daily5v5Manager.K_TOWER_CATEGORY_INNER;
        }
        removeInvincibleBuff(camp, category);
        //检测buff
        checkBuffAndAdd(tower.getUid(), Daily5v5Manager.TOWER_SOUL, true, false);
    }

    private void handleFighterDead(String victimUid, String attackerUid) {
        // 人杀人
        if (fight.getFighterMap().containsKey(attackerUid) && fight.getFighterMap().containsKey(victimUid)) {
            FighterEntity victimEntity = fight.getFighterMap().get(victimUid);
            FighterEntity attackerEntity = fight.getFighterMap().get(attackerUid);
            // 提示
            if(checkIsFighting(parseLong(attackerUid))){            	
            	Daily5v5RpcHelper.roleService().send(getMainServerId(attackerUid), parseLong(attackerUid),
            			new ClientText(attackerEntity.getName() + "杀死" + victimEntity.getName()));
            }

            // 更新攻击者的连杀数量, 并发送公告
            Integer attackerComboKillCount = fight.getComboKillCountMap().get(attackerUid);
            if (attackerComboKillCount == null) {
                fight.getComboKillCountMap().put(attackerUid, attackerComboKillCount = 1);
            } else {
                fight.getComboKillCountMap().put(attackerUid, attackerComboKillCount += 1);
            }
            if (attackerComboKillCount >= KillNotice) {
                long attackerTeamId = fight.getCampId(attackerUid);
                ClientText packet = new ClientText("fivepvpwar_tips_selfdoublekill", attackerEntity.getName(), attackerComboKillCount.toString());
                for (long roleId : fight.getFighterIdListMap().get(attackerTeamId)) {
                	if(!checkIsFighting(roleId)) continue;
                	Daily5v5RpcHelper.roleService().send(getMainServerId(String.valueOf(roleId)), roleId, packet);
                }

                // 对方
                long victimTeamId = fight.getCampId(victimUid);
                packet = new ClientText("fivepvpwar_tips_enemydoublekill", attackerEntity.getName(), attackerComboKillCount.toString());
                for (long roleId : fight.getFighterIdListMap().get(victimTeamId)) {
                	if(!checkIsFighting(roleId)) continue;
                	Daily5v5RpcHelper.roleService().send(getMainServerId(String.valueOf(roleId)), roleId, packet);
                }
            }
            // 更新受害者的连杀数量(置为0)，并发送公告
            Integer victimComboKillCount = fight.getComboKillCountMap().get(victimUid);
            if (victimComboKillCount != null && victimComboKillCount >= KillNotice) {
                // 己方
                long attackerTeamId = fight.getCampId(attackerUid);
                ClientText packet = new ClientText("fivepvpwar_tips_selfoverdoublekill", attackerEntity.getName(), victimEntity.getName(), victimComboKillCount.toString());
                for (long roleId : fight.getFighterIdListMap().get(attackerTeamId)) {
                	if(!checkIsFighting(roleId)) continue;
                	Daily5v5RpcHelper.roleService().send(getMainServerId(String.valueOf(roleId)), roleId, packet);
                }

                // 对方
                packet = new ClientText("fivepvpwar_tips_enemyoverdoublekill", attackerEntity.getName(), victimEntity.getName(), victimComboKillCount.toString());
                long victimTeamId = fight.getCampId(victimUid);
                for (long roleId : fight.getFighterIdListMap().get(victimTeamId)) {
                	if(!checkIsFighting(roleId)) continue;
                	Daily5v5RpcHelper.roleService().send(getMainServerId(String.valueOf(roleId)), roleId, packet);
                }
            }
            fight.getComboKillCountMap().put(victimUid, 0);

            // 增加士气
            updateStatMorale(fight.getCampId(attackerUid), moraleDeltaOfKillFighterInEliteFight);

            // 计算积分
            Map<String, Long> damageMap = fight.getSufferedDamageMap().get(victimUid);
            if (damageMap != null) {
                long totalDamage = MapUtil.sum(damageMap, 0L);
                for (Map.Entry<String, Long> entry : damageMap.entrySet()) {
                    if (towerMap.containsKey(entry.getKey())) continue;
                    double ratio = (entry.getValue() * 1.0) / totalDamage;
                    if (ratio >= DamagePercent) {
                        long pointsDelta = (long) (ratio * Coefficient_A[0] + Coefficient_A[1]);
                        battleInfoHandler.updateElitePoints(entry.getKey(), pointsDelta);
                        int assistDelta = entry.getKey().equals(attackerUid) ? 0 : 1;
                        battleStat.updatePersonalStat(getMainServerId(entry.getKey()), parseLong(entry.getKey()), 0, 0, assistDelta, 0, pointsDelta);
                        if(battleData.getCamp1FighterMap().containsKey(entry.getKey())){                        	
                        	battleStat.updateCampPoints(camp1Id, pointsDelta);
                        }else{
                        	battleStat.updateCampPoints(camp2Id, pointsDelta);                        	
                        }
                        syncPersonalPoints(entry.getKey(), battleData.getFightStat());
                    }
                }
            }

            // 更新统计信息
            battleStat.updatePersonalStat(getMainServerId(attackerUid), parseLong(attackerUid), 1, 0, 0, attackerComboKillCount, 0);
            battleStat.updatePersonalStat(getMainServerId(victimUid), parseLong(victimUid), 0, 1, 0, 0, 0);
            sendPersonalStat(parseLong(attackerUid));
            sendPersonalStat(parseLong(victimUid));
            sendBattleFightUpdateInfo();

            // 发送双方的连杀包
            if(checkIsFighting(parseLong(attackerUid))){            	
            	Daily5v5RpcHelper.roleService().send(getMainServerId(attackerUid),
            			parseLong(attackerUid), new ClientDaily5v5KillCount(attackerComboKillCount));
            }
            if(checkIsFighting(parseLong(attackerUid))){            	
            	Daily5v5RpcHelper.roleService().send(getMainServerId(victimUid),
            			parseLong(victimUid), new ClientDaily5v5KillCount(0));
            }
            //检测buff
            checkBuffAndAdd(attackerUid, Daily5v5Manager.KILL_AND_ADD_HP, true, true);

        }
        //搭打人
        if (towerMap.containsKey(attackerUid) && fight.getFighterMap().containsKey(victimUid)) {
            //TODO
        }
        // 发送复活框
        long deadTime = System.currentTimeMillis();
        fight.getReviveMap().put(victimUid, deadTime);
        Integer payReviveCount = payReviveCountMap.get(victimUid);
        LogUtil.info(victimUid + "|daily5v5|===================发送复活框数据");
        ClientDaily5v5Revive packet = new ClientDaily5v5Revive();
//        packet.setType(ClientDaily5v5Revive.TYPE_OF_COUNT);
//        packet.setReviveCount(payReviveCount == null ? 0 : payReviveCount.intValue());
        packet.setType(ClientDaily5v5Revive.TYPE_OF_TIME);
        int[] reviveInfo = reviveTimeMap.get(victimUid);
        int reviveTimes = 1;
        if(reviveInfo!=null){
        	reviveTimes = reviveInfo[0]+1;
        }
        Iterator<Integer> iterator = Daily5v5Manager.reliveTimeMap.keySet().iterator();
        int tempAbs = 0;
        int abs = -1;
        int key;
        int selectKey = 1;
        for(;iterator.hasNext();){
        	key = iterator.next();
        	tempAbs = Math.abs(key-reviveTimes);
        	if(abs==-1){
        		abs = tempAbs;
        		selectKey = key;
        	}else if(tempAbs<abs){
        		abs = tempAbs;
        		selectKey = key;
        	}
        }
        int revivetime = Daily5v5Manager.reliveTimeMap.get(selectKey);
        revivetime = getReviveTime(victimUid, revivetime, packet);
        reviveTimeMap.put(victimUid, new int[]{reviveTimes,revivetime});
        Daily5v5RpcHelper.roleService().send(getMainServerId(victimUid), Long.parseLong(victimUid), packet);
    }
    
    private int getReviveTime(String victimUid, int revivetime, ClientDaily5v5Revive packet){
    	Daily5v5MatchingVo matchingVo = checkBuffAndAdd(victimUid, Daily5v5Manager.ANGLE_PRAY, false, true);
    	if(matchingVo==null){
    		packet.setReviveTime(revivetime);
    		packet.setBuffName("");
    		return revivetime;
    	}
    	Daily5v5BuffInfo daily5v5BuffInfo = matchingVo.getPassivityBuff().get(Daily5v5Manager.ANGLE_PRAY);
    	if(daily5v5BuffInfo!=null){    		
    		int[] paramArr = daily5v5BuffInfo.getParamArr();
    		revivetime = revivetime - paramArr[0];
    		if(revivetime<0){
    			revivetime = 0;
    		}
    	}
    	int effectId = daily5v5BuffInfo.getEffectId();
    	PvpExtraEffect pvpExtraEffect = Daily5v5Manager.pvpExtraEffectMap.get(effectId);
    	packet.setReviveTime(revivetime);
        packet.setBuffName(pvpExtraEffect.getName());
        packet.setLevel(1);
        packet.setReduceTime(daily5v5BuffInfo.getParamArr()[0]);
        return revivetime;
    }
    
    private void sendPersonalStat(long roleId){
    	ClientDaily5v5BattleStat packet = new ClientDaily5v5BattleStat();
        FightPersonalStat personalStat = battleStat.getPersonalStatMap().get(roleId);
        packet.setMyKillCount(personalStat.getKillCount());
        packet.setMyDeadCount(personalStat.getDeadCount());
        packet.setMyAssistCount(personalStat.getAssistCount());
        Daily5v5RpcHelper.roleService().send(getMainServerId(String.valueOf(roleId)), roleId, packet);
    }

    private void sendStatPacket(long campId, long roleId, String fighterId) {
        Daily5v5BattleStat fightStat = battleData.getFightStat();
        ClientDaily5v5TeamPoints points = new ClientDaily5v5TeamPoints();
        if (campId == camp1Id) {
            points.setMyTeamPoints(fightStat.getCamp1TotalPoints());
            points.setEnemyTeamPoints(fightStat.getCamp2TotalPoints());
        } else if (campId == camp2Id) {
            points.setMyTeamPoints(fightStat.getCamp2TotalPoints());
            points.setEnemyTeamPoints(fightStat.getCamp1TotalPoints());
        }
        sendPersonalStat(roleId);
        syncPersonalPoints(Long.toString(roleId), fightStat);
        Daily5v5RpcHelper.roleService().send(getMainServerId(String.valueOf(roleId)), roleId, points);
        sendBuffCdInfo(campId, roleId);
        sendReviveCd(roleId);
        handChangeConn(String.valueOf(roleId));
    }
    
    private void sendFightUpdateInfo(long roleId){
    	int camp1Morale = battleStat.getCamp1Morale();
    	int camp2Morale = battleStat.getCamp2Morale();
    	int camp1BuffId = fight.getCamp1BuffId();
        int camp2BuffId = fight.getCamp2BuffId();
        long camp1Points = battleStat.getCamp1TotalPoints();
        long camp2Points = battleStat.getCamp2TotalPoints();
    	ClientDaily5v5FightUpdateInfo packet = new ClientDaily5v5FightUpdateInfo(camp1Morale, camp1BuffId, camp1Points, 
    			camp2Morale, camp2BuffId, camp2Points, towerMap);
    	Daily5v5RpcHelper.roleService().send(getMainServerId(String.valueOf(roleId)), roleId, packet);    		
    }
    
    private void sendBuffCdInfo(long campId, long roleId){
    	List<Daily5v5MatchingVo> memberList = null;
    	if(campId == camp1Id){
    		memberList = team1.getMemberList();
    	}else{
    		memberList = team2.getMemberList();
    	}
    	int size = memberList.size();
    	Daily5v5MatchingVo matchingVo = null;
    	for(int i=0;i<size;i++){
    		matchingVo = memberList.get(i);
    		if(matchingVo.getRoleId()==roleId){
    			break;
    		}
    	}
    	if(matchingVo==null){
    		return;
    	}
    	Map<Integer, Integer> buffCD = matchingVo.getBuffCD();
    	if(StringUtil.isEmpty(buffCD)) return;
    	List<int[]> buffCdList = new ArrayList<>();
    	Map<Integer, Daily5v5BuffInfo> initiativeBuff = matchingVo.getInitiativeBuff();
    	Daily5v5BuffInfo daily5v5BuffInfo = null;
    	long currentTime = System.currentTimeMillis();
    	int nowTime = (int)(currentTime/1000);
    	int cdStartTime = 0;
    	int[] paramArr = null;
    	int passTime = 0;
    	for(int effectType : buffCD.keySet()){
    		initiativeBuff.get(effectType);
    		daily5v5BuffInfo = initiativeBuff.get(effectType);
    		paramArr = daily5v5BuffInfo.getParamArr();
    		cdStartTime = buffCD.get(effectType);
    		passTime = nowTime-cdStartTime;
    		if(passTime<paramArr[paramArr.length-1]){    			
    			buffCdList.add(new int[]{daily5v5BuffInfo.getEffectId(), paramArr[paramArr.length-1]-passTime});
    		}
    	}
    	if(StringUtil.isEmpty(buffCdList)) return;
    	ClientDaily5v5 buffCdPacket = new ClientDaily5v5(Daily5v5Manager.BUFF_CD_INFO);
    	buffCdPacket.setBuffCdList(buffCdList);
    	String roleIdStr = String.valueOf(roleId);
        Daily5v5RpcHelper.roleService().send(getMainServerId(roleIdStr), roleId, buffCdPacket);
    }
    
    private void sendReviveCd(long roleId){
    	ClientDaily5v5 buffCdPacket = new ClientDaily5v5(Daily5v5Manager.REVIVE_CD_INFO);
    	String roleIdStr = String.valueOf(roleId);
    	int[] info = reviveTimeMap.get(roleIdStr);
    	if(info!=null){    		
    		long currentTime = System.currentTimeMillis();
    		Long deadTime = fight.getReviveMap().get(roleIdStr);
    		if(deadTime!=null){    
    			int deadPassTime = (int)((currentTime-deadTime)/1000);
    			int reviveCd = info[1]-deadPassTime;
    			if(reviveCd<0){
    				reviveCd = 0;
    			}
    			buffCdPacket.setReviveCd(reviveCd);
    			Daily5v5RpcHelper.roleService().send(getMainServerId(roleIdStr), roleId, buffCdPacket);
    		}
    	}
    }
    
    /**
     * 更新士气
     *
     * @param campId
     * @param moraleDelta
     */
    public void updateStatMorale(long campId, int moraleDelta) {
        if (campId == camp1Id) {
            int preMorale = battleStat.getCamp1Morale();
            battleStat.updateCampMorale(campId, moraleDelta);
//            ClientDaily5v5Morale packet = new ClientDaily5v5Morale();
//            packet.setMorale(battleStat.getCamp1Morale());
//            Daily5v5RpcHelper.roleService().send(fightServerId, preMorale, packet);
            sendUpdateBuffInfo(preMorale, campId, battleStat.getCamp1Morale());
        } else if (campId == camp2Id) {
            int preMorale = battleStat.getCamp2Morale();
            battleStat.updateCampMorale(campId, moraleDelta);
//            ClientDaily5v5Morale packet = new ClientDaily5v5Morale();
//            packet.setMorale(battleStat.getCamp2Morale());
//            Daily5v5RpcHelper.roleService().send(fightServerId, preMorale, packet);
            sendUpdateBuffInfo(preMorale, campId, battleStat.getCamp2Morale());
        }
    }
    
    public void sendUpdateBuffInfo(int preMorale, long campId, int curMorale){
    	Daily5v5MoraleVo preMoraleVo = Daily5v5Manager.getMoraleVo(preMorale);
    	Daily5v5MoraleVo curMoraleVo = Daily5v5Manager.getMoraleVo(curMorale);
        LogUtil.info("daily5v5|curMoraleVo:{}|preMoraleVo == null || preMoraleVo != curMoraleVo:{}", curMoraleVo != null, preMoraleVo == null || preMoraleVo != curMoraleVo);
        if (curMoraleVo != null) {
            LogUtil.info("daily5v5|campId:{}curMorale:{}|buffId:{}|deBuffId:{}", campId, curMorale, curMoraleVo.getBuffId(), curMoraleVo.getDebuffId());
            if (preMoraleVo == null || preMoraleVo != curMoraleVo) { // 理应比较id的，但暂时没有id
            	if(curMoraleVo.getBuffId()!=0){            		
            		fight.setBuff(campId, ServerOrder.CHARACTER_TYPE_PLAYER, ServerOrder.CHARACTER_TYPE_MONSTER, 
            				curMoraleVo.getBuffId(), curMoraleVo.getDebuffId(), 1, FightConst.T_DAILY_5V5);
            	}
            }
        }
    	sendBattleFightUpdateInfo();
    }
    
    public void sendBattleFightUpdateInfo(){
    	int camp1Morale = battleStat.getCamp1Morale();
    	int camp2Morale = battleStat.getCamp2Morale();
    	int camp1BuffId = fight.getCamp1BuffId();
        int camp2BuffId = fight.getCamp2BuffId();
        long camp1Points = battleStat.getCamp1TotalPoints();
        long camp2Points = battleStat.getCamp2TotalPoints();
    	ClientDaily5v5FightUpdateInfo packet = new ClientDaily5v5FightUpdateInfo(camp1Morale, camp1BuffId, camp1Points, 
    			camp2Morale, camp2BuffId, camp2Points, towerMap);
    	for(Long roleId : fightingInBattleRoleIdMap){
    		Daily5v5RpcHelper.roleService().send(getMainServerId(String.valueOf(roleId)), roleId, packet);    		
    	}
    }

    public int getMainServerId(String fighterUid) {
        Map<String, Integer> fighterSeverIdMap = battleData.getFighterSeverIdMap();
        if(fighterSeverIdMap.containsKey(fighterUid)){
        	return fighterSeverIdMap.get(fighterUid);
        }
        return 0;
    }

    public void syncPersonalPoints(String fighterUid, Daily5v5BattleStat fightStat) {
        long roleId = Long.parseLong(fighterUid);
        long rolePoints = fightStat.getPersonalStatMap().get(roleId).getPoints();
        Daily5v5RpcHelper.roleService().send(
        		getMainServerId(String.valueOf(roleId)), roleId,
                new ClientDaily5v5PersonalPoint(rolePoints));
    }

    private byte[] createEnterEliteFightPacket() {
        StageinfoVo stageVo = SceneManager.getStageVo(Daily5v5Manager.STAGEID);
        ClientEnterDaily5v5 enterPacket = new ClientEnterDaily5v5();
		enterPacket.setFightType(SceneManager.SCENETYPE_DAILY_5V5);
		enterPacket.setStageId(Daily5v5Manager.STAGEID);
		enterPacket.setLimitTime(Daily5v5Manager.FIGHT_TIME_LIMIT);
		enterPacket.setStartRemainderTime(Daily5v5Manager.DYNAMIC_BLOCK_TIME);
		enterPacket.addMonsterVoMap(stageVo.getMonsterVoMap());
        /* 动态阻挡数据 */
        Map<String, Byte> blockStatus = new HashMap<>();
        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
            if (dynamicBlock.getShowSpawnId() == 0) {
                blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
            }
        }
        LogUtil.info("动态阻挡数据Elites:{}", blockStatus);
        enterPacket.setBlockMap(stageVo.getDynamicBlockMap());
        enterPacket.addBlockStatusMap(blockStatus);
        return PacketUtil.packetToBytes(enterPacket);
    }

    private Map<String, FighterEntity> getMonsterFighterEntity(int stageId) {
        Map<String, FighterEntity> retMap = new HashMap<>();
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        for (int monsterSpawnId : stageVo.getMonsterSpawnIdList()) {
            retMap.putAll(spawnMonster(stageId, monsterSpawnId));
        }
        return retMap;
    }

    private Map<String, FighterEntity> spawnMonster(int stageId, int monsterSpawnId) {
        Map<String, FighterEntity> resultMap = new HashMap<>();
        MonsterSpawnVo monsterSpawnVo = SceneManager.getMonsterSpawnVo(monsterSpawnId);
        if (monsterSpawnVo == null) {
            LogUtil.error("daily5v5|找不到刷怪组配置monsterspawnid={},请检查表", monsterSpawnId, new IllegalArgumentException());
            return resultMap;
        }
        int index = 0;
        for (MonsterAttributeVo monsterAttrVo : monsterSpawnVo.getMonsterAttrList()) {
            String monsterUniqueId = getMonsterUId(stageId, monsterSpawnId, monsterAttrVo.getStageMonsterId());
            FighterEntity monsterEntity = FighterCreator.create(FighterEntity.TYPE_MONSTER, monsterUniqueId,
                    getSpawnUId(monsterSpawnId), monsterSpawnId, monsterAttrVo, monsterSpawnVo.getAwake(),
                    monsterSpawnVo.getSpawnDelayByIndex(index++), null);
            resultMap.put(monsterUniqueId, monsterEntity);
        }
        return resultMap;
    }
    
    /**
     * 
     * @param uId
     * @param effectType
     * @param addBuff  true 要触发发送添加buff   false 不用发（用来获取满足条件的Daily5v5MatchingVo）
     * @param isAddAgain 复活或重进战场   重新给角色家回buff
     * @return
     */
    public Daily5v5MatchingVo checkBuffAndAdd(String uId, int effectType, boolean addBuff, boolean isAddAgain){
    	List<Daily5v5MatchingVo> memberList = null;
    	byte camp = 0;
    	ArrayList<String> uniqueIdList= new ArrayList<>();
    	if(towerMap.containsKey(uId)){
    		EliteFightTower tower = towerMap.get(uId);
    		camp = tower.getCamp();
    		if(camp==Daily5v5Manager.CAMP1){
    			memberList = team1.getMemberList();
    		}else{
    			memberList = team2.getMemberList();
    		}
    		int size = memberList.size();
    		Daily5v5MatchingVo matchingVo = null;
    		Daily5v5MatchingVo selectMatchingVo = null;
    		for(int i=0;i<size;i++){
    			matchingVo = memberList.get(i);
    			uniqueIdList.add(String.valueOf(matchingVo.getRoleId()));
    			if(matchingVo.getPassivityBuff().containsKey(effectType)){
    				if(selectMatchingVo==null){
    					selectMatchingVo = matchingVo;
    				}else{
    					Daily5v5BuffInfo daily5v5BuffInfo = selectMatchingVo.getPassivityBuff().get(effectType);
    					Daily5v5BuffInfo oDaily5v5BuffInfo = matchingVo.getPassivityBuff().get(effectType);
    					if(daily5v5BuffInfo.getBuffLevel()<oDaily5v5BuffInfo.getBuffLevel()){    						
    						selectMatchingVo = matchingVo;
    					}
    				}
    			}
    		}
    		if(selectMatchingVo==null) return null;
    		if(addBuff){    			
    			Map<Integer, Daily5v5BuffInfo> passivityBuff = selectMatchingVo.getPassivityBuff();
    			Daily5v5BuffInfo daily5v5BuffInfo = passivityBuff.get(effectType);
    			ServerOrder order = ServerOrders.newAddBuffOrder(ServerOrder.NONE, ServerOrder.NONE, daily5v5BuffInfo.getBuffId(), 1);
    			order.setUniqueIDs(uniqueIdList);
    			fight.sendServerOrder(battleData.getFightId(), order, FightConst.T_DAILY_5V5);
    			for(int i=0;i<size;i++){
        			matchingVo = memberList.get(i);    				
    				Map<Integer, Set<Integer>> passivityBuffCd = matchingVo.getPassivityBuffCd();
    				if(!isAddAgain){
    					Set<Integer> timeSet = passivityBuffCd.get(effectType);
    					if(timeSet==null){    				
    						timeSet = new HashSet<>();//为以后有时间扩展
    						passivityBuffCd.put(effectType, timeSet);
    					}
    					timeSet.add((int)(System.currentTimeMillis()/1000));
    				}
    			}
    		}
    		return selectMatchingVo;
    	}else{    		
    		if(fight.getCampId(uId)==team1.getTeamId()){
    			memberList = team1.getMemberList();
    			camp = Daily5v5Manager.CAMP1;
    		}else{
    			camp = Daily5v5Manager.CAMP2;
    			memberList = team2.getMemberList();
    		}
    		int size = memberList.size();
    		Daily5v5MatchingVo matchingVo = null;
    		for(int i=0;i<size;i++){
    			matchingVo = memberList.get(i);
    			if(matchingVo.getRoleId()==Long.parseLong(uId)){
    				break;
    			}
    		}
    		if(matchingVo==null) return null;
    		Map<Integer, Daily5v5BuffInfo> passivityBuff = matchingVo.getPassivityBuff();
    		if(!passivityBuff.containsKey(effectType)&&effectType!=Daily5v5Manager.TOWER_SOUL){
    			return null;
    		}
    		if(addBuff){    			
    			Daily5v5BuffInfo daily5v5BuffInfo = passivityBuff.get(effectType);
    			if(effectType==Daily5v5Manager.TOWER_SOUL){
    	    		Daily5v5MatchingVo selectMatchingVo = null;
    	    		for(int i=0;i<size;i++){
    	    			matchingVo = memberList.get(i);
    	    			if(matchingVo.getPassivityBuff().containsKey(effectType)){
    	    				if(selectMatchingVo==null){
    	    					selectMatchingVo = matchingVo;
    	    				}else{
    	    					Daily5v5BuffInfo tempDaily5v5BuffInfo = selectMatchingVo.getPassivityBuff().get(effectType);
    	    					Daily5v5BuffInfo oDaily5v5BuffInfo = matchingVo.getPassivityBuff().get(effectType);
    	    					if(tempDaily5v5BuffInfo.getBuffLevel()<oDaily5v5BuffInfo.getBuffLevel()){    						
    	    						selectMatchingVo = matchingVo;
    	    					}
    	    				}
    	    			}
    	    		}
    	    		if(selectMatchingVo==null) return null;
    	    		passivityBuff = selectMatchingVo.getPassivityBuff();
        			daily5v5BuffInfo = passivityBuff.get(effectType);
    			}
    			ServerOrder order = ServerOrders.newAddBuffOrder(ServerOrder.NONE, ServerOrder.NONE, daily5v5BuffInfo.getBuffId(), 1);
    			uniqueIdList.add(uId);
    			order.setUniqueIDs(uniqueIdList);
    			fight.sendServerOrder(battleData.getFightId(), order, FightConst.T_DAILY_5V5);
    			Map<Integer, Set<Integer>> passivityBuffCd = matchingVo.getPassivityBuffCd();
    			if(!isAddAgain){
    				Set<Integer> timeSet = passivityBuffCd.get(effectType);
    				if(timeSet==null){    				
    					timeSet = new HashSet<>();//为以后有时间扩展
    					passivityBuffCd.put(effectType, timeSet);
    				}
    				timeSet.add((int)(System.currentTimeMillis()/1000));
    			}
    		}
    		return matchingVo;
    	}
    }

    /**
     * 添加无敌buff
     */
    public void addInvincibleBuff() {
        ArrayList<String> addBuffTowerList = new ArrayList<>();
        for (EliteFightTower tower : towerMap.values()) {
            if (tower.getType() == Daily5v5Manager.K_TOWER_TYPE_BASEBOT
                    || tower.getType() == Daily5v5Manager.K_TOWER_TYPE_BASETOP
                    || tower.getType() == Daily5v5Manager.K_TOWER_TYPE_CRYSTAL) {
                addBuffTowerList.add(tower.getUid());
            }
        }
        ServerOrder order = ServerOrders.newAddBuffOrder(ServerOrder.NONE, ServerOrder.NONE, Daily5v5Manager.invincibleBuffId, 1);
        fight.setInvincibleBuffInstId(order.getInstanceId());
        order.setUniqueIDs(addBuffTowerList);
        Daily5v5RpcHelper.fightBaseService().addServerOrder(fight.getFightServerId(), FightConst.T_DAILY_5V5,
                MultiServerHelper.getServerId(), fight.getFightId(), order);
    }

    /**
     * 移除无敌buff
     */
    public void removeInvincibleBuff(byte camp, byte category) {
        ArrayList<String> removeBuffTowerList = new ArrayList<>();
        for (EliteFightTower tower : towerMap.values()) {
            if (tower.getCamp() != camp) continue;
            if (category == Daily5v5Manager.K_TOWER_CATEGORY_OUTER) {
                if (tower.getType() == Daily5v5Manager.K_TOWER_TYPE_BASETOP || tower.getType() == Daily5v5Manager.K_TOWER_TYPE_BASEBOT) {
                    removeBuffTowerList.add(tower.getUid());
                }
            } else if (category == Daily5v5Manager.K_TOWER_CATEGORY_INNER) {
                if (tower.getType() == Daily5v5Manager.K_TOWER_TYPE_CRYSTAL) {
                    removeBuffTowerList.add(tower.getUid());
                }
            }
        }
        ServerOrder order = ServerOrders.newRemoveBuffOrder(ServerOrder.NONE, fight.getInvincibleBuffInstId());
        order.setUniqueIDs(removeBuffTowerList);
        Daily5v5RpcHelper.fightBaseService().addServerOrder(fight.getFightServerId(), FightConst.T_DAILY_5V5,
                MultiServerHelper.getServerId(), fight.getFightId(), order);
    }
    
    public void finishFight(long winnerTeamId, long loserTeamId, byte endType){
    	if(isFinishEnd) return;
    	isFinishEnd = true;
    	//奖励发放
    	List<FightingEndVo> winnerList = new ArrayList<>();
    	List<FightingEndVo> loserList = new ArrayList<>();
    	FightStat stat = fight.getStat();
    	Map<Long, FightPersonalStat> personalStatMap = stat.getPersonalStatMap();
    	MatchingTeamVo winTeam = null;
    	MatchingTeamVo loseTeam = null;
    	long winPoints = 0;
    	long losePoints = 0;
    	if(battleData.getCamp1Id()==winnerTeamId){
    		winTeam = team1;
    		loseTeam = team2;
    		winPoints = stat.getCamp1TotalPoints();
    		losePoints = stat.getCamp2TotalPoints();
    	}else{
    		winTeam = team2;
    		loseTeam = team1;
    		winPoints = stat.getCamp2TotalPoints();
    		losePoints = stat.getCamp1TotalPoints();
    	}
    	Map<Long, Byte> resultMap = new HashMap<>();
    	Daily5v5MatchingVo matVo = null;
    	FightPersonalStat fightPersonalStat = null;
    	List<Daily5v5MatchingVo> winMemberList = winTeam.getMemberList();
    	int winSize = winMemberList.size();
    	long roleId = 0;
    	FightingEndVo topFeVo = null;
    	for(int i=0;i<winSize;i++){
    		matVo = winMemberList.get(i);
    		roleId = matVo.getRoleId();
    		try {
        		fightPersonalStat = personalStatMap.get(roleId);
        		FightingEndVo feVo = new FightingEndVo(roleId, matVo.getRoleName(), matVo.getLevel(), matVo.getJob(), 
        				fightPersonalStat.getPoints(), matVo.getFightValue(), fightPersonalStat.getKillCount(), 
        				fightPersonalStat.getDeadCount(), fightPersonalStat.getAssistCount(), fightPersonalStat.getMaxComboKillCount());
        		winnerList.add(feVo);
        		if(topFeVo==null){
        			topFeVo = feVo;
        		}else if(feVo.getIntegral()>topFeVo.getIntegral()){
        			topFeVo = feVo;
        		}
        		resultMap.put(roleId, Daily5v5Manager.WIN_RESULT);
        		ServiceHelper.daily5v5MatchService().removeFromFightingMap(roleId);
			} catch (Exception e) {
				Integer serverId = battleData.getFighterSeverIdMap().get(roleId);
				if(serverId==null){
					serverId = -1;
				}
				LogUtil.error("handle winner fail, roleId"+roleId+" , serverId:"+serverId, e);
			}
    	}
    	if(endType==2&&topFeVo!=null){
    		topFeVo.setIntegral(topFeVo.getIntegral()+300);
    	}
    	List<Daily5v5MatchingVo> loseMemberList = loseTeam.getMemberList();
    	int loseSize = loseMemberList.size();
    	for(int i=0;i<loseSize;i++){
    		matVo = loseMemberList.get(i);
    		roleId = matVo.getRoleId();
    		try {				
    			fightPersonalStat = personalStatMap.get(roleId);
    			FightingEndVo feVo = new FightingEndVo(roleId, matVo.getRoleName(), matVo.getLevel(), matVo.getJob(), 
        				fightPersonalStat.getPoints(), matVo.getFightValue(), fightPersonalStat.getKillCount(), 
        				fightPersonalStat.getDeadCount(), fightPersonalStat.getAssistCount(), fightPersonalStat.getMaxComboKillCount());
    			loserList.add(feVo);
    			resultMap.put(roleId, Daily5v5Manager.LOSE_RESULT);
    			ServiceHelper.daily5v5MatchService().removeFromFightingMap(roleId);
			} catch (Exception e) {
				Integer serverId = battleData.getFighterSeverIdMap().get(roleId);
				if(serverId==null){
					serverId = -1;
				}
				LogUtil.error("handle loser fail, roleId"+roleId+" , serverId:"+serverId, e);
			}
    	}
    	ClientDaily5v5 fightEndPacket = new ClientDaily5v5(Daily5v5Manager.FIGHTING_END);
    	Collections.sort(winnerList);
    	Collections.sort(loserList);
    	fightEndPacket.setEndType(endType);
    	fightEndPacket.setWinnerList(winnerList);
    	fightEndPacket.setLoserList(loserList);
    	fightEndPacket.setWinPoints(winPoints);
    	fightEndPacket.setLosePoints(losePoints);
    	Iterator<Entry<String, Integer>> iterator = battleData.getFighterSeverIdMap().entrySet().iterator();
    	Entry<String, Integer> entry = null;
    	String keyRoleId;
    	byte result = 0;
    	int mainServerId = 0;
    	long currentTime = System.currentTimeMillis();
		int passTime =(int)((currentTime-creatTimestamp)/1000);
		if(passTime>Daily5v5Manager.FIGHT_TIME_LIMIT){
			passTime = Daily5v5Manager.FIGHT_TIME_LIMIT;
		}
		if(endType==2){
			passTime = Daily5v5Manager.FIGHT_TIME_LIMIT;
		}
    	for(;iterator.hasNext();){
    		entry = iterator.next();
    		keyRoleId = entry.getKey();
    		try {
        		result = resultMap.get(parseLong(keyRoleId));
        		mainServerId = getMainServerId(keyRoleId);
        		Daily5v5RpcHelper.daily5v5Service().finishFight(mainServerId, parseLong(keyRoleId), result, passTime, fightEndPacket);
        		LogUtil.info("daily5v5, finishFight roleId:"+keyRoleId+" , fightId:"+battleData.getFightId());
    		} catch (Exception e) {
				LogUtil.error("Send result back, roleId"+keyRoleId+" , result:"+result, e);
			}
    	}
    	ServiceHelper.daily5v5MatchService().endRemoveFight(battleData.getFightId());
    	stopFight();
    }

    private String getMonsterUId(int stageId, int spawnId, int monsterId) {
        return "m" + stageId + getSpawnUId(spawnId) + monsterId;
    }

    private String getSpawnUId(int spawnId) {
        return Integer.toString(spawnId);
    }

    private void printTowerAttr(FighterEntity entity, String text) {
        Attribute attribute = entity.getAttribute();
        LogUtil.info("daily5v5|塔属性:{}|uid:{},hp:{},attack:{},anticrit:{},avoid:{},crit:{},defense:{},hit:{}"
                , text, entity.getUniqueId(), attribute.getHp(), attribute.getAttack(), attribute.getAnticrit(), attribute.getAvoid(), attribute.getCrit(), attribute.getDefense(), attribute.getHit());
    }

    public BattleData getBattleData() {
		return battleData;
	}

	public void setBattleData(BattleData battleData) {
		this.battleData = battleData;
	}

	public String getcamp1Name() {
        return camp1Name;
    }

    public void setcamp1Name(String camp1Name) {
        this.camp1Name = camp1Name;
    }

    public String getcamp2Name() {
        return camp2Name;
    }

    public void setcamp2Name(String camp2Name) {
        this.camp2Name = camp2Name;
    }

    public Map<String, Integer> getPayReviveCountMap() {
        return payReviveCountMap;
    }

    public void setPayReviveCountMap(Map<String, Integer> payReviveCountMap) {
        this.payReviveCountMap = payReviveCountMap;
    }

    public Map<String, EliteFightTower> getTowerMap() {
        return towerMap;
    }

    public void setTowerMap(Map<String, EliteFightTower> towerMap) {
        this.towerMap = towerMap;
    }

    public Daily5v5BattleStat getBattleStat() {
        return battleStat;
    }

    public void setBattleStat(Daily5v5BattleStat battleStat) {
        this.battleStat = battleStat;
    }

    public long getcamp1Id() {
        return camp1Id;
    }

    public void setcamp1Id(long camp1Id) {
        this.camp1Id = camp1Id;
    }

    public long getcamp2Id() {
        return camp2Id;
    }

    public void setcamp2Id(long camp2Id) {
        this.camp2Id = camp2Id;
    }

	public BattleInfoHandler getBattleInfoHandler() {
		return battleInfoHandler;
	}

	public void setBattleInfoHandler(BattleInfoHandler battleInfoHandler) {
		this.battleInfoHandler = battleInfoHandler;
	}

	public MatchingTeamVo getTeam1() {
		return team1;
	}

	public void setTeam1(MatchingTeamVo team1) {
		this.team1 = team1;
	}

	public MatchingTeamVo getTeam2() {
		return team2;
	}

	public void setTeam2(MatchingTeamVo team2) {
		this.team2 = team2;
	}

	public int getFightServerId() {
		return fightServerId;
	}

	public void setFightServerId(int fightServerId) {
		this.fightServerId = fightServerId;
	}

	public long getCreatTimestamp() {
		return creatTimestamp;
	}

	public void setCreatTimestamp(long creatTimestamp) {
		this.creatTimestamp = creatTimestamp;
	}

}
