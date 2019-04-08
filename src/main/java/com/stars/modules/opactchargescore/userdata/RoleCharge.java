package com.stars.modules.opactchargescore.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by likang on 2017/3/29.
 */
public class RoleCharge extends DbRow {
	private static final long serialVersionUID = 4669110572034305588L;
	private long roleId;
	private int totalCharge; // 活动期间的累计充值,单位：元
	private long time;// 充值的日期
	private long validity;// 有效时间

	public RoleCharge() {
	}

	public RoleCharge(long roleId) {
		this.roleId = roleId;
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public int getTotalCharge() {
		return totalCharge;
	}

	public void setTotalCharge(int totalCharge) {
		this.totalCharge = totalCharge;
	}

	public void addTotalCharge(int money) {
		if (money <= 0)
			return;
		this.totalCharge += money;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}

	public long getValidity() {
		return validity;
	}

	public void setValidity(long validity) {
		this.validity = validity;
	}

	public void reset(long validity) {
		this.validity = validity;
		this.totalCharge = 0;
	}

	@Override
	public String getChangeSql() {
		return SqlUtil.getSql(this, DBUtil.DB_USER, "rolecharge", "`roleid`=" + roleId);
	}

	@Override
	public String getDeleteSql() {
		return SqlUtil.getDeleteSql("rolecharge", "`roleid`=" + roleId);
	}
}
