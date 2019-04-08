package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.event.CampCityFightEvent;
import com.stars.modules.camp.packet.ClientCampCiytFight;
import com.stars.modules.camp.pojo.CampCityFightData;
import com.stars.modules.camp.prodata.CommonOfficerVo;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.Damage;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterEliteDungeon;
import com.stars.modules.scene.packet.fightSync.ClientSyncAttr;
import com.stars.modules.scene.packet.fightSync.ClientSyncOrder;
import com.stars.modules.scene.packet.fightSync.ServerFightDamage;
import com.stars.modules.scene.packet.fightSync.ServerSyncOrder;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.camp.pojo.CampPlayerImageData;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMember;

import java.util.*;
import java.util.Map.Entry;

public class CampCityFightScene extends FightScene{
	
	private List<Long> memberRoleIds = new ArrayList<>();
	
	private Map<String, CampCityFightData> integralMap = new HashMap<String, CampCityFightData>();
	
	private Map<String, Map<String, Integer>> damageMap = new HashMap<>();//<key:受击者, <key:攻击者, value:伤害值>>
	
	private Map<String, CampPlayerImageData> enemyMap = new HashMap<>();
	
	private HashSet<String> enemyDeatSet = new HashSet<>();//敌方死亡
	
	private HashSet<String> selfDeatSet = new HashSet<>();//我方死亡
	
	private HashSet<Long> robotSet = new HashSet<>();//我方机器人
	
	private int teamId;
	
	private int chaCityId;

