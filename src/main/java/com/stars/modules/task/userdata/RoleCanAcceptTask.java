package com.stars.modules.task.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

public class RoleCanAcceptTask extends DbRow{	
	private long roleId;
	/**
	 * 任务ID，同产品数据的ID相同
	 */
	private int taskId;
	
	public RoleCanAcceptTask(){
		
	}
	
	public RoleCanAcceptTask(long roleId , int taskId){
		this.roleId = roleId;
		this.taskId = taskId;
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
			return SqlUtil.getSql(this,DBUtil.DB_USER, "rolecanaccepttask",
        		" roleId='" + this.roleId + "' and taskid='"+taskId+"'");
			
		} catch (Exception e) {
			// TODO: handle exception
		}
        return "";
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolecanaccepttask", " roleId='" + this.roleId + "' and taskid='"+taskId+"'");
    }
}
