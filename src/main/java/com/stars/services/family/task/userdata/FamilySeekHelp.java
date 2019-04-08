package com.stars.services.family.task.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.DateUtil;

import java.util.Calendar;

/**
 * @author huzhipeng
 *
 */
public class FamilySeekHelp extends DbRow {
	
	private long roleId;
	
	private String roleName;//玩家名字
	
	private long familyId;
	
	private int taskId;
	
	private byte waitHandle;//等待处理状态
	
	private int time;//生成帮助的当天的零点时间
	
	public FamilySeekHelp() {
		// TODO Auto-generated constructor stub
	}

	public FamilySeekHelp(long roleId, String roleName, long familyId, int taskId, byte waitHandle) {
		super();
		this.roleId = roleId;
		this.roleName = roleName;
		this.familyId = familyId;
		this.taskId = taskId;
		this.waitHandle = waitHandle;
		this.time = (int)(DateUtil.getZeroTime(Calendar.getInstance())/1000);
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public long getFamilyId() {
		return familyId;
	}

	public void setFamilyId(long familyId) {
		this.familyId = familyId;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public byte getWaitHandle() {
		return waitHandle;
	}

	public void setWaitHandle(byte waitHandle) {
		this.waitHandle = waitHandle;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	@Override
	public String getChangeSql() {
		StringBuffer condition = new StringBuffer();
		condition.append(" `roleid`=").append(this.roleId).append(" and `taskid`=").append(this.taskId);
		return SqlUtil.getSql(this, DBUtil.DB_USER, "familytaskseekhelp", condition.toString());
	}

	@Override
	public String getDeleteSql() {
		StringBuffer condition = new StringBuffer();
		condition.append(" `roleid`=").append(this.roleId).append(" and `taskid`=").append(this.taskId);
		return "delete from familytaskseekhelp where "+condition;
	}

}