	@Override
	public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
		if(obj == null || "".equals(obj)) return false;
		Object[] objs = (Object[])obj;
		int tmpStageId = (Integer) objs[2];
        StageinfoVo stageVo = SceneManager.getStageVo(tmpStageId);
        if (stageVo == null) {
            return false;
        }
        return true;
	}

	@Override
	public void enter(Map<String, Module> moduleMap, Object obj) {
		Object[] objs = (Object[])obj;
		this.teamId = (Integer)objs[0];
		this.chaCityId = (Integer)objs[1];
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
        CampCityFightEvent event = new CampCityFightEvent();
        event.setOpType(CampCityFightEvent.CHANGE_SCENE);
        event.setScene(this);
        RoleModule roleModule = (RoleModule)moduleMap.get(MConst.Role);
        long myRoleId = roleModule.id();
        for(long memberId : memberRoleIds){   
        	if(memberId==myRoleId) continue;
        	ServiceHelper.roleService().notice(memberId, event);
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

	@Override
	public void exit(Map<String, Module> moduleMap) {
		long myroleId = moduleMap.get(MConst.Role).id();
		exit(myroleId, true);
		if((stageStatus==SceneManager.STAGE_FAIL||stageStatus==SceneManager.STAGE_VICTORY)&&memberRoleIds.size()>0){
			BaseTeam team = ServiceHelper.baseTeamService().getTeam(teamId);
			if(team==null){
				return;
			}
			if(myroleId!=team.getCaptainId()) return;
			memberRoleIds = new ArrayList<>(team.getPlayerMembers().keySet());
			List<Long> members = new ArrayList<>(memberRoleIds);
			for(long id : members){
				exit(id, true);
				CampCityFightEvent event = new CampCityFightEvent();
				event.setOpType(CampCityFightEvent.BACK_TO_CITY);
				ServiceHelper.roleService().notice(id, event);
			}
		}
	}
	
	public void exit(long roleId, boolean isCaptain) {
		if(!memberRoleIds.contains(roleId)) return;
        memberRoleIds.remove(roleId);
        if(memberRoleIds.size()>0){
        	if(!selfDeatSet.contains(String.valueOf(roleId))){
        		robotSet.add(roleId);
        	}
        }
        selfDeatSet.remove(String.valueOf(roleId));
//        ClientUpdatePlayer packet = new ClientUpdatePlayer();
//        List<String> removeFighter = new ArrayList<>();
//        removeFighter.add(Long.toString(roleId));
//        packet.setRemoveFighter(removeFighter);
//        sendPacketToTeamMembers(packet, -1);
        //离开场景  直接弹战斗失败
        if(stageStatus!=SceneManager.STAGE_FAIL&&stageStatus!=SceneManager.STAGE_VICTORY){        	
        	PlayerUtil.send(roleId, new ClientText("战斗失败"));
        	if(isCaptain){
        		ServiceHelper.baseTeamService().leaveTeam(roleId);
        	}
        }
        //退出队伍
        if(!isCaptain){        		
        	ServiceHelper.baseTeamService().leaveTeam(roleId);
        }
    }
	
	public boolean hasNoPlayer() {
        return memberRoleIds.isEmpty();
    }

	@Override
	public boolean isEnd() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void finishDeal(Map<String, Module> moduleMap, byte finish) {
		List<CampCityFightData> integralList = new ArrayList<>();
		int myTotalIntegral = 0;
		int enemyTotalIntegral = 0;
		for(CampCityFightData data : integralMap.values()){
			if(memberRoleIds.contains(data.getRoleId())){
				integralList.add(data);
				myTotalIntegral += data.getIntegral();
			}else{
				enemyTotalIntegral += data.getIntegral();
			}
		}
		byte result = 0;
//		if(myTotalIntegral>=enemyTotalIntegral){
//			result = 1;
//		}
		if(finish==SceneManager.STAGE_VICTORY){
			result = 1;
		}
		List<CampCityFightData> tempIntegralList = new ArrayList<>(integralList);
		int size = tempIntegralList.size();
		boolean teamAddition = false;
		if(memberRoleIds.size()>=2){
			teamAddition = true;
		}
		CampCityFightData data = null;
		for(int i=0;i<size;i++){
			data = tempIntegralList.get(i);
			CampCityFightEvent event = new CampCityFightEvent();
			event.setOpType(CampCityFightEvent.FIGHT_END);
			event.setResult(result);
			event.setIntegral(data.getIntegral());
			event.setIntegralList(integralList);
			event.setChaCityId(chaCityId);
			event.setTeamAddition(teamAddition);
			ServiceHelper.roleService().notice(data.getRoleId(), event);//通知队员结算
		}
		BaseTeam team = ServiceHelper.baseTeamService().getTeam(teamId);
		if(team!=null){
			team.setFight(false);
		}
		ServiceHelper.campCityFightService().removeFightScene(teamId);
	}
	
	@Override
    public void receivePacket(Map<String, Module> moduleMap, PlayerPacket packet) {
        if (stageStatus != SceneManager.STAGE_PROCEEDING)
            return;
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
                collectPlayerDamage(receiver.getUniqueId(), giver.getUniqueId(), damage.getValue());
            } else if (giver.getFighterType() == FighterEntity.TYPE_BUDDY && damage.getValue() < 0) {
//                String masterRoleId = entityMap.get(giver.getUniqueId()).getMasterUId();
//                collectPlayerDamage(masterRoleId, damage.getValue());
            }else if (giver.getFighterType() == FighterEntity.TYPE_ROBOT && damage.getValue() < 0) {
            	String uniqueId = giver.getUniqueId();
//            	if(uniqueId.indexOf('r')!=-1){
//            		uniqueId = uniqueId.substring(uniqueId.indexOf('r')+1);
//            	}
                collectPlayerDamage(receiver.getUniqueId(), uniqueId, damage.getValue());
            }
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
                    //积分处理
                    handleIntegral(receiverUniqueId);
                } else {// 我方
                    deadSelfIds.add(damage.getReceiverId());
                }
            }
        }
        if (!curHpMap.isEmpty()) {
            // 同步属性(血量)到客户端
            ClientSyncAttr clientSyncAttr = new ClientSyncAttr(curHpMap);
            clientSyncAttr.setDamageMap(null);
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
    private void collectPlayerDamage(String recieverUid,String giverUid, int damage) {
        Map<String, Integer> map = damageMap.get(recieverUid);
        if(map==null){
        	map = new HashMap<>();
        	damageMap.put(recieverUid, map);
        }
        Integer curDamage = map.get(giverUid);
        if(curDamage==null){
        	curDamage = 0;
        }
        map.put(giverUid, curDamage+(damage*-1));
    }
    
    private void handleIntegral(String deadUid){
    	Map<String, Integer> map = damageMap.get(deadUid);
    	CampPlayerImageData playerImageData = enemyMap.get(deadUid);
    	if(playerImageData==null){
    		return;
    	}
    	int commonOfficerId = playerImageData.getCommonOfficerId();
    	int level = 0;
    	if(commonOfficerId>0){    		
    		CommonOfficerVo commonOfficer = CampManager.commonOfficerMap.get(commonOfficerId);
    		level = commonOfficer.getLevel();
    	}
    	int quality = 0;
    	int designateOfficerId = playerImageData.getDesignateOfficerId();
    	int rareOfficerId = playerImageData.getRareOfficerId();
    	if(designateOfficerId>0){
    		quality = CampManager.designateOfficerMap.get(designateOfficerId).getQuality();
    	}
    	if(rareOfficerId>0){
    		quality = CampManager.rareOfficerMap.get(rareOfficerId).getQuality();
    	}
    	long totalIntegral = 0;
    	for(int[] info : CampManager.commonOfficerMark){
    		if(level>=info[0]&&level<=info[1]){
    			totalIntegral = info[2];
    			break;
    		}
    	}
    	for(int[] info : CampManager.rareOfficerMark){
    		if(quality>=info[0]&&quality<=info[1]){
    			totalIntegral += info[2];
    			break;
    		}
    	}
    	
    	FighterEntity deadEntity = entityMap.get(deadUid);
    	int maxHp = deadEntity.getAttribute().getMaxhp();
    	Iterator<Entry<String, Integer>> iterator = map.entrySet().iterator();
    	Entry<String, Integer> entry = null;
    	int memberIntegral = 0;
    	List<CampCityFightData> integralList = new ArrayList<>();
    	for(;iterator.hasNext();){
    		entry = iterator.next();
    		memberIntegral = (int)(totalIntegral * (entry.getValue() *100 / maxHp) / 100);
    		CampCityFightData campCityFightData = integralMap.get(entry.getKey());
    		if(campCityFightData==null){
    			continue;
    		}
    		campCityFightData.setIntegral(campCityFightData.getIntegral()+memberIntegral);
    		integralList.add(campCityFightData);
    	}
    	//刷新积分
    	ClientCampCiytFight packet = new ClientCampCiytFight();
    	packet.setOpType(ClientCampCiytFight.UPDATE_INTEGRAL);
    	packet.setIntegralList(integralList);
    	sendPacketToTeamMembers(packet, -1);
    }
    
    @Override
    public void selfDead(Map<String, Module> moduleMap) {//屏蔽通用自身死亡方法，避免一个人死了就触发结算
    	
    }
    
    public void selfDead(List<String> uIdList) {
    	selfDeatSet.addAll(uIdList);
    	int size = selfDeatSet.size();
    	int pNum = memberRoleIds.size()+robotSet.size();
		if(size>=pNum){	
			stageStatus = SceneManager.STAGE_FAIL;
			finishDeal(null, SceneManager.STAGE_FAIL);//失败
		}
    }

	@Override
	public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {
		enemyDeatSet.addAll(uIdList);
		int size = enemyDeatSet.size();
		if(size==enemyMap.size()){	
			stageStatus = SceneManager.STAGE_VICTORY;
			finishDeal(moduleMap, SceneManager.STAGE_VICTORY);//胜利
		}
	}

	@Override
	public void areaSpawnMonster(Map<String, Module> moduleMap, int spawnId) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void killAll() {
		
	}
	
	/**
     * 加入队伍成员的属性
     *
     * @param collection
     */
    public void addTeamMemberFighter(Collection<BaseTeamMember> collection) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        int serverId = MultiServerHelper.getDisplayServerId();
        int i=0;
        for (BaseTeamMember teamMember : collection) {
            for (FighterEntity entity : teamMember.getEntityMap().values()) {
                FighterEntity newEntity = entity.copy();
                // 玩家注入出生位置/朝向
                if (newEntity != null && newEntity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                    newEntity.setPosition(CampManager.myPosition[i]);
                    newEntity.setRotation(stageVo.getRotation());
                    if (teamMember.isPlayer()) {
                        memberRoleIds.add(Long.parseLong(newEntity.getUniqueId()));
                    }else{
                    	newEntity.setFighterType(FighterEntity.TYPE_ROBOT);
                    	newEntity.setIsRobot(true);
                    }
                    CampCityFightData campCityFightData = new CampCityFightData();
                    campCityFightData.setEntity(newEntity);
                    campCityFightData.setJob(teamMember.getJob());
                    campCityFightData.setRoleId(newEntity.getRoleId());
                    campCityFightData.setServerId(serverId);
                    integralMap.put(newEntity.getUniqueId(), campCityFightData);
                    entityMap.put(entity.getUniqueId(), newEntity);
                }
            }
            i++;
        }
    }

	public List<Long> getMemberRoleIds() {
		return memberRoleIds;
	}

	public void setMemberRoleIds(List<Long> memberRoleIds) {
		this.memberRoleIds = memberRoleIds;
	}

	public Map<String, CampPlayerImageData> getEnemyMap() {
		return enemyMap;
	}

	public void setEnemyMap(Map<String, CampPlayerImageData> enemyMap) {
		this.enemyMap = enemyMap;
		StageinfoVo stageVo = SceneManager.getStageVo(stageId);
		int i=0;
		for(CampPlayerImageData data : enemyMap.values()){
			FighterEntity newEntity = data.getEntity().copy();
			newEntity.setFighterType(FighterEntity.TYPE_ROBOT);
			 if (newEntity != null && newEntity.getFighterType() == FighterEntity.TYPE_ROBOT) {
                 newEntity.setPosition(CampManager.enemyPosition[i]);
                 newEntity.setRotation(stageVo.getEnemyRot(0));
//             	 newEntity.setFighterType(FighterEntity.TYPE_ROBOT);
             	 newEntity.setIsRobot(true);
             }
//			 CampCityFightData campCityFightData = new CampCityFightData();
//			 campCityFightData.setEntity(newEntity);
//			 campCityFightData.setJob(data.getJob());
//			 campCityFightData.setRoleId(newEntity.getRoleId());
//			 campCityFightData.setServerId(data.getServerId());
//			 integralMap.put(newEntity.getUniqueId(), campCityFightData);
             entityMap.put(newEntity.getUniqueId(), newEntity);
             i++;
		}
	}

}
