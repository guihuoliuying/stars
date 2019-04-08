package com.stars.multiserver.daily5v5.data;

public class Daily5v5BuffInfo {
	
	private int buffId;
	
	private int[] paramArr;
	
	private int buffLevel;
	
	private int effectId;
	
	public Daily5v5BuffInfo() {
		// TODO Auto-generated constructor stub
	}

	public Daily5v5BuffInfo(int effectId, int buffId, int[] paramArr, int buffLevel) {
		super();
		this.effectId = effectId;
		this.buffId = buffId;
		this.paramArr = paramArr;
		this.buffLevel = buffLevel;
	}

	public int getBuffId() {
		return buffId;
	}

	public void setBuffId(int buffId) {
		this.buffId = buffId;
	}

	public int[] getParamArr() {
		return paramArr;
	}

	public void setParamArr(int[] paramArr) {
		this.paramArr = paramArr;
	}

	public int getBuffLevel() {
		return buffLevel;
	}

	public void setBuffLevel(int buffLevel) {
		this.buffLevel = buffLevel;
	}

	public int getEffectId() {
		return effectId;
	}

	public void setEffectId(int effectId) {
		this.effectId = effectId;
	}

}
