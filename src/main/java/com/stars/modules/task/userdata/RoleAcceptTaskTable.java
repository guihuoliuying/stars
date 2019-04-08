package com.stars.modules.task.userdata;

import com.stars.modules.task.TaskManager;
import com.stars.modules.task.prodata.TaskVo;

import java.util.*;

public class RoleAcceptTaskTable{
	
	private Map<Integer, RoleAcceptTask> acceptTaskMap;
		
	/**
	 * 按任务关键字来存储，便于event事件获取任务
	 * key = key1+key2，key1分别用tool、monster表示,key2用toolId或者monsterId表示
	 */
	private transient HashMap<String, ArrayList<RoleAcceptTask>> acceptTaskByKey;
		
	
	public RoleAcceptTaskTable(long key){
		acceptTaskMap = new HashMap<Integer, RoleAcceptTask>();
		acceptTaskByKey = new HashMap<String, ArrayList<RoleAcceptTask>>();
	}
	
	public void init(){
		if (acceptTaskByKey == null) {
			acceptTaskByKey = new HashMap<String, ArrayList<RoleAcceptTask>>();
		}
		if (acceptTaskMap.size() <= 0) {
			return;
		}
		Collection<RoleAcceptTask> cl = acceptTaskMap.values();
		for (RoleAcceptTask initRatb:cl) {
			TaskVo tv = TaskManager.getTaskById(initRatb.getTaskId()); 
			if (tv != null) {
				ArrayList<RoleAcceptTask> keyList = acceptTaskByKey.get(tv.getKey());
				if (keyList == null) {
					keyList = new ArrayList<RoleAcceptTask>();
					acceptTaskByKey.put(tv.getKey(), keyList);
				}
				keyList.add(initRatb);
			}
		}
	}
	
	public RoleAcceptTask getAcceptTaskRaw(int taskId){
		return acceptTaskMap.get(taskId);
	}
	
	public void putAcceptTaskRaw(RoleAcceptTask roleAccept,byte sort){
		acceptTaskMap.put(roleAccept.getTaskId(), roleAccept);
		TaskVo tv = TaskManager.getTaskById(roleAccept.getTaskId());
		if (acceptTaskByKey == null) {
			acceptTaskByKey = new HashMap<String, ArrayList<RoleAcceptTask>>();
		}
		ArrayList<RoleAcceptTask> keyList = acceptTaskByKey.get(tv.getKey());
		if (keyList == null) {
			keyList = new ArrayList<RoleAcceptTask>();
			acceptTaskByKey.put(tv.getKey(), keyList);
		}
		keyList.add(roleAccept);
	}
	
	
	public boolean removeAcceptTask(RoleAcceptTask roleAccept){
		acceptTaskMap.remove(roleAccept.getTaskId());
		TaskVo tbv = TaskManager.getTaskById(roleAccept.getTaskId());
		List keyList = acceptTaskByKey.get(tbv.getKey());
		if (keyList != null) {
			keyList.remove(roleAccept);
		}
		return true;
	}
	
	public boolean removeAcceptTask(int taskId){
		RoleAcceptTask roleAccept = acceptTaskMap.get(taskId);
		if (roleAccept == null) {
			return false;
		}
		acceptTaskMap.remove(taskId);
		TaskVo tbv = TaskManager.getTaskById(taskId);
		List keyList = acceptTaskByKey.get(tbv.getKey());
		if (keyList != null) {
			keyList.remove(roleAccept);
		}
		return true;
	}
	
	public List<RoleAcceptTask> getAcceptTaskByKey(String key){
		if (acceptTaskByKey == null) {
			return null;
		}
		return acceptTaskByKey.get(key);
	}

	public Map<Integer, RoleAcceptTask> getAcceptTaskMap() {
		return acceptTaskMap;
	}

	public void setAcceptTaskMap(Map<Integer, RoleAcceptTask> acceptTaskMap) {
		this.acceptTaskMap = acceptTaskMap;
	}

}
