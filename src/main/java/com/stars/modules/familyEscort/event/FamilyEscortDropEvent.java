package com.stars.modules.familyEscort.event;

import com.stars.core.event.Event;

public class FamilyEscortDropEvent extends Event {

	private boolean isWin;
	private int dropId;
	private byte isHasCar; //是否属于劫镖 0否 1是
	
	private boolean isFinishEscort = false;//是否是完成运镖的掉落
	
	public FamilyEscortDropEvent(boolean isWin, int dropId,byte isHasCar){
		this.isWin = isWin;
		this.dropId = dropId;
		this.isHasCar = isHasCar;
	}

	public FamilyEscortDropEvent(boolean isWin, int dropId,boolean isFinishEscort,byte isHasCar){
		this.isWin = isWin;
		this.dropId = dropId;
		this.setFinishEscort(isFinishEscort);
		this.isHasCar = isHasCar;
	}

	public boolean isWin() {
		return isWin;
	}

	public int getDropId() {
		return dropId;
	}

	public boolean isFinishEscort() {
		return isFinishEscort;
	}

	public void setFinishEscort(boolean isFinishEscort) {
		this.isFinishEscort = isFinishEscort;
	}

	public byte getIsHasCar() {
		return isHasCar;
	}

	public void setIsHasCar(byte isHasCar) {
		this.isHasCar = isHasCar;
	}
}
