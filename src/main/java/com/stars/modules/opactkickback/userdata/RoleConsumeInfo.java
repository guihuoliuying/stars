package com.stars.modules.opactkickback.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * *
 * 
 * @author likang 2017/04/13
 *
 */
public class RoleConsumeInfo extends DbRow {
	private static final long serialVersionUID = -6209858842524938768L;
	private long roleId;
	private int consume;
	private long validity; // 数据的有效期
	private Set<Integer> sendAwardSet = new HashSet<>();

	public RoleConsumeInfo() {

	}

	public RoleConsumeInfo(long roleId) {
		this.roleId = roleId;
	}

	public List<Integer> getSendAwardList() {
		List<Integer> list = new ArrayList<Integer>();
		list.addAll(sendAwardSet);
		return list;
	}

	public boolean hasSendAward(int id) {
		if (sendAwardSet.isEmpty()) {
			return false;
		}
		return sendAwardSet.contains(id);
	}

	public void recordSendAward(int id) {
		if (sendAwardSet == null) {
			sendAwardSet = new HashSet<>();
		}
		if (sendAwardSet.contains(id)) {
			return;
		}
		sendAwardSet.add(id);
	}

	public String getSendAward() {
		return StringUtil.makeString(sendAwardSet, ',');
	}

	public void setSendAward(String sendAward) throws Exception {
		if (sendAward.isEmpty()) {
			return;
		}
		this.sendAwardSet = StringUtil.toHashSet(sendAward, Integer.class, ',');
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public int getConsume() {
		return consume;
	}

	public void setConsume(int consume) {
		this.consume = consume;
	}

	public void incConsume(int consume) {
		this.consume += consume;
	}

	public void reset(long validity) {
		this.consume = 0;
		this.validity = validity;
		this.sendAwardSet.clear();
	}

	@Override
	public String getChangeSql() {
		return SqlUtil.getSql(this, DBUtil.DB_USER, "roleconsumeinfo", "roleid=" + roleId);
	}

	@Override
	public String getDeleteSql() {
		return SqlUtil.getDeleteSql("roleconsumeinfo", "`roleid`=" + roleId);
	}

	public long getValidity() {
		return validity;
	}

	public void setValidity(long validity) {
		this.validity = validity;
	}

}
