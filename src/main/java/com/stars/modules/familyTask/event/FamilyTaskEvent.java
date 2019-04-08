package com.stars.modules.familyTask.event;

import com.stars.core.event.Event;
import com.stars.services.family.task.userdata.FamilySeekHelp;

import java.util.List;

public class FamilyTaskEvent extends Event {
	
	public static byte SEND_HELP_LIST = 1;
	
	public static byte BE_HELP_COMMIT = 2;
	
	public static byte FIX_STATE = 3;
	
	private byte opType;
	
	private List<FamilySeekHelp> list;
	
	private int taskId;
	
	private String name;
		
	public FamilyTaskEvent(byte opType) {
		this.opType = opType;
	}

	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
	}

	public List<FamilySeekHelp> getList() {
		return list;
	}

	public void setList(List<FamilySeekHelp> list) {
		this.list = list;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
