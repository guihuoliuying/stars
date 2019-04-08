package com.stars.services.familyEscort;

import com.stars.modules.familyEscort.FamilyEscortConst;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.services.familyEscort.route.EscortCar;
import com.stars.util.StringUtil;

/**
 * Created by zhaowenshuo on 2017/4/22.
 */
public class FamilyEscortRoleData {
	
	public static int UNBEATED_TIME = 5000;//无敌持续时间
	
    private long roleId;
    private int serverId;//FIXME 这个要初始化
    private FighterEntity entity; // 玩家的战斗实体
    private FighterEntity buddyEntity; // 伙伴的战斗实体
    private String fightId; // 战斗id
    private long fightStartTimestamp; // 战斗开始的时间戳
    
    private byte lastFightWin = 0;//上一场战斗是否赢了
    
    private long unBeatTime; //无敌截止时间
    
    private long offLineTime;//断线开始时间
    
    private int[] position;
    
    public FamilyEscortRoleData(){}
    
    /**
     * 初始化战斗状态
     * @param role
     */
    public void initFightData(FighterEntity role){
		this.setEntity(role);
		unBeatTime = 0;
		lastFightWin = 0;
		fightId = null;
		fightStartTimestamp = 0;
		offLineTime =0;
    }
    
    
    /**
     * 进入场景后的数据初始化处理
     */
    public void initStateByEnterScene(){
    	fightId = null;
    	offLineTime =0;
    	fightStartTimestamp = 0;
    }
    
    public byte getStatus(){
    	if(isFighting()){
    		return EscortCar.STAT_FIGHTING;
    	}
    	if(isUnbeated() || isOffline()){
    		return EscortCar.STAT_UNBEAT;
    	}
    	return EscortCar.STAT_RUN;
    }
    
    public boolean isOffline(){
    	if(this.offLineTime >0){
    		return true;
    	}
    	return false;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public FighterEntity getEntity() {
        return entity;
    }

    public void setEntity(FighterEntity entity) {
        this.entity = entity;
    }

    public FighterEntity getBuddyEntity() {
        return buddyEntity;
    }

    public void setBuddyEntity(FighterEntity buddyEntity) {
        this.buddyEntity = buddyEntity;
    }

    public String getFightId() {
        return fightId;
    }

    public long getFightStartTimestamp() {
        return fightStartTimestamp;
    }

    public void setFightIdAndStartTimestamp(String fightId, long fightStartTimestamp) {
        this.fightId = fightId;
        this.fightStartTimestamp = fightStartTimestamp;
    }
    
	public boolean isUnbeated(){
		if(System.currentTimeMillis() <unBeatTime || isOffline()){
			return true;
		}
		return false;
	}

    public boolean isFighting() {
    	if(fightStartTimestamp != 0 &&System.currentTimeMillis() - fightStartTimestamp > FamilyEscortConst.timeoutOf1v1){
    		fightId = null;
    		return false;
    	}
        return StringUtil.isNotEmpty(fightId);
    }

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int[] getPosition() {
		return position;
	}

	public void setPosition(int[] position) {
		this.position = position;
	}

	public long getUnBeatTime() {
		return unBeatTime;
	}

	public void setUnBeatTime(long unBeatTime) {
		if(unBeatTime == 0 || unBeatTime >this.unBeatTime){
			this.unBeatTime = unBeatTime;
		}
	}

	public byte getLastFightWin() {
		return lastFightWin;
	}

	public void setLastFightWin(byte lastFightWin) {
		this.lastFightWin = lastFightWin;
	}

	public long getOffLineTime() {
		return offLineTime;
	}

	public void setOffLineTime(long offLineTime) {
		this.offLineTime = offLineTime;
	}
}
