package com.stars.services.familyEscort;

import com.stars.core.player.PlayerUtil;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.familyEscort.FamilyEscortConst;
import com.stars.modules.familyEscort.event.FamilyEscortDropEvent;
import com.stars.modules.familyEscort.event.FamilyEscortFightEvent;
import com.stars.modules.familyEscort.event.FamilyEscortKillEvent;
import com.stars.modules.familyEscort.packet.*;
import com.stars.modules.familyEscort.prodata.FamilyEscortConfig;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.login.packet.ClientWarning;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;
import com.stars.services.familyEscort.route.EscortCar;
import com.stars.util.LogUtil;
import com.stars.util.RandomUtil;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author dengzhou
 *一个家族的运镖场景
 */
public class FamilyEscortMap implements Comparable<FamilyEscortMap> {

    private Map<Long, FamilyEscortRoleData> actEscortMap = new HashMap<>();
    private Map<Long, FamilyEscortRoleData> lootEscortMap = new HashMap<>();

    /**
	 * 镖车信息
	 */
	private HashMap<Long, EscortCar>esCarPosMap = new HashMap<>();
	
	private EscortFamily familyInfo;
	
	private FamilyEscortServiceActor famEsSerActor;
	
	private HashMap<Long, HashSet<Long>>careEscCarMap;
	
	/**
	 * 玩家能看到的周围玩家列表
	 */
	private HashMap<Long, List<Long>> arroundPlayerList = new HashMap<>();
	
	/**
	 * 更新周围玩家列表
	 * @param roleId
	 * @param list
	 */
	public void updateArroudPlayerList(long roleId,List<Long> list){
		if(actEscortMap.containsKey(roleId) || lootEscortMap.containsKey(roleId)){
			arroundPlayerList.put(roleId, list);
		}
	}
	
