package com.stars.services.familyEscort.route;

import com.stars.modules.familyEscort.packet.ClientEscCarFlush;
import com.stars.modules.familyEscort.prodata.FamilyEscortConfig;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.familyEscort.FamilyEscortRoleData;
import com.stars.util.RandomUtil;

import java.util.HashSet;
import java.util.Map;

/**
 * @author dengzhou
 *角色当前在线路图上的root点
 */
public class EscortCar {
	
	public static byte STAT_RUN = 0;//行走
	
	public static byte STAT_STOP = 1;//停止
	
	public static byte STAT_FIGHTING = 2;//战斗中
	
	public static byte STAT_BARRY = 3;//障碍状态
	
	public static byte STAT_UNBEAT = 4;//无敌状态
	
	public static byte STAT_OFFLINE = 5;//离线状态
	
	
	
	/**
	 * 行走时间
	 */
	private int runTime = 0;
	
	private long roleId;//对应的角色ID
	
	private String name;
	
	private int fightScore;
	
	private int escoctTime = 1;
	/**
	 * 行走状态
	 */
	private byte runStatus = STAT_RUN;
	
	/**
	 * 关注这辆镖车的人
	 */
	private HashSet<Long>careOfRoles;
	
	private long barrierTimer = 0;
	
	//无敌开始时间
	private long unBeatTime = 0;
	
	//断线开始时间
	 private long offLineTime;
	
	private byte starLv = 10;//星级，初始化10颗星，客户端处理要除以2
	
	//开始的战斗时间
	private long fightBeginTime = 0;
	
	private boolean isFinished = false;
	
	
	public EscortCar(){
		careOfRoles = new HashSet<Long>();
	}
	
	public void reduceStar(FamilyEscortRoleData roleFightData) {
		if (getStarLv() > FamilyEscortConfig.config.getRobMinStar()) {
			// 处理掉星
			Map<Integer, Integer> robedStarMap = FamilyEscortConfig.config.getRobedStarMap();
			int key = RandomUtil.powerRandom(robedStarMap);
			int star = getStarLv() - key;
			if (star < FamilyEscortConfig.config.getRobMinStar()) {
				star = FamilyEscortConfig.config.getRobMinStar();
			}
			if(star <= FamilyEscortConfig.config.getRobMinStar()){
				setRunStatus(EscortCar.STAT_UNBEAT);
				long unBeatedTime = System.currentTimeMillis() + 1000*60*60;
				setUnBeatTime(unBeatedTime);
				//角色的无敌状态也改变
				if(roleFightData!= null){
					roleFightData.setUnBeatTime(unBeatedTime);
				}
			}
			setStarLv((byte) star);
		}
	}
	
	public boolean isMove(){
		if(this.offLineTime <=0 &&(this.runStatus == STAT_RUN || this.runStatus == STAT_UNBEAT
				|| starLv <= FamilyEscortConfig.config.getRobMinStar())){
			return true;
		}
		return false;
	}

	public byte getRunStatus() {
		if(this.offLineTime >0){
			return STAT_OFFLINE;
		}
		return runStatus;
	}
	public void setRunStatus(byte runStatus) {
		boolean updateStatus = false;
		if(this.runStatus != runStatus){
			updateStatus = true;
		}
		this.runStatus = runStatus;
		if(updateStatus){
			updateStatusToCareRoles();
		}
	}
	
	public long getRoleId() {
		return roleId;
	}
	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public void addCare(long roleId){
		careOfRoles.add(roleId);
	}
	
	public void cancelCare(long roleId){
		careOfRoles.remove(roleId);
	}
	
	public void updateStatusToCareRoles(){
		if (careOfRoles.size() <= 0) {
			return;
		}
		ClientEscCarFlush escCarFlush = new ClientEscCarFlush((byte)0, this);
		for (Long long1 : careOfRoles) {
			PacketManager.send(long1, escCarFlush);
		}
	}
	
	public HashSet<Long> destroy(){
		if (careOfRoles.size() <= 0) {
			return careOfRoles;
		}
		ClientEscCarFlush escCarFlush = new ClientEscCarFlush((byte)1, this);
		for (Long long1 : careOfRoles) {
			PacketManager.send(long1, escCarFlush);
		}
		return careOfRoles;
	}

	public int getRunTime() {
		return runTime;
	}

	public void setRunTime(int runTime) {
		this.runTime = runTime;
	} 
	
	public void addRunTime(int add){
		runTime = runTime + add;
	}

	public long getBarrierTimer() {
		return barrierTimer;
	}

	public void setBarrierTimer(long barrierTimer) {
		this.barrierTimer = barrierTimer;
	}

	public long getUnBeatTime() {
		return unBeatTime;
	}

	public void setUnBeatTime(long unBeatTime) {
		if(unBeatTime == 0 || unBeatTime > this.unBeatTime){
			this.unBeatTime = unBeatTime;
		}
	}

	public byte getStarLv() {
		return starLv;
	}

	public void setStarLv(byte starLv) {
		this.starLv = starLv;
	}

	public long getFightBeginTime() {
		return fightBeginTime;
	}

	public void setFightBeginTime(long fightBeginTime) {
		this.fightBeginTime = fightBeginTime;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public int getEscoctTime() {
		return escoctTime;
	}

	public void setEscoctTime(int escoctTime) {
		this.escoctTime = escoctTime;
	}

	public long getOffLineTime() {
		return offLineTime;
	}

	public void setOffLineTime(long offLineTime) {
		this.offLineTime = offLineTime;
	}

	public int getFightScore() {
		return fightScore;
	}

	public void setFightScore(int fightScore) {
		this.fightScore = fightScore;
	}
	
}
