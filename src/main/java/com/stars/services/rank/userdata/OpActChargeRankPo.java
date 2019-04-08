package com.stars.services.rank.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.SqlUtil;
import com.stars.modules.opactchargescore.userdata.RoleCharge;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.rank.RankConstant;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;

import java.util.Date;

/**
 * Created by likang on 2017-04-13 10:34
 */

public class OpActChargeRankPo extends AbstractRankPo {
	private static final long serialVersionUID = -842026374930825480L;
	// 入库数据
	private long roleId;
	private int totalCharge;
	private long time;

	public static OpActChargeRankPo build(RoleCharge rc) {
		OpActChargeRankPo ocrp = new OpActChargeRankPo();
		ocrp.roleId = rc.getRoleId();
		ocrp.totalCharge = rc.getTotalCharge();
		return ocrp;
	}
	@Override
	public int compareTo(Object o) {
		OpActChargeRankPo other = (OpActChargeRankPo) o;
		if (roleId == other.roleId) {
			return 0;
		}
		if (totalCharge != other.totalCharge) {
			return totalCharge > other.totalCharge ? -1 : 1;
		}
		if (time != other.time) {
			return time > other.time ? -1 : 1;
		}
		return roleId > other.roleId ? -1 : 1;
	}

	@Override
	public long getUniqueId() {
		return roleId;
	}

	@Override
	public void writeToBuffer(int rankId, NewByteBuffer buff) {
		buff.writeInt(getRank());
		buff.writeString(String.valueOf(getRoleId()));
		buff.writeInt(getTotalCharge());
	}

	public void writeToBuffer(NewByteBuffer buff) {
		writeToBuffer(RankConstant.RANKID_CHARGESOCRE, buff);
	}

	@Override
	public AbstractRankPo copy() {
		try {
			return (AbstractRankPo) this.clone();
		} catch (CloneNotSupportedException e) {
			LogUtil.error("OpActChargeRankPo克隆失败", e);
		}
		return null;
	}

	public String toMyString() {
		Date date = new Date(time);
		String dateStr = DateUtil.formatDate(date, DateUtil.YMDHMS);
		return String.format("roleId[%s] charge[%s] time[%s]", roleId, totalCharge, dateStr);
	}

	@Override
	public String getChangeSql() {
		return SqlUtil.getSql(this, DBUtil.DB_USER, "opactchargerank", "`roleid`=" + roleId);
	}

	@Override
	public String getDeleteSql() {
		return String.format("delete from `opactchargerank` where `roleid`=%s;", roleId);
	}
	public int getTotalCharge() {
		return totalCharge;
	}
	public void setTotalCharge(int totalCharge) {
		this.totalCharge = totalCharge;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}

	public long getRoleId() {
		return roleId;
	}
	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

}
