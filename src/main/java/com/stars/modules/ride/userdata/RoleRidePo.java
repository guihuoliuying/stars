package com.stars.modules.ride.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.modules.ride.RideConst;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/9/18.
 */
public class RoleRidePo extends DbRow {

    private long roleId; //
    private int rideId; // 坐骑id
    private int awakeLevel; // 觉醒等级
    private byte owned; // 是否拥有
    private byte active; // 是否骑乘
//    private byte click;//是否点击过，，（新旧坐骑）
    private int endTime;//限时坐骑截止时间
    private byte firstGet;//第一次获得
    private byte sendOver;//离线有缓存过期， 重新发送提示
    
    public RoleRidePo() {
    }

    public RoleRidePo(long roleId, int rideId, int awakeLevel, byte owned, byte active, byte click) {
        this.roleId = roleId;
        this.rideId = rideId;
        this.awakeLevel = awakeLevel;
        this.owned = owned;
        this.active = active;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleride", "`roleid`=" + roleId + " and `rideid`=" + rideId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `roleride` where `roleid`=" + roleId + " and `rideid`=" + rideId;
    }

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(rideId);
        buff.writeByte(owned);
        buff.writeInt(awakeLevel);
        buff.writeInt(endTime);
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getRideId() {
        return rideId;
    }

    public void setRideId(int rideId) {
        this.rideId = rideId;
    }

    public int getAwakeLevel() {
        return awakeLevel;
    }

    public void setAwakeLevel(int awakeLevel) {
        this.awakeLevel = awakeLevel;
    }

    public byte getOwned() {
        return owned;
    }

    public void setOwned(byte owned) {
        this.owned = owned;
    }

    public boolean isOwned() {
        return owned == RideConst.OWNED;
    }

    public byte getActive() {
        return active;
    }

    public void setActive(byte active) {
        this.active = active;
    }

    public boolean isActive() {
        return active == RideConst.ACTIVE;
    }

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public byte getFirstGet() {
		return firstGet;
	}

	public void setFirstGet(byte firstGet) {
		this.firstGet = firstGet;
	}

	public byte getSendOver() {
		return sendOver;
	}

	public void setSendOver(byte sendOver) {
		this.sendOver = sendOver;
	}
}
