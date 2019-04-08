package com.stars.modules.task.userdata;

import java.util.HashMap;
import java.util.Map;

public class RoleCanAcceptTaskTable{
	
	private Map<Integer, RoleCanAcceptTask> canAcceptTaskMap;
		
	public RoleCanAcceptTaskTable(){
		canAcceptTaskMap = new HashMap<Integer, RoleCanAcceptTask>();
	}

	public Map<Integer, RoleCanAcceptTask> getCanAcceptTaskMap() {
		return canAcceptTaskMap;
	}

	public void setCanAcceptTaskMap(Map<Integer, RoleCanAcceptTask> canAcceptTaskMap) {
		this.canAcceptTaskMap = canAcceptTaskMap;
	}
	
	public void putCanAcceptTaskRaw(RoleCanAcceptTask roleCanAcceptTask){
		canAcceptTaskMap.put(roleCanAcceptTask.getTaskId(), roleCanAcceptTask);
	}

	public RoleCanAcceptTask getCanAcceptTaskRaw(int taskId){
		return canAcceptTaskMap.get(taskId);
	}
	
	public boolean removeCanAcceptTask(RoleCanAcceptTask roleAccept){
		if (canAcceptTaskMap.containsKey(roleAccept.getTaskId())) {
			canAcceptTaskMap.remove(roleAccept.getTaskId());
			return true;
		}
		
		return false;
	}
	
	public boolean removeCanAcceptTask(int taskId){
		if (!canAcceptTaskMap.containsKey(taskId)) {
			return false;
		}
		
		canAcceptTaskMap.remove(taskId);
		return true;
	}
}
