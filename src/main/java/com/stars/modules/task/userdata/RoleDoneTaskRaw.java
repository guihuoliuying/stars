package com.stars.modules.task.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

import java.util.HashMap;
import java.util.Set;

public class RoleDoneTaskRaw extends DbRow{
	private String tasks;
	private long roleId;
	private HashMap<Integer, Object> doneTaskMap;
	
	public RoleDoneTaskRaw(){
		
	}
	
	public RoleDoneTaskRaw(long roleId){
		this.roleId = roleId;
		this.tasks = "";
		doneTaskMap = new HashMap<Integer, Object>();
//		setInsertStatus();
	}
	
	public String getTasks() {
		if (doneTaskMap == null || doneTaskMap.size() <= 0) {
			return "";
		}
		StringBuffer bf = new StringBuffer();
		Set<Integer>st = doneTaskMap.keySet();
		for (int id:st) {
			if (bf.length() > 0) {
				bf.append(";");
			}
			bf.append(id);
		}
		return bf.toString();
	}
	public void setTasks(String tasks) {
		this.tasks = tasks;
		doneTaskMap = new HashMap<Integer, Object>();
		if (tasks != null && !tasks.equals("")) {
			String[] sts = tasks.split(";");
			for(String id:sts){
				doneTaskMap.put(Integer.parseInt(id), null);
			}
		}
	}
	public boolean containsTask(int taskId){
		return doneTaskMap.containsKey(taskId);
	}
	public void removeTask(int taskId){
		doneTaskMap.remove(taskId);
	}
	public void addTask(int taskId){
		if (!doneTaskMap.containsKey(taskId)) {
			doneTaskMap.put(taskId, null);
			this.tasks = getTasks();
		}
	}
	public long getRoleId() {
		return this.roleId;
	}


	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public HashMap<Integer, Object> getDoneTaskMap() {
		return doneTaskMap;
	}
	public void setDoneTaskMap(HashMap<Integer, Object> doneTaskMap) {
		this.doneTaskMap = doneTaskMap;
	}
	@Override
	public String getChangeSql(){
		return SqlUtil.getSql(this, DBUtil.DB_USER, "roledonetask",
        		" roleid='" + this.roleId + "'");
	}

	@Override
    public String getDeleteSql(){
		return "";
    }
}
