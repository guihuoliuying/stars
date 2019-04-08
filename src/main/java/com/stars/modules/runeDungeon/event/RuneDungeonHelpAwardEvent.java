package com.stars.modules.runeDungeon.event;

import com.stars.core.event.Event;

import java.util.Map;

public class RuneDungeonHelpAwardEvent extends Event {
	
	public static final byte ONLINE_AFTER_FIGHT = 1;
	
	private byte opType;
	
	private Map<Integer, Integer> toolMap;
	
	private long beHelpId;

	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
	}

	public Map<Integer, Integer> getToolMap() {
		return toolMap;
	}

	public void setToolMap(Map<Integer, Integer> toolMap) {
		this.toolMap = toolMap;
	}

	public long getBeHelpId() {
		return beHelpId;
	}

	public void setBeHelpId(long beHelpId) {
		this.beHelpId = beHelpId;
	}

}
