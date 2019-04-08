package com.stars.modules.camp.event;

import com.stars.core.event.Event;
import com.stars.modules.camp.pojo.CampCityFightData;
import com.stars.modules.scene.imp.fight.CampCityFightScene;
import com.stars.multiserver.camp.pojo.CampPlayerImageData;

import java.util.List;

public class CampCityFightEvent extends Event {
	
	public static final byte FIGHT_END = 1;//结算
	public static final byte GET_PLAYERIMAGE = 2;//获取镜像数据
	public static final byte BACK_TO_CITY = 3;//回城
	public static final byte CHANGE_SCENE = 4;//队员切换场景
	public static final byte SYN_ENEMY_INFO = 5;//同步敌人信息
	
	private byte opType;
	
	private byte result;
	
	private int integral;
	
	private int chaCityId;
	
	private boolean teamAddition;
	
	private List<CampCityFightData> integralList;
	
	private CampCityFightScene scene;
	
	private List<CampPlayerImageData> enemyList;

	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
	}

	public byte getResult() {
		return result;
	}

	public void setResult(byte result) {
		this.result = result;
	}

	public int getIntegral() {
		return integral;
	}

	public void setIntegral(int integral) {
		this.integral = integral;
	}

	public List<CampCityFightData> getIntegralList() {
		return integralList;
	}

	public void setIntegralList(List<CampCityFightData> integralList) {
		this.integralList = integralList;
	}

	public int getChaCityId() {
		return chaCityId;
	}

	public void setChaCityId(int chaCityId) {
		this.chaCityId = chaCityId;
	}

	public boolean isTeamAddition() {
		return teamAddition;
	}

	public void setTeamAddition(boolean teamAddition) {
		this.teamAddition = teamAddition;
	}

	public CampCityFightScene getScene() {
		return scene;
	}

	public void setScene(CampCityFightScene scene) {
		this.scene = scene;
	}

	public List<CampPlayerImageData> getEnemyList() {
		return enemyList;
	}

	public void setEnemyList(List<CampPlayerImageData> enemyList) {
		this.enemyList = enemyList;
	}

}
