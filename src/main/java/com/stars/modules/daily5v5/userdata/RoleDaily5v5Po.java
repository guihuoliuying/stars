package com.stars.modules.daily5v5.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

public class RoleDaily5v5Po extends DbRow{
	
	private long roleId;
	
	private int win;
	
	private int lose;
	
//	private int fightId;
	
	private byte frequency;//当天已参与次数
	
	public RoleDaily5v5Po() {
		// TODO Auto-generated constructor stub
	}
	
	public RoleDaily5v5Po(long roleId) {
		this.roleId = roleId;
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public int getWin() {
		return win;
	}

	public void setWin(int win) {
		this.win = win;
	}

	public int getLose() {
		return lose;
	}

	public void setLose(int lose) {
		this.lose = lose;
	}

//	public int getFightId() {
//		return fightId;
//	}
//
//	public void setFightId(int fightId) {
//		this.fightId = fightId;
//	}

	public byte getFrequency() {
		return frequency;
	}

	public void setFrequency(byte frequency) {
		this.frequency = frequency;
	}

	@Override
	public String getChangeSql() {
		return SqlUtil.getSql(this, DBUtil.DB_USER, "roledaily5v5", "`roleid`="+roleId);
	}

	@Override
	public String getDeleteSql() {
		return "delete from roledaily5v5 where `roleid` = "+roleId;
	}

}
