package com.stars.modules.skyrank.event;

import com.stars.core.event.Event;

/**
 * 
 * 积分变化处理事件
 * 
 * @author xieyuejun
 *
 */
public class SkyRankScoreHandleEvent extends Event {

	private short fightType;
	private byte isWin;

	public SkyRankScoreHandleEvent(short fightType, byte isWin) {
		this.fightType = fightType;
		this.isWin = isWin;
	}

	public short getFightType() {
		return fightType;
	}

	public void setFightType(short fightType) {
		this.fightType = fightType;
	}

	public byte getIsWin() {
		return isWin;
	}

	public void setIsWin(byte isWin) {
		this.isWin = isWin;
	}

}