	public void checkAndRepairState() {
		// 镖车同步状态的自动修复
		try {
			List<Long> removeRoleList = new LinkedList<Long>();
			for (Entry<Long, HashSet<Long>> entry : careEscCarMap.entrySet()) {
				if (getFightData(entry.getKey()) == null) {
					removeRoleList.add(entry.getKey());
					continue;
				}
				List<Long> removeList = new LinkedList<Long>();
				for (Long carId : entry.getValue()) {
					EscortCar car = esCarPosMap.get(carId);
					if (car == null || car.isFinished()) {
						removeList.add(carId);
					}
				}
				// 清除无用镖车
				for (Long carId : removeList) {
					entry.getValue().remove(carId);
				}
			}
			// 不在场景里面的玩家也移除通知消息
			for (Long roleId : removeRoleList) {
				careEscCarMap.remove(roleId);
			}
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	public static int CLEAR_CAR_OFFLINETIME = 30*1000;
	
	public void clearOffLineCar() {
		try {
			long now = System.currentTimeMillis();
			List<Long> removeList = new LinkedList<>();
			for (EscortCar car : esCarPosMap.values()) {
				if (car.getOffLineTime() > 0 && now - car.getOffLineTime() > CLEAR_CAR_OFFLINETIME) {
					removeList.add(car.getRoleId());
				}
			}
			for (Long roleId : removeList) {
				destroyACar(roleId);
			}
		} catch (Throwable e) {
			LogUtil.error(e.getMessage());
		}
	}
	
	public EscortFamily getFamilyInfo() {
		return familyInfo;
	}

	public void setFamilyInfo(EscortFamily familyInfo) {
		this.familyInfo = familyInfo;
	}

	public EscortCar getEscortCar(long roleId){
		return esCarPosMap.get(roleId); 
	}
	
	/**
	 * 角色离开场景
	 * @param roleId
	 */
	public void exitEscortMap(long roleId) {
		// 属于运镖的
		destroyACar(roleId);
		actEscortMap.remove(roleId);
			// 属于劫镖的
		lootEscortMap.remove(roleId);
		// 关注的车也处理
		careEscCarMap.remove(roleId);
		if (esCarPosMap != null && esCarPosMap.size() > 0) {
			for (EscortCar eCar : esCarPosMap.values()) {
				eCar.cancelCare(roleId);
			}
		}
		LogUtil.info("exitEscortMap "+roleId+"|"+familyInfo.getFamilyId());
	}
	
	public void enterEscortMap(int serverId,long roleId, long roleFamilyId, FighterEntity role) {
		if(roleFamilyId == familyInfo.getFamilyId()){
			if (!actEscortMap.containsKey(roleId)) {
				FamilyEscortRoleData roleData = new FamilyEscortRoleData();
				roleData.setRoleId(roleId);
				roleData.setServerId(serverId);
				roleData.initFightData(role);
				actEscortMap.put(roleId, roleData);
			}	
		}else{
			if (!lootEscortMap.containsKey(roleId)) {
	            FamilyEscortRoleData roleData = new FamilyEscortRoleData();
	            roleData.setRoleId(roleId);
	            roleData.setServerId(serverId);
	        	roleData.initFightData(role);
				lootEscortMap.put(roleId, roleData);
			}
		}
		FamilyEscortRoleData roleData =  getFightData(roleId);
		roleData.initStateByEnterScene();
		LogUtil.info("enterEscortMap "+roleId+"|"+familyInfo.getFamilyId());
	}
	/**
	 * 初始化战斗状态
	 * @param roleId
	 * @param roleFamilyId
	 * @param role
	 */
	public void initFightData(long roleId, FighterEntity role){
		FamilyEscortRoleData roleData =getFightData(roleId);
		if(roleData == null)return;
		roleData.initFightData(role);
	}
	
	public int getEsCarSize(){
		int fSize = this.esCarPosMap == null?0:this.esCarPosMap.size();
		return fSize;
	}
	
	public void writeToBuffer(NewByteBuffer buff){
		buff.writeString(familyInfo.getFamilyId()+"");
		buff.writeString(familyInfo.getFamilyName());
		buff.writeInt(familyInfo.getFightRank());
		int  escortSize =  getEsCarSize();
		buff.writeInt(escortSize);
	}
	
	
	public FamilyEscortMap(FamilyEscortServiceActor famEsSerActor,EscortFamily efinfo){
		this.famEsSerActor = famEsSerActor;
		careEscCarMap = new HashMap<Long, HashSet<Long>>();
		this.familyInfo = efinfo;
		actEscortMap = new HashMap<Long, FamilyEscortRoleData>();
		lootEscortMap = new HashMap<Long, FamilyEscortRoleData>();
		esCarPosMap = new HashMap<Long, EscortCar>();
	}
	
	public void actEscort(FighterEntity role,int nowEscortTime) {
		long roleId = Long.parseLong(role.getUniqueId());
		EscortCar eCar = createANewCar(role);
		eCar.setEscoctTime(nowEscortTime);
		eCar.addCare(roleId);// 自己关注自己，便于更新
		initCareEscCar(roleId);
		
		//FIXME 推广新车给其他还可以刷的玩家
		
	}
	
	/**
	 * 销毁镖车
	 * @param roleId
	 */
	public void destroyACar(long roleId){
		EscortCar eCar = esCarPosMap.remove(roleId);
		if (eCar == null)
			return;
		LogUtil.info("destroyACar "+roleId+"|"+this.familyInfo.getFamilyId());
		HashSet<Long> careRoles = eCar.destroy();
		//给关注这个镖车的玩家重新刷一个新的镖车
		if (careRoles != null && careRoles.size() > 0) {
			for (Long long1 : careRoles) {
				HashSet<Long>tmpSet = careEscCarMap.get(long1);
				if (tmpSet == null || tmpSet.size() <= 0) {
					continue;
				}
				tmpSet.remove(long1);
			}
//				if (long1 == eCar.getRoleId()) {
//					continue;
//				}
//				List<Long> ls = RandomUtil.random(esCarPosMap.keySet(),1,tmpSet);
//				if (ls.size() == 1) {
//					//新增关注镖车
//					long long2 = ls.get(0);
//					tmpSet.add(long2);
//					//镖车的主人也加上关注列表
//					EscortCar escortCar = esCarPosMap.get(long2);
//					escortCar.addCare(long1);
//					//下发新增镖车
//					ClientFamilyEscortCars cFamilyEscortCars = new ClientFamilyEscortCars();
//					cFamilyEscortCars.addEscortCar(escortCar);
//					PacketManager.send(long1, cFamilyEscortCars);
//				}
//			}
		}
	}

	/**
	 * 创建新的镖车
	 * @param fAuth
	 * @param role
	 * @return
	 */
	public EscortCar createANewCar( FighterEntity role){
		EscortCar eCar = new EscortCar();
		eCar.setRoleId(role.getRoleId());
		eCar.setFightScore(role.getFightScore());
		eCar.setName( role.getName());
		esCarPosMap.put(role.getRoleId(), eCar);
		return eCar;
	}
	
	/**
	 * 加入运镖地图
	 * @param fAuth
	 * @param entity
	 */
	public void joinActEscort(int serverId,FamilyAuth fAuth, FighterEntity entity, FighterEntity buddyEntity) {
		//先移除旧的镖车信息
		long roleId = Long.parseLong(entity.getUniqueId());
		initCareEscCar(roleId);
	}
	
	/**
	 * 开始劫镖
	 * @param entity
	 */
	public void joinLootEscort(int serverId,FighterEntity entity, FighterEntity buddyEntity) {
		long roleId = Long.parseLong(entity.getUniqueId());
		initCareEscCar(roleId);
	}
	public void lootEscort(String self, String aim) {
		FamilyEscortRoleData selfRoleData = lootEscortMap.get(Long.parseLong(self));
		if (selfRoleData == null) {
			   PlayerUtil.send(Long.parseLong(self), new ClientText("已方数据异常"));
			return;
		}
        if (selfRoleData.isFighting()) {
            PlayerUtil.send(selfRoleData.getRoleId(), new ClientText("本方正在战斗中"));
            return;
        }
        FamilyEscortRoleData aimRoleData = actEscortMap.get(Long.parseLong(aim));
        if (aimRoleData == null) {
        	  PlayerUtil.send(selfRoleData.getRoleId(), new ClientText("对方数据异常"));
            return;
        }
        if (aimRoleData.isFighting()) {
            PlayerUtil.send(selfRoleData.getRoleId(), new ClientText("对方正在战斗中"));
            return;
        }
        
        if (aimRoleData.isUnbeated()) {
       	 	PlayerUtil.send(selfRoleData.getRoleId(), new ClientText("对方正处于免战状态"));
            return;
       }
        
		EscortCar aimCar = esCarPosMap.get(Long.parseLong(aim));
		if(!isCarCanFighted(aimCar,selfRoleData.getRoleId())){
			return;
		}
		if (aimCar != null) {
			aimCar.setRunStatus(EscortCar.STAT_FIGHTING);
			aimCar.setFightBeginTime(System.currentTimeMillis());
		}
		
		LogUtil.info("lootEscort "+self+"|"+ aim+"|"+this.familyInfo.getFamilyId());
		
		famEsSerActor.createFight(selfRoleData, aimRoleData, -1, familyInfo.getFamilyId(),familyInfo.getFamilyId());
	}
	
	/**
	 * 镖车能否被攻击
	 * @param aimCar
	 * @param myRoleId
	 * @return
	 */
	public boolean isCarCanFighted(EscortCar aimCar,long myRoleId){
		if (aimCar != null) {
			if (aimCar.getRunStatus() == EscortCar.STAT_FIGHTING) {
				PlayerUtil.send(myRoleId, new ClientText("对方正在战斗中"));
				return false;
			}
			if (aimCar.getRunStatus() == EscortCar.STAT_UNBEAT) {
				PlayerUtil.send(myRoleId, new ClientText("对方正处于免战状态"));
				return false;
			}
		}
		return true;
	}
	
	//刷运镖车给玩家
	public void initCareEscCar(long roleId){
		HashSet<Long>careCars = careEscCarMap.get(roleId);
		if (careCars == null) {
			careCars = new HashSet<>();
			careEscCarMap.put(roleId,careCars);
		}
		//镖车刷满
		fullCareList(careCars, roleId);
		if (careCars.size() >0) {
			List<EscortCar>ls = new ArrayList<EscortCar>();
			for (Long long1 : careCars) {
				EscortCar car = esCarPosMap.get(long1);
				if (car != null) {
					ls.add(car);
				}
			}
//			System.err.println(this.getFamilyInfo().getFamilyName()+" 场景 发镖车给 玩家 "+roleId+"|"+careCars.size()+"|"+ls.size());
			ClientFamilyEscortCars cFamilyEscortCars = new ClientFamilyEscortCars(ls);
			PacketManager.send(roleId, cFamilyEscortCars);
		}
	}
	
	/**
	 * 玩家战斗状态
	 * @param roleId
	 */
	public void updatePlayerStatus(long roleId){
		ClientEscortPlayerStatus eps = new ClientEscortPlayerStatus();
		 Map<Long,Byte> playerStatusMap = new HashMap<>();
		 List<Long> list = arroundPlayerList.get(roleId);
		 if(list == null)return;
		for (Long aRoleId : list) {
			FamilyEscortRoleData erd = getFightData(aRoleId);
			if (erd == null)
				continue;
			playerStatusMap.put(erd.getRoleId(), erd.getStatus());
		}
		//自己的状态也加入
		FamilyEscortRoleData myErd = getFightData(roleId);
		if(myErd != null){
			playerStatusMap.put(myErd.getRoleId(), myErd.getStatus());
		}
		eps.setPlayerStatusMap(playerStatusMap);
		PacketManager.send(roleId, eps);
	}
	
	public static int MAX_CAR = 15;
	
	/**
	 * 刷满镖车
	 * @param careCars
	 * @param roleId
	 */
	public void fullCareList(HashSet<Long>careCars,long roleId){
		//自己的镖车一定要刷出来
		if(esCarPosMap.containsKey(roleId)){
			careCars.add(roleId);
		}
		int leftSize = MAX_CAR -careCars.size() ;
		if(leftSize <=0)return;
		List<EscortCar> ls = RandomUtil.random(esCarPosMap.values(),leftSize);
		if (ls != null && ls.size() > 0) {
			for (EscortCar escortCar : ls) {
				escortCar.addCare(roleId);
				careCars.add(escortCar.getRoleId());
			}
		}
	}
	
	/**
	 * 给不在战斗的玩家发送数据包
	 * @param packet
	 */
	public void sendPacketToPlayerNoFight(Packet packet) {
		try {
			for (FamilyEscortRoleData ferd : actEscortMap.values()) {
				try {
					if (ferd.isFighting())
						continue;
					PacketManager.send(ferd.getRoleId(), packet);
				} catch (Throwable e) {
					LogUtil.error(e.getMessage(), e);
				}
			}
			for (FamilyEscortRoleData ferd : lootEscortMap.values()) {
				try {
					if (ferd.isFighting())
						continue;
					PacketManager.send(ferd.getRoleId(), packet);
				} catch (Throwable e) {
					LogUtil.error(e.getMessage(), e);
				}
			}
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
	}

//	public void onFightEnd(long attacker, long defender, long loser) {
//		EscortCar car = esCarPosMap.get(defender);
//		// 杀人战斗结束, 清除状态
//		clearFightState(attacker);
//		clearFightState(defender);
//		// 处理劫镖人的奖励
//		Map<Integer, Integer> robAwardMap = FamilyEscortConfig.config.getRobAwardMap();
//		int awardId = 0;
//		long winner = (attacker == loser ? defender : attacker);
//
//		long loseUnBeatedTime = System.currentTimeMillis() + FamilyEscortConfig.config.getLoseProtectTime();
//		long winUnBeatedTime = System.currentTimeMillis() + FamilyEscortConfig.config.getWinProtectTime();
//		//杀人事件处理
//		ServiceHelper.roleService().notice(winner, new FamilyEscortKillEvent());
//		if (car != null) {
//			car.setRunStatus(EscortCar.STAT_UNBEAT);
//			if (winner == attacker) {
//				// 劫镖成功
//				car.setUnBeatTime(winUnBeatedTime);
//				// 角色无敌时间
//				car.reduceStar();
//				int key = RandomUtil.powerRandom(robAwardMap);
//				//劫镖奖励
//				awardId = robAwardMap.get(key);
//			} else {
//				car.setUnBeatTime(loseUnBeatedTime);
//			}
//		}
//		//角色无敌时间
//		FamilyEscortRoleData winRoleData = getFightData(winner);
//		if (winRoleData != null) {
//			winRoleData.setUnBeatTime(winUnBeatedTime);
//		}
//		FamilyEscortRoleData loseRoleData = getFightData(loser);
//		if (loseRoleData != null) {
//			loseRoleData.setUnBeatTime(loseUnBeatedTime);
//		}
//		// 掉落奖励，及弹结算框
//		ServiceHelper.roleService().notice(winner, new FamilyEscortDropEvent(true, awardId));
//		ServiceHelper.roleService().notice(loser, new FamilyEscortDropEvent(false, 0));
//	}
	
	public static void main(String[] args){
		Map<Integer, Integer> robAwardMap = new HashMap<>();
		robAwardMap.put(1, 1000);
		robAwardMap.put(2, 500);
		robAwardMap.put(3, 200);
		robAwardMap.put(4, 100);
//		robAwardMap.put(5, 500);
		
		Map<Integer, Integer> robAwardCount = new HashMap<>();
		for(int i = 0;i <10000;i ++){
			int value = RandomUtil.powerRandom(robAwardMap);
			if(robAwardCount.containsKey(value)){
				robAwardCount.put(value, robAwardCount.get(value)+1);
			}else{
				robAwardCount.put(value, 1);
			}
		}
		for(Entry<Integer, Integer> result:robAwardCount.entrySet()){
			System.err.println(result.getKey()+"="+result.getValue());
		}
	}
	
	
	public void onFightEnd(long attacker, long defender, long robCount,boolean isRobWin) {
		EscortCar car = esCarPosMap.get(defender);
		// 处理劫镖人的奖励
		Map<Integer, Integer> robAwardMap = FamilyEscortConfig.config.getRobAwardMap();
		int awardId = 0;
		//已经劫镖的次数
		//代表攻击者胜利
		long winner = (isRobWin ?  attacker: defender);
		long loser = (isRobWin ?  defender: attacker);
		

		ServiceHelper.roleService().notice(attacker, new FamilyEscortFightEvent());
		ServiceHelper.roleService().notice(defender, new FamilyEscortFightEvent());
		//杀人事件处理
		ServiceHelper.roleService().notice(winner, new FamilyEscortKillEvent());
		byte isHasCar = 0;
		if (car != null) {
			isHasCar = 1;
			if (winner == attacker) {
				car.reduceStar(getFightData(car.getRoleId()));
				
				//小于0代表没奖励
				if(robCount >= 0){
					if (robCount < FamilyEscortConfig.config.getRobTime()) {
						int key = RandomUtil.powerRandom(robAwardMap);
						// 劫镖奖励
						awardId = key;
					} else if (robCount < FamilyEscortConfig.config.getRobTime() +FamilyEscortConfig.config.getRobBaseAwardMaxTime()) {
						awardId = FamilyEscortConfig.config.getRobBaseAward();
					}
				}
				
//				if(isGetLootAward){
//					int key = RandomUtil.powerRandom(robAwardMap);
//					//劫镖奖励
//					awardId = key;
//				}else{
//					awardId = FamilyEscortConfig.config.getRobBaseAward();
//				}
			}
		}
		//角色无敌时间
		FamilyEscortRoleData winRoleData = getFightData(winner);
		if (winRoleData != null) {
			winRoleData.setLastFightWin((byte) 1);
		}
		FamilyEscortRoleData loseRoleData = getFightData(loser);
		if (loseRoleData != null) {
			loseRoleData.setLastFightWin((byte)0);
		}
		// 掉落奖励，及弹结算框
		ServiceHelper.roleService().notice(winner, new FamilyEscortDropEvent(true, awardId,isHasCar));
		ServiceHelper.roleService().notice(loser, new FamilyEscortDropEvent(false, 0,isHasCar));
		
		LogUtil.info("onFightEnd escort "+attacker+"|"+ defender+"|"+loser+"|"+this.familyInfo.getFamilyId());
	}
	
	/**
	 * 从战斗场景回来后的状态处理
	 * @param attacker
	 * @param isWin
	 */
	public void onFireEndAfterEnterEscortScene(long attacker, byte isWin){
		EscortCar car = esCarPosMap.get(attacker);
		// 杀人战斗结束, 清除状态
		clearFightState(attacker);
		long unBeatedTime =0;
		if(isWin ==1){
			unBeatedTime = System.currentTimeMillis() + FamilyEscortConfig.config.getWinProtectTime();
		}else{
			unBeatedTime = System.currentTimeMillis() + FamilyEscortConfig.config.getLoseProtectTime();
		}
		if (car != null) {
			car.setRunStatus(EscortCar.STAT_UNBEAT);
			car.setUnBeatTime(unBeatedTime);
		}
		//角色无敌时间
		FamilyEscortRoleData winRoleData = getFightData(attacker);
		if (winRoleData != null) {
			winRoleData.setUnBeatTime(unBeatedTime);
		}
	}
	
	public void clearCarState(long roleId){
		EscortCar car = esCarPosMap.get(roleId);
		if(car == null)return;
		car.setRunStatus(EscortCar.STAT_RUN);
		car.setUnBeatTime(0);
		car.setOffLineTime(0);
		car.setFightBeginTime(0);
	}
	
	public FamilyEscortRoleData getFightData(long roleId) {
		FamilyEscortRoleData roleData = actEscortMap.get(roleId);
		if (roleData == null) {
			roleData = lootEscortMap.get(roleId);
		}
		return roleData;
	}
	
	public void clearFightState(long roleId) {
		FamilyEscortRoleData roleData = getFightData(roleId);
		roleData.setFightIdAndStartTimestamp(null, 0L);
		//离线状态也清除
		roleData.setOffLineTime(0);
	}
	
	/**
	 * 检查镖车状态
	 */
	public void checkEscortCarStatus(){
		try {
			if (esCarPosMap == null) {
				esCarPosMap = new HashMap<>();
			}
			Iterator<Long> it = esCarPosMap.keySet().iterator();
			EscortCar eCar;
			long now = System.currentTimeMillis();
			List<Long> removes = null;
			while (it.hasNext()) {
				try {
					Long long1 = (Long) it.next();
					eCar = esCarPosMap.get(long1);
					if (eCar.isMove()) {
						// 计算运镖时间，一次加一秒
						eCar.addRunTime((int) (FamilyEscortServiceActor.runInterval*1000));
					}
					//无敌状态处理
					if (eCar.getUnBeatTime() > 0 && now >eCar.getUnBeatTime() ) {
						eCar.setUnBeatTime(0);
						eCar.setRunStatus(EscortCar.STAT_RUN);
					}
					//战斗超时处理
					if (eCar.getRunStatus() == EscortCar.STAT_RUN && now - eCar.getFightBeginTime() > FamilyEscortConst.timeoutOf1v1) {
						eCar.setUnBeatTime(0);
						eCar.setRunStatus(EscortCar.STAT_RUN);
					}
					if (eCar.getRunTime() >=FamilyEscortConfig.config.getEscortPathTime()
							&& eCar.getRunStatus() != EscortCar.STAT_FIGHTING) {
						// 判断运镖结束
						if (removes == null) {
							removes = new ArrayList<Long>();
						}
						removes.add(long1);
						continue;
					}
					if (eCar.getRunStatus() == EscortCar.STAT_RUN) {
						//每运一段距离，则开始算是否有障碍物
						if (eCar.getRunTime() - eCar.getBarrierTimer() >= FamilyEscortConfig.config.getRoadBlockDistance()*1000) {
							// 计算障碍物
							eCar.setBarrierTimer(eCar.getRunTime());
							Random r = new Random();
							if (r.nextInt(1000) < FamilyEscortConfig.config.getRoadBlockOdds()) {
//								stopEscortCar(eCar);
								eCar.setRunStatus(EscortCar.STAT_BARRY);
								ClientEscCarFlush cBarrier = new ClientEscCarFlush((byte) 0,eCar);
								PacketManager.send(long1, cBarrier);
							}
						}
					}
				} catch (Throwable e) {
					LogUtil.error(e.getMessage(), e);
				}
			}
			if (removes != null) {
				for (Long long1 : removes) {
					eCar = esCarPosMap.get(long1);
					finishEscort(eCar);
				}
			}
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
		//更新镖车状态给客户端
		updateCarStatusToClient();
		
	}
	
	private long lastUpdateTime = 0;
	
	public static int UPDATE_CAR_INTERVAL = 900;

	public void updateCarStatusToClient() {
		try {
			long now = System.currentTimeMillis();
			if (now - lastUpdateTime < UPDATE_CAR_INTERVAL)
				return;
			lastUpdateTime = now;
			for (Long roleId : careEscCarMap.keySet()) {
				try {
					//如果在战斗中，则不下发
					FamilyEscortRoleData fed = getFightData(roleId);
					if(fed ==null){
//						LogUtil.info("updateCarStatusToClient no update  fed ==null"+roleId);
						continue;
					}
					if(fed.isFighting()){
//						LogUtil.info("updateCarStatusToClient no update  roleId is FIGHTing"+roleId);
						continue;
					}
					//清除离线的玩家运镖数据
					clearOffLineCar();
					//镖车状态自动修复
					checkAndRepairState();
					//刷镖车给玩家
					initCareEscCar(roleId);
					//刷玩家状态休息
					updatePlayerStatus(roleId);
				} catch (Throwable e) {
					LogUtil.error(e.getMessage(), e);
				}
			}
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 清场处理
	 */
	public void clearUp() {
		
//		private Map<Long, FamilyEscortRoleData> actEscortMap = new HashMap<>();
//	    private Map<Long, FamilyEscortRoleData> lootEscortMap = new HashMap<>();
		//镖车处理
		try {
			for (EscortCar eCar : esCarPosMap.values()) {
				finishEscort(eCar);
			}
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
		
		//发送清场提示
	    ClientFamilyEscortClearTips ect = new  ClientFamilyEscortClearTips();
	    Map<Long, Byte> sendRMap = new HashMap<>();
	    
	    for(FamilyEscortRoleData erd :actEscortMap.values()){
	    	if(sendRMap.containsKey(erd.getRoleId()))continue;
	    	sendRMap.put(erd.getRoleId(), (byte)(1));
	    	PacketManager.send(erd.getRoleId(), ect);
	    }
	    for(FamilyEscortRoleData erd :lootEscortMap.values()){
	     	if(sendRMap.containsKey(erd.getRoleId()))continue;
	    	sendRMap.put(erd.getRoleId(), (byte)(1));
	    	PacketManager.send(erd.getRoleId(), ect);
	    }
	}

	//完成运镖
	public void finishEscort(EscortCar eCar) {
		try {
			if(eCar.isFinished())return;
			eCar.setFinished(true);
			int[] awardIds = FamilyEscortConfig.config.getEscortStartAwardMap().get((int) eCar.getStarLv());
			if(awardIds == null){
				LogUtil.error("FamilyEscort no finish award starlv="+eCar.getStarLv());
			}else{
				//发奖励
				for(int val :awardIds){
					ServiceHelper.roleService().notice(eCar.getRoleId(), new FamilyEscortDropEvent(true, val,true,(byte) 0));
				}
				ClientEscortResult er = new ClientEscortResult();
				er.setStartCount((byte) eCar.getStarLv());
				int leftEsCount = FamilyEscortConfig.config.getEscortTime() -eCar.getEscoctTime();
				leftEsCount = leftEsCount>0?leftEsCount:0;
				er.setEscortCount(leftEsCount);
				er.setEscortAward(awardIds[0]);
				if(awardIds.length >1){
					for(int i =1;i < awardIds.length;i ++){
						er.getExtAward().add(awardIds[i]);
					}
				}
				PacketManager.send(eCar.getRoleId(), er);
			}
			destroyACar(eCar.getRoleId());
			//清理状态
			FamilyEscortRoleData efd = getFightData(eCar.getRoleId());
			if (efd != null) {
				efd.initFightData(efd.getEntity());
			}
			LogUtil.info("finishEscort "+eCar.getRoleId()+"|"+ eCar.getEscoctTime()+"|"+eCar.getStarLv()+"|"+this.familyInfo.getFamilyId());
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(),e);
		}
	}
	
	public void clearEscort(EscortCar eCar) {
		try {
			if(eCar.isFinished())return;
			eCar.setFinished(true);
			int[] awardIds = FamilyEscortConfig.config.getEscortStartAwardMap().get(FamilyEscortConfig.config.getRobMinStar());
			if(awardIds == null){
				LogUtil.error("FamilyEscort no finish award starlv="+eCar.getStarLv());
			}else{
				//发奖励
				for(int val :awardIds){
					ServiceHelper.roleService().notice(eCar.getRoleId(), new FamilyEscortDropEvent(true, val,true,(byte) 0));
				}
			}
			destroyACar(eCar.getRoleId());
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(),e);
		}
	}
	
	
	
	public void removeBarrier(long roleId){
		EscortCar eCar = esCarPosMap.get(roleId);
		if (eCar == null) {
			return;
		}
		if (!(eCar.getRunStatus() == EscortCar.STAT_BARRY)) {
			return;
		}
		runEscortCar(eCar);
		eCar.setRunStatus(EscortCar.STAT_RUN);
		ClientFamEscBarrier cBarrier = new ClientFamEscBarrier((byte)1);
		PacketManager.send(roleId, cBarrier);
	}
	
	// 停止镖车，如遇到障碍，或者被劫镖
	public void stopEscortCar(long roleId) {
		EscortCar escortCar = esCarPosMap.get(roleId);
		if (escortCar != null) {
			stopEscortCar(escortCar);
		}
	}
	
	public void stopEscortCar(EscortCar escortCar) {
		escortCar.setRunStatus(EscortCar.STAT_STOP);
	}

	// 恢复镖车行走
	public void runEscortCar(long roleId) {
		EscortCar escortCar = esCarPosMap.get(roleId);
		if (escortCar != null) {
			escortCar.setRunStatus(EscortCar.STAT_RUN);
		}
	}

	@Override
	public int compareTo(FamilyEscortMap o) {
		int fSize = this.getEsCarSize();
		int oSize = o.getEsCarSize();
		if(fSize == oSize){
			return this.familyInfo.getFightRank() - o.familyInfo.getFightRank();
		}
		return fSize - oSize;
	}
	
	public void runEscortCar(EscortCar escortCar) {
		escortCar.setRunStatus(EscortCar.STAT_RUN);
	}
	
	public boolean isEnimy(long selfRoleId, long aimRoleId){
		if((actEscortMap.containsKey(selfRoleId)
				&& lootEscortMap.containsKey(aimRoleId))
				|| (actEscortMap.containsKey(aimRoleId)
				&& lootEscortMap.containsKey(selfRoleId))){
			return true;
		}
		return false;
	}
	
	/**
	 * 主动攻击玩家
	 * @param selfRoleId
	 * @param aimRoleId
	 */
	public void killRole(long selfRoleId, long aimRoleId){
		if(!isEnimy( selfRoleId,  aimRoleId)){
			PacketManager.send(selfRoleId, new ClientWarning("不是敌方玩家，不能攻击"));
			return;
		}
		
		FamilyEscortRoleData selfRoleData = getFightData(selfRoleId);
		if (selfRoleData == null) {
			PacketManager.send(selfRoleId, new ClientWarning("不在场景中"));
			return;
		}
		if (esCarPosMap.containsKey(selfRoleId)) {
			PacketManager.send(selfRoleId, new ClientWarning("您正在运镖，不能发起此操作"));
			return;
		}
        FamilyEscortRoleData aimRoleData = getFightData(aimRoleId);
		if (aimRoleData == null) {
			PacketManager.send(selfRoleId, new ClientWarning("对方不在场景中，不能发起此操作"));
			return;
		}
        if (selfRoleData.isFighting()) {
            PlayerUtil.send(selfRoleData.getRoleId(), new ClientText("本方正在战斗中"));
            return;
        }
        if (aimRoleData.isFighting()) {
            PlayerUtil.send(selfRoleData.getRoleId(), new ClientText("对方正在战斗中"));
            return;
        }
//        if (selfRoleData.isUnbeated()) {
//        	 PlayerUtil.send(selfRoleData.getRoleId(), new ClientText("本方正处于免战状态"));
//             return;
//        }
        if (aimRoleData.isUnbeated()) {
       	 	PlayerUtil.send(selfRoleData.getRoleId(), new ClientText("对方正处于免战状态"));
            return;
       }
        
        //有镖车的，以镖车的状态为准
    	EscortCar aimCar = esCarPosMap.get(aimRoleId);
		if(!isCarCanFighted(aimCar,selfRoleData.getRoleId())){
			return;
		}
		if (aimCar != null) {
			aimCar.setRunStatus(EscortCar.STAT_FIGHTING);
			aimCar.setFightBeginTime(System.currentTimeMillis());
		}
        selfRoleData.setUnBeatTime(0);
        
    	LogUtil.info("killRole "+selfRoleId+"|"+ aimRoleId+"|"+this.familyInfo.getFamilyId());
        
		this.famEsSerActor.createFight(selfRoleData, aimRoleData, -1, familyInfo.getFamilyId(),familyInfo.getFamilyId());
	}

	public HashMap<Long, List<Long>> getArroundPlayerList() {
		return arroundPlayerList;
	}

	public void setArroundPlayerList(HashMap<Long, List<Long>> arroundPlayerList) {
		this.arroundPlayerList = arroundPlayerList;
	}
}
