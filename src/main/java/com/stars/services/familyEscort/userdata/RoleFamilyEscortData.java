package com.stars.services.familyEscort.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * 玩家运镖次数记录
 * 
 * @author xieyuejun
 *
 */
public class RoleFamilyEscortData extends DbRow {

	private long roleId;
	private int escortTime;
	private int robTime;

	public int getEscortTime() {
		return escortTime;
	}

	public void setEscortTime(int escortTime) {
		this.escortTime = escortTime;
	}


	@Override
	public String getChangeSql() {
		return SqlUtil.getSql(this, DBUtil.DB_COMMON, "rolefamilyescort",
                " roleid=" + this.getRoleId());
	}

	@Override
	public String getDeleteSql() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public int getRobTime() {
		return robTime;
	}

	public void setRobTime(int robTime) {
		this.robTime = robTime;
	}

}
