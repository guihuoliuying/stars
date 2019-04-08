package com.stars.modules.trump.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by zhouyaohui on 2016/9/19.
 */
public class RoleTrumpRow extends DbRow {

    private long roleId;
    private int trumpId;
    private short level;
    private byte awake;
    private byte position;
//    private byte click;

    public byte getPosition() {
        return position;
    }

    public void setPosition(byte position) {
        this.position = position;
    }

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getTrumpId() {
        return trumpId;
    }



    public void setTrumpId(int trumpId) {
        this.trumpId = trumpId;
    }

    public byte getAwake() {
        return awake;
    }

    public void setAwake(byte awake) {
        this.awake = awake;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roletrump", "roleid = " + roleId + " and trumpid = " + trumpId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from roletrump where roleid = " + roleId + " and trumpid = " + trumpId;
    }
}
