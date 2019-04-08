package com.stars.modules.marry.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by zhoujin on 2017/4/17.
 */
public class RoleRing extends DbRow {

    public RoleRing() {}

    public RoleRing(long roleId) {
        this.roleId = roleId;
    }
    private long roleId;
    private int ringId;
    private int pos;
    private short level;

    public long getRoleId() {return this.roleId;}

    public void setRoleId(long roleId) {this.roleId = roleId;}

    public int getRingId() {return this.ringId;}

    public void setRingId(int ringId) {this.ringId = ringId;}

    public int getPos() {return this.pos;}

    public void setPos(int pos) {this.pos = pos;}

    public short getLevel() {return this.level;}

    public void setLevel(short level) {this.level = level;}

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolering", " `roleid`=" + roleId + " and pos=" + pos);
    }

    @Override
    public String getDeleteSql() {
        return null;
    }
}
