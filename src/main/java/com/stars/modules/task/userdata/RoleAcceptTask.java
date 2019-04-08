package com.stars.modules.task.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.task.TaskManager;

public class RoleAcceptTask extends DbRow{
	/**
	 * 任务ID，同产品数据的ID相同
	 */
	private int taskId;
	
	private long roleId;
	
	/**
	 * 当前进度
	 */
	private int process;
	
	
	public RoleAcceptTask(){
		
	}
	
	public RoleAcceptTask(int taskId,long roleId){
		this.taskId = taskId;
		this.roleId = roleId;
//		setInsertStatus();
	}
	
	public RoleAcceptTask(int taskId,long roleId,int process){
		this.taskId = taskId;
		this.roleId = roleId;
		this.process = process;
//		setInsertStatus();
	}

	
	public boolean acceptNextTask(){
		return true;
	}	

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int id) {
		this.taskId = id;
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}
	
	
//	TODO:异常这边后续处理
	@Override
    public String getChangeSql() {
		try {
			return SqlUtil.getSql(this,DBUtil.DB_USER, "roletask",
        		" roleId='" + this.roleId + "' and taskid='"+taskId+"'");
			
		} catch (Exception e) {
			// TODO: handle exception
		}
        return "";
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roletask", " roleId='" + this.roleId + "' and taskid='"+taskId+"'");
    }

	public int getProcess() {
		return process;
	}

	public void setProcess(int process) {
		this.process = process;
	}
	
	public byte getState(){
		int targetCount = TaskManager.getTaskById(taskId).getTargetCount();
		return process >= targetCount?TaskManager.Task_State_CanSubmit:TaskManager.Task_State_Accept;
	}
	
}
